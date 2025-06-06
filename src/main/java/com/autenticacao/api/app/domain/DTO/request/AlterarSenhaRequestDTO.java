package com.autenticacao.api.app.domain.DTO.request;

import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequestDTO(@NotBlank String senha) {}
