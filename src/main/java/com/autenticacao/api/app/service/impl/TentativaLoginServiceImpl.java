package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.ContaBloqueadaException;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.TentativaLoginService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por controle de tentativas de login e bloqueios de segurança.
 *
 * <p>Define políticas como limite de tentativas falhas e bloqueio temporário de conta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TentativaLoginServiceImpl implements TentativaLoginService {

  private static final int MAX_TENTATIVAS = 5;
  private static final int TEMPO_BLOQUEIO_MIN = 15;

  private final UsuarioRepository usuarioRepository;

  /**
   * Valida se a conta do usuário está bloqueada.
   *
   * @param usuario Usuário a ser verificado
   * @throws ContaBloqueadaException se a conta estiver com bloqueio ativo
   */
  public void validarBloqueio(Usuario usuario) {
    if (usuario.getBloqueadoAte() != null
        && usuario.getBloqueadoAte().isAfter(LocalDateTime.now())) {
      throw new ContaBloqueadaException(
          CONTA_BLOQUEADA.getChave() + " até " + usuario.getBloqueadoAte());
    }
  }

  /**
   * Registra uma tentativa de login falha. Bloqueia a conta após o limite.
   *
   * @param usuario Usuário a ser atualizado
   */
  public void registrarFalha(Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          int tentativas = usuario.getTentativasFalhas() + 1;
          usuario.setTentativasFalhas(tentativas);

          if (tentativas >= MAX_TENTATIVAS) {
            usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(TEMPO_BLOQUEIO_MIN));
          }

          usuarioRepository.save(usuario);
          return null;
        },
        ERRO_REGISTRAR_TENTATIVA_LOGIN.getChave());
  }

  /**
   * Reseta o número de tentativas falhas do usuário e remove bloqueios temporários.
   *
   * @param usuario Usuário a ser atualizado
   */
  public void resetarTentativas(Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          usuario.setTentativasFalhas(0);
          usuario.setBloqueadoAte(null);
          usuarioRepository.save(usuario);
          return null;
        },
        ERRO_RESETAR_TENTATIVAS_LOGIN.getChave());
  }
}
