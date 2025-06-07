package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.util.SecurityUtil.obterUsuarioLogado;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.autenticacao.api.app.config.security.TokenService;
import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.exception.SenhaExpiradaException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutenticacaoServiceImpl implements UserDetailsService {

  private final UsuarioRepository usuarioRepository;
  private final AutenticacaoRepository autenticacaoRepository;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenServiceImpl refreshTokenService;
  private final TentativaLoginServiceImpl tentativaLoginService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return usuarioRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Email ou senha inválidos"));
  }

  public LoginResponseDTO login(
      LoginUsuarioRequestDTO data, AuthenticationManager authenticationManager) {
    Usuario usuario = buscarUsuarioPorEmail(data.email());

    tentativaLoginService.validarBloqueio(usuario);

    try {
      Usuario usuarioAutenticado = autenticarUsuario(data, authenticationManager);

      tentativaLoginService.resetarTentativas(usuario);

      validarSenhaExpirada(usuarioAutenticado);

      return criarLoginResponse(usuarioAutenticado);

    } catch (BadCredentialsException ex) {
      tentativaLoginService.registrarFalha(usuario);
      throw ex;
    }
  }

  private Usuario buscarUsuarioPorEmail(String email) {
    return usuarioRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
  }

  private Usuario autenticarUsuario(
      LoginUsuarioRequestDTO data, AuthenticationManager authenticationManager) {
    var authToken = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
    var authentication = authenticationManager.authenticate(authToken);
    return (Usuario) authentication.getPrincipal();
  }

  private void validarSenhaExpirada(Usuario usuario) {
    if (senhaExpirada(usuario.getAutenticacao())) {
      throw new SenhaExpiradaException("Sua senha expirou, por favor altere.");
    }
  }

  private LoginResponseDTO criarLoginResponse(Usuario usuario) {
    String accessToken = tokenService.generateToken(usuario);
    String refreshToken = refreshTokenService.createRefreshToken(usuario);
    return new LoginResponseDTO(accessToken, refreshToken);
  }

  public void criarAutenticacao(CadastroUsuarioRequest requestDTO, Usuario usuario) {
    if (autenticacaoRepository.findByUsuario(usuario).isPresent()) {
      throw new IllegalStateException("Já existe autenticação associada a esse usuário");
    }
    Autenticacao autenticacao = obterAutenticacao(requestDTO, usuario);
    autenticacaoRepository.save(autenticacao);
  }

  public void alterarSenha(AlterarSenhaRequestDTO requestDTO) {
    Usuario usuario =
        usuarioRepository
            .findById(Objects.requireNonNull(obterUsuarioLogado()).getId())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    Autenticacao autenticacao = usuario.getAutenticacao();
    autenticacao.setSenha(passwordEncoder.encode(requestDTO.senha()));
    autenticacao.setDataHoraAtualizacao(LocalDateTime.now());
    autenticacaoRepository.save(autenticacao);
  }

  public void desativarAutenticacao(UUID usuarioId) {
    Autenticacao autenticacao =
        autenticacaoRepository
            .buscarPorUsuarioId(usuarioId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado na autenticação"));
    autenticacao.setAtivo(false);
    autenticacao.setDataHoraExclusao(LocalDateTime.now());
    autenticacaoRepository.save(autenticacao);
  }

  public boolean senhaExpirada(Autenticacao autenticacao) {
    return autenticacao.getDataHoraAtualizacao().plusDays(90).isBefore(LocalDateTime.now());
  }

  private Autenticacao obterAutenticacao(
      CadastroUsuarioRequest usuarioCadastroRequestDTO, Usuario usuario) {
    Autenticacao autenticacao = new Autenticacao();
    autenticacao.setSenha(passwordEncoder.encode(usuarioCadastroRequestDTO.senha()));
    autenticacao.setEmail(usuario.getEmail());
    autenticacao.setUsuario(usuario);
    autenticacao.setDataHoraAtualizacao(LocalDateTime.now());
    autenticacao.setDataHoraCriacao(LocalDateTime.now());
    autenticacao.setAtivo(true);
    return autenticacao;
  }
}
