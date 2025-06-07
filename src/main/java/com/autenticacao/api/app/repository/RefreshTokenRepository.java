package com.autenticacao.api.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autenticacao.api.app.domain.entity.RefreshToken;
import com.autenticacao.api.app.domain.entity.Usuario;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  void deleteByUsuario(Usuario usuario);
}
