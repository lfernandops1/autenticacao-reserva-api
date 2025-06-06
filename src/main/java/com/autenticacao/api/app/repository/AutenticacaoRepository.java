package com.autenticacao.api.app.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;

@Repository
public interface AutenticacaoRepository extends JpaRepository<Autenticacao, UUID> {

  Optional<Autenticacao> findByUsuario(Usuario usuario);

  @Query("SELECT a FROM Autenticacao a WHERE a.usuario.id = :usuarioId")
  Optional<Autenticacao> buscarPorUsuarioId(@Param("usuarioId") UUID usuarioId);

  Optional<Autenticacao> findByEmail(String email);
}
