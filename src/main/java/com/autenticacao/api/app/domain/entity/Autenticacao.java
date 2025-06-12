package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.ColunasAutenticacao.*;
import static com.autenticacao.api.app.Constantes.Schema.AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.Tabelas.AUTENTICACOES;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
@Builder(toBuilder = true)
@Table(name = AUTENTICACOES, schema = AUTENTICACAO)
@AllArgsConstructor
public class Autenticacao {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToOne
  @JoinColumn(name = USUARIO_ID, nullable = false)
  @JsonBackReference
  private Usuario usuario;

  @Column(name = EMAIL, nullable = false)
  private String email;

  @Getter
  @Column(name = SENHA, nullable = false)
  private String senha;

  @Column(name = DATA_HORA_CRIACAO, nullable = false)
  private LocalDateTime dataHoraCriacao;

  @Column(name = DATA_HORA_ATUALIZACAO, nullable = false)
  private LocalDateTime dataHoraAtualizacao;

  @Column(name = DATA_HORA_EXCLUSAO)
  private LocalDateTime dataHoraExclusao;

  @Column(name = ATIVO, nullable = false)
  private Boolean ativo;
}
