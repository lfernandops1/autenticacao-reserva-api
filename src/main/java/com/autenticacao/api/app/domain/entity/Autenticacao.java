package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.SCHEMA.SCHEMA_AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.TABELAS.TABELA_AUTENTICACOES;
import static com.autenticacao.api.app.Constantes.TABELA_AUTENTICACAO.*;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
@Table(name = TABELA_AUTENTICACOES, schema = SCHEMA_AUTENTICACAO)
public class Autenticacao {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToOne
  @JoinColumn(name = USUARIO_ID, nullable = false)
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
