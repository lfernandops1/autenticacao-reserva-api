package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.ControleAcessoUsuario;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.ContaBloqueadaException;
import com.autenticacao.api.app.repository.ControleAcessoUsuarioRepository;
import com.autenticacao.api.app.service.TentativaLoginService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TentativaLoginServiceImpl implements TentativaLoginService {

  private static final int MAX_TENTATIVAS = 5;
  private static final int TEMPO_BLOQUEIO_MIN = 15;

  private final ControleAcessoUsuarioRepository controleAcessoUsuarioRepository;

  /**
   * Valida se a conta do usuário está bloqueada.
   *
   * @param usuario Usuário a ser verificado
   * @throws ContaBloqueadaException se a conta estiver com bloqueio ativo
   */
  @Override
  public void validarBloqueio(Usuario usuario) {
    ControleAcessoUsuario controle = buscarOuCriarControle(usuario);

    if (controle.getBloqueadoAte() != null
        && controle.getBloqueadoAte().isAfter(LocalDateTime.now())) {
      throw new ContaBloqueadaException(
          CONTA_BLOQUEADA.getChave() + " até " + controle.getBloqueadoAte());
    }
  }

  /**
   * Registra uma tentativa de login falha. Bloqueia a conta após o limite.
   *
   * @param usuario Usuário a ser atualizado
   */
  @Override
  public void registrarFalha(Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          ControleAcessoUsuario controle = buscarOuCriarControle(usuario);

          int tentativas = controle.getTentativasFalhas() + 1;
          controle.setTentativasFalhas(tentativas);

          if (tentativas >= MAX_TENTATIVAS) {
            controle.setBloqueadoAte(LocalDateTime.now().plusMinutes(TEMPO_BLOQUEIO_MIN));
          }

          controleAcessoUsuarioRepository.save(controle);
          return null;
        },
        ERRO_REGISTRAR_TENTATIVA_LOGIN.getChave());
  }

  /**
   * Reseta o número de tentativas falhas do usuário e remove bloqueios temporários.
   *
   * @param usuario Usuário a ser atualizado
   */
  @Override
  public void resetarTentativas(Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          ControleAcessoUsuario controle = buscarOuCriarControle(usuario);

          controle.setTentativasFalhas(0);
          controle.setBloqueadoAte(null);

          controleAcessoUsuarioRepository.save(controle);
          return null;
        },
        ERRO_RESETAR_TENTATIVAS_LOGIN.getChave());
  }

  /** Busca o ControleAcessoUsuario associado ao usuário ou cria um novo se não existir. */
  private ControleAcessoUsuario buscarOuCriarControle(Usuario usuario) {
    Optional<ControleAcessoUsuario> controleOpt =
        controleAcessoUsuarioRepository.findByUsuario(usuario);

    if (controleOpt.isPresent()) {
      return controleOpt.get();
    } else {
      ControleAcessoUsuario novoControle = new ControleAcessoUsuario();
      novoControle.setUsuario(usuario);
      novoControle.setTentativasFalhas(0);
      novoControle.setBloqueadoAte(null);
      return controleAcessoUsuarioRepository.save(novoControle);
    }
  }
}
