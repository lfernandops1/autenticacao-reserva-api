package com.autenticacao.api.app.domain.DTO.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AtualizarUsuarioRequest(
    String nome,
    String sobrenome,
    String telefone,
    String email,
    LocalDate dataNascimento,
    LocalDateTime dataHoraAtualizacao,
    LocalDateTime senhaAtualizadaEm) {

  public AtualizarUsuarioRequest {

    dataHoraAtualizacao = dataHoraAtualizacao != null ? dataHoraAtualizacao : LocalDateTime.now();
    senhaAtualizadaEm = senhaAtualizadaEm != null ? senhaAtualizadaEm : LocalDateTime.now();
  }

  public LocalDateTime dataHoraAtualizacao() {
    return LocalDateTime.now();
  }

  public LocalDateTime senhaAtualizadaEm() {
    return LocalDateTime.now();
  }
}
