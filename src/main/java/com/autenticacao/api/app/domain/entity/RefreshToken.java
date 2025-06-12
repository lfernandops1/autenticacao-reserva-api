package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.ColunasAutenticacao.USUARIO;
import static com.autenticacao.api.app.Constantes.Schema.AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.Tabelas.REFRESH_TOKEN;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = REFRESH_TOKEN, schema = AUTENTICACAO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = USUARIO, nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private LocalDateTime expiryDate;
}
