package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.ColunasAutenticacao.USUARIO_ID;
import static com.autenticacao.api.app.Constantes.Schema.AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.Tabelas.CONTROLE_ACESSO_USUARIO;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = CONTROLE_ACESSO_USUARIO, schema = AUTENTICACAO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ControleAcessoUsuario {

  @Id
  @GeneratedValue(generator = "UUID")
  @UuidGenerator(style = UuidGenerator.Style.AUTO)
  @Column(updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @OneToOne
  @JoinColumn(name = USUARIO_ID, nullable = false)
  @JsonBackReference
  private Usuario usuario;

  private int tentativasFalhas;
  private LocalDateTime bloqueadoAte;
}
