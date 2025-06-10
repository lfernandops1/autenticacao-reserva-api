package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.ROTAS.USUARIO_ID;
import static com.autenticacao.api.app.Constantes.TABELAS.TABELA_HISTORICO_USUARIO;
import static com.autenticacao.api.app.Constantes.TABELA_AUTENTICACAO.DATA_HORA_ATUALIZACAO;
import static com.autenticacao.api.app.Constantes.TABELA_HISTORICO_USUARIO.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.autenticacao.api.util.enums.TipoMovimentacao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = TABELA_HISTORICO_USUARIO)
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

  @Column(name = DATA_HORA_ATUALIZACAO, nullable = false)
  private LocalDateTime dataAlteracao;

  @Column(name = TIPO_MOVIMENTACAO, nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private TipoMovimentacao tipoAlteracao;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = USUARIO_RESPONSAVEL)
  private Usuario usuarioResponsavel;

  @Column(name = CAMPOS_ALTERADOS, columnDefinition = "TEXT")
  private String camposAlterados;
}
