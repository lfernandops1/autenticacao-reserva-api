package com.autenticacao.api.app.domain.DTO.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record DesativarUsuarioRequest(@NotNull UUID id, boolean ativo) {}
