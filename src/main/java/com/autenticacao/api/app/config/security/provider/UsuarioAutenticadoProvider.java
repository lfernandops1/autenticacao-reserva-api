package com.autenticacao.api.app.config.security.provider;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.autenticacao.api.app.domain.entity.Usuario;

@Component
public class UsuarioAutenticadoProvider {

  public Optional<Usuario> getUsuarioLogado() {
    return obterUsuarioLogadoOptional();
  }

  public Optional<UUID> getIdUsuarioLogado() {
    return getUsuarioLogado().map(Usuario::getId);
  }

  public static Usuario obterUsuarioLogado() {
    return obterUsuarioLogadoOptional().orElse(null);
  }

  public static Optional<Usuario> obterUsuarioLogadoOptional() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
      return Optional.of(usuario);
    }
    return Optional.empty();
  }

  public static UUID obterIdUsuarioLogado() {
    return obterUsuarioLogadoOptional().map(Usuario::getId).orElse(null);
  }
}
