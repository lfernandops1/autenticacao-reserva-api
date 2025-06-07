package com.autenticacao.api.app.domain.DTO.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.autenticacao.api.util.enums.UserRole;

import jakarta.validation.constraints.*;
import lombok.Getter;

public record CadastroUsuarioRequest(
    @NotBlank(message = "Nome é obrigatório") String nome,
    @NotBlank(message = "Sobrenome é obrigatório") String sobrenome,
    @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
    @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message =
                "Senha deve conter ao menos uma letra maiúscula, uma minúscula, um número e um caractere especial")
        String senha,
    @NotBlank(message = "Telefone é obrigatório") String telefone,
    @NotNull(message = "Data de nascimento não pode ser nula") LocalDate dataNascimento,
    @Getter Boolean ativo,
    UserRole role,
    LocalDateTime dataHoraCriacao,
    LocalDateTime senhaAtualizadaEm) {

  public CadastroUsuarioRequest {

    ativo = ativo != null ? ativo : true;

    role = role != null ? role : UserRole.USER;

    dataHoraCriacao = dataHoraCriacao != null ? dataHoraCriacao : LocalDateTime.now();
    senhaAtualizadaEm = senhaAtualizadaEm != null ? senhaAtualizadaEm : LocalDateTime.now();
  }

  public Boolean ativo() {
    return true;
  }

  public UserRole role() {
    return UserRole.USER;
  }

  public LocalDateTime dataHoraCriacao() {
    return LocalDateTime.now();
  }

  public LocalDateTime senhaAtualizadaEm() {
    return LocalDateTime.now();
  }
}
