package com.autenticacao.api.app.domain.DTO.request;

import jakarta.validation.constraints.NotBlank;

public record LoginUsuarioRequestDTO(@NotBlank String email, @NotBlank String senha) {}
