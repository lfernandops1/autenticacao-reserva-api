package com.autenticacao.api.app.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autenticacao.api.app.domain.entity.ControleAcessoUsuario;
import com.autenticacao.api.app.domain.entity.Usuario;

@Repository
public interface ControleAcessoUsuarioRepository
    extends JpaRepository<ControleAcessoUsuario, UUID> {

  Optional<ControleAcessoUsuario> findByUsuario(Usuario usuario);
}
