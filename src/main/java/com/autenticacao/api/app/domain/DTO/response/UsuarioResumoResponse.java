package com.autenticacao.api.app.domain.DTO.response;

import java.util.UUID;

public record UsuarioResumoResponse(
    UUID id, String nomeCompleto, String email, String telefone, boolean ativo) {}
