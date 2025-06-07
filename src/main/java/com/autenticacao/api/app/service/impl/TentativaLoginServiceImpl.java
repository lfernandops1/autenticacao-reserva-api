package com.autenticacao.api.app.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.exception.ContaBloqueadaException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TentativaLoginServiceImpl {

  private static final int MAX_TENTATIVAS = 5;
  private static final int TEMPO_BLOQUEIO_MIN = 15;

  private final UsuarioRepository usuarioRepository;

  public void validarBloqueio(Usuario usuario) {
    if (usuario.getBloqueadoAte() != null
        && usuario.getBloqueadoAte().isAfter(LocalDateTime.now())) {
      throw new ContaBloqueadaException("Conta bloqueada até " + usuario.getBloqueadoAte());
    }
  }

  public void registrarFalha(Usuario usuario) {
    int tentativas = usuario.getTentativasFalhas() + 1;
    usuario.setTentativasFalhas(tentativas);

    if (tentativas >= MAX_TENTATIVAS) {
      usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(TEMPO_BLOQUEIO_MIN));
    }
    usuarioRepository.save(usuario);
  }

  public void resetarTentativas(Usuario usuario) {
    usuario.setTentativasFalhas(0);
    usuario.setBloqueadoAte(null);
    usuarioRepository.save(usuario);
  }
}
