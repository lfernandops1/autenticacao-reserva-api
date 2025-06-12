package com.autenticacao.api.app.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autenticacao.api.app.domain.entity.HistoricoUsuario;

public interface HistoricoUsuarioRepository extends JpaRepository<HistoricoUsuario, UUID> {}
