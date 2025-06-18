package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.ColunasAutenticacao.*;
import static com.autenticacao.api.app.Constantes.ColunasHistoricoUsuario.*;
import static com.autenticacao.api.app.Constantes.ColunasUsuario.DATA_HORA_ATUALIZACAO;
import static com.autenticacao.api.app.Constantes.Schema.AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.Tabelas.HISTORICO_AUTENTICACAO;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.autenticacao.api.app.util.enums.TipoMovimentacao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = HISTORICO_AUTENTICACAO, schema = AUTENTICACAO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class HistoricoAutenticacao {
  @Id
  @GeneratedValue(generator = "UUID")
  @UuidGenerator(style = UuidGenerator.Style.AUTO)
  @Column(updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "autenticacao_id", nullable = false)
  private Autenticacao autenticacao;

  @Column(name = DATA_HORA_ATUALIZACAO, nullable = false)
  private LocalDateTime dataAlteracao;

  @Column(name = TIPO_MOVIMENTACAO, nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private TipoMovimentacao tipoAlteracao;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = USUARIO_RESPONSAVEL)
  private Usuario usuarioResponsavel;

  @Column(name = DATA_HORA_CRIACAO, updatable = false)
  private LocalDateTime dataHoraCriacao;

  @Column(name = SENHA_ATUALIZACAO)
  private LocalDateTime senhaAtualizacao;

  @Column(name = DATA_HORA_EXCLUSAO, updatable = false)
  private LocalDateTime dataHoraExclusao;

  @Column(name = DATA_HORA_ALTERACAO_SENHA)
  private LocalDateTime dataHoraAlteracaoSenha;

  @Column(name = CAMPOS_ALTERADOS, columnDefinition = "TEXT")
  private String camposAlterados;
}
