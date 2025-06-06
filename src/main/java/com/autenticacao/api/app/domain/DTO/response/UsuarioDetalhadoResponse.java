package com.autenticacao.api.app.domain.DTO.response;

import java.util.UUID;

import com.autenticacao.api.util.enums.UserRole;

public record UsuarioDetalhadoResponse(
    UUID id,
    String nome,
    String sobrenome,
    String email,
    String telefone,
    UserRole role,
    boolean ativo) {}
