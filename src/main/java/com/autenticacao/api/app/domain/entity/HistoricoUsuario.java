package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.ColunasAutenticacao.DATA_HORA_EXCLUSAO;
import static com.autenticacao.api.app.Constantes.ColunasAutenticacao.USUARIO_ID;
import static com.autenticacao.api.app.Constantes.ColunasHistoricoUsuario.*;
import static com.autenticacao.api.app.Constantes.ColunasUsuario.DATA_HORA_ATUALIZACAO;
import static com.autenticacao.api.app.Constantes.ColunasUsuario.DATA_HORA_CRIACAO;
import static com.autenticacao.api.app.Constantes.Schema.AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.Tabelas.HISTORICO_USUARIO;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.autenticacao.api.app.util.enums.TipoMovimentacao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = HISTORICO_USUARIO, schema = AUTENTICACAO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class HistoricoUsuario {

  @Id
  @GeneratedValue(generator = "UUID")
  @UuidGenerator(style = UuidGenerator.Style.AUTO)
  @Column(updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = USUARIO_ID, nullable = false)
  private Usuario usuario;

  @Column(name = DATA_HORA_CRIACAO, updatable = false)
  private LocalDateTime dataHoraCriacao;

  @Column(name = DATA_HORA_ATUALIZACAO)
  private LocalDateTime dataHoraAtualizacao;

  @Column(name = DATA_HORA_EXCLUSAO)
  private LocalDateTime dataHoraExclusao;

  @Column(name = TIPO_MOVIMENTACAO, nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private TipoMovimentacao tipoAlteracao;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = USUARIO_RESPONSAVEL)
  private Usuario usuarioResponsavel;

  @Column(name = CAMPOS_ALTERADOS, columnDefinition = "TEXT")
  private String camposAlterados;
}
