package com.autenticacao.api.app.repository;

import com.autenticacao.api.app.domain.entity.HistoricoAutenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HistoricoAutenticacaoRepository
        extends JpaRepository<HistoricoAutenticacao, UUID> {

    Optional<HistoricoAutenticacao> findTopByUsuarioResponsavelOrderByDataHoraCriacaoDesc(Usuario usuarioResponsavel);

}
