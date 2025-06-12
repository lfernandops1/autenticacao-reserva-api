package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.config.security.TokenService;
import com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider;
import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.AutenticacaoService;
import com.autenticacao.api.app.service.SenhaService;
import com.autenticacao.api.app.exception.UsuarioNaoAutenticadoException;
import com.autenticacao.api.app.exception.UsuarioNaoEncontradoException;

import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela autenticação de usuários, incluindo ‘login’ e alteração de senha, com
 * tratamento centralizado de exceções para garantir maior robustez e rastreabilidade.
 */
@Service
@RequiredArgsConstructor
public class AutenticacaoServiceImpl implements AutenticacaoService {

  private final UsuarioRepository usuarioRepository;
  private final TokenService tokenService;
  private final RefreshTokenServiceImpl refreshTokenService;
  private final SenhaService senhaService;
  private final TentativaLoginServiceImpl tentativaLoginService;
  private final @Lazy AuthenticationManager authenticationManager;
  private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
  private static final Logger logger = LoggerFactory.getLogger(AutenticacaoServiceImpl.class);

  /**
   * Realiza o ‘login’ do usuário com base nos dados de autenticação informados. Aplica tratamento
   * centralizado para exceções e persiste tentativas de ‘login’.
   *
   * @param dto Dados para ‘login’ contendo e-mail e senha.
   * @return Objeto {@link LoginResponseDTO} com ‘tokens’ de acesso e refresh.
   * @throws UsuarioNaoEncontradoException se o usuário não for encontrado pelo e-mail.
   * @throws BadCredentialsException em caso de credenciais inválidas.
   * @throws UsuarioNaoAutenticadoException se o usuário não estiver autenticado.
   * @throws RuntimeException para outros erros inesperados durante o login.
   */
  @Override
  public LoginResponseDTO login(LoginUsuarioRequestDTO dto) {
    return executarComandoComTratamentoErroComMensagem(
        () -> {
          Usuario usuario = buscarUsuarioPorEmail(dto.email());
          tentativaLoginService.validarBloqueio(usuario);

          try {
            Usuario autenticado = autenticarUsuario(dto);
            tentativaLoginService.resetarTentativas(usuario);
            senhaService.validarSenhaExpirada(autenticado);

            logger.info("Usuário autenticado com sucesso: {}", autenticado.getEmail());

            return new LoginResponseDTO(
                tokenService.generateToken(autenticado),
                refreshTokenService.createRefreshToken(autenticado));

          } catch (BadCredentialsException e) {
            tentativaLoginService.registrarFalha(usuario);
            logger.warn("Falha de autenticação para o email: {}", dto.email());
            throw e;
          }
        },
        ERRO_REALIZAR_LOGIN.getChave());
  }

  /**
   * Altera a senha do usuário atualmente autenticado. O método obtém o usuário logado e realiza a
   * alteração da senha, aplicando tratamento centralizado de exceções.
   *
   * @param requestDTO Dados contendo a nova senha.
   * @throws UsuarioNaoAutenticadoException se não houver usuário autenticado.
   * @throws UsuarioNaoEncontradoException se o usuário autenticado não for encontrado no banco.
   * @throws RuntimeException para outros erros inesperados durante a alteração de senha.
   */
  @Override
  public void alterarSenha(AlterarSenhaRequestDTO requestDTO) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          UUID idUsuario =
              usuarioAutenticadoProvider
                  .getIdUsuarioLogado()
                  .orElseThrow(
                      () -> new UsuarioNaoAutenticadoException(USUARIO_NAO_AUTENTICADO.getChave()));

          Usuario usuario =
              usuarioRepository
                  .findById(idUsuario)
                  .orElseThrow(
                      () -> new UsuarioNaoEncontradoException(USUARIO_NAO_ENCONTRADO.getChave()));

          senhaService.alterarSenha(usuario, requestDTO.senha());
          logger.info("Senha alterada com sucesso para usuário: {}", usuario.getEmail());

          return null;
        },
        ERRO_ALTERAR_SENHA.getChave());
  }

  /**
   * Busca o usuário pelo e-mail informado.
   *
   * @param email E-mail do usuário a ser buscado.
   * @return Usuário encontrado.
   * @throws UsuarioNaoEncontradoException caso não encontre o usuário pelo e-mail.
   */
  private Usuario buscarUsuarioPorEmail(String email) {
    return usuarioRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsuarioNaoEncontradoException(USUARIO_NAO_ENCONTRADO.getChave()));
  }

  /**
   * Realiza a autenticação do usuário com base nos dados fornecidos.
   *
   * @param dto Dados para ‘login’ contendo e-mail e senha.
   * @return Usuário autenticado.
   * @throws BadCredentialsException caso as credenciais estejam incorretas.
   */
  private Usuario autenticarUsuario(LoginUsuarioRequestDTO dto) {
    var authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
    var authentication = authenticationManager.authenticate(authToken);
    return (Usuario) authentication.getPrincipal();
  }
}
