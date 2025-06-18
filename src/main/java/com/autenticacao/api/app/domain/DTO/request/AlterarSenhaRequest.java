package com.autenticacao.api.app.domain.DTO.request;

import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequest(@NotBlank String senhaAtual, @NotBlank String novaSenha) {}
