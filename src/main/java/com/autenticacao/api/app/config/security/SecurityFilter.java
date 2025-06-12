package com.autenticacao.api.app.config.security;

import static com.autenticacao.api.app.Constantes.Http.HEADER_BEARER_PREFIX;
import static com.autenticacao.api.app.Constantes.Util.STRING_VAZIA;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

  private final TokenService tokenService;

  private final UsuarioRepository usuarioRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    var token = this.recoverToken(request);

    if (token != null) {
      var email = tokenService.validateToken(token);
      if (email != null && !email.isBlank()) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        if (usuarioOptional.isPresent()) {
          Usuario usuario = usuarioOptional.get();
          var authentication =
              new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private String recoverToken(HttpServletRequest request) {
    var authHeader = request.getHeader(AUTHORIZATION);
    if (authHeader == null) return null;
    return authHeader.replace(HEADER_BEARER_PREFIX, STRING_VAZIA);
  }
}
