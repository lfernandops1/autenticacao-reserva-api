package com.autenticacao.api.app.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autenticacao.api.app.domain.entity.HistoricoAutenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;

public interface HistoricoAutenticacaoRepository
    extends JpaRepository<HistoricoAutenticacao, UUID> {

  Optional<HistoricoAutenticacao> findTopByUsuarioResponsavelOrderByDataHoraCriacaoDesc(
      Usuario usuarioResponsavel);
}
