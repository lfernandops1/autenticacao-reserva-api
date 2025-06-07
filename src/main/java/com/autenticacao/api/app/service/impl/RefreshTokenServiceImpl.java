package com.autenticacao.api.app.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.RefreshToken;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.RefreshTokenRepository;
import com.autenticacao.api.app.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {
  private static final long EXPIRATION_MINUTES = 60 * 24 * 7; // 7 dias

  private final RefreshTokenRepository refreshTokenRepository;
  private final UsuarioRepository usuarioRepository;

  public String createRefreshToken(Usuario usuario) {
    String token = UUID.randomUUID().toString();

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setUsuario(usuario);
    refreshToken.setExpiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));

    refreshTokenRepository.save(refreshToken);

    return token;
  }

  public boolean isValid(String token) {
    return refreshTokenRepository
        .findByToken(token)
        .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()))
        .isPresent();
  }

  public Usuario getUsuario(String token) {
    return refreshTokenRepository
        .findByToken(token)
        .map(RefreshToken::getUsuario)
        .orElseThrow(() -> new RuntimeException("Refresh token inválido"));
  }

  public void deleteByUsuario(Usuario usuario) {
    refreshTokenRepository.deleteByUsuario(usuario);
  }
}
