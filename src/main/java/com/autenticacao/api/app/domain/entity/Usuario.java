package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.SCHEMA.SCHEMA_AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.TABELAS.TABELA_USUARIOS;
import static com.autenticacao.api.app.Constantes.TABELA_AUTENTICACAO.DATA_HORA_EXCLUSAO;
import static com.autenticacao.api.app.Constantes.TABELA_AUTENTICACAO.USUARIO;
import static com.autenticacao.api.app.Constantes.TABELA_USUARIO.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.autenticacao.api.util.enums.UserRole;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Table(name = TABELA_USUARIOS, schema = SCHEMA_AUTENTICACAO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

  @Id
  @GeneratedValue(generator = "UUID")
  @UuidGenerator(style = UuidGenerator.Style.AUTO)
  private UUID id;

  @Column(name = NOME, nullable = false)
  private String nome;

  @Column(name = SOBRENOME, nullable = false)
  private String sobrenome;

  @Email
  @Column(unique = true, nullable = false, length = 50, name = EMAIL)
  private String email;

  @Column(name = TELEFONE, nullable = false, unique = true)
  private String telefone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(name = ATIVO)
  private boolean ativo;

  @OneToOne(mappedBy = USUARIO, cascade = CascadeType.ALL)
  private Autenticacao autenticacao;

  @Column(name = DATA_HORA_CRIACAO, nullable = false, updatable = false)
  private LocalDateTime dataHoraCriacao;

  @Column(name = DATA_HORA_ATUALIZACAO)
  private LocalDateTime dataHoraAtualizacao;

  @Column(name = DATA_HORA_EXCLUSAO)
  private LocalDateTime dataHoraExclusao;

  @Column(name = DATA_NASCIMENTO)
  private LocalDate dataNascimento;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return autenticacao != null ? autenticacao.getSenha() : "";
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return UserDetails.super.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return UserDetails.super.isEnabled();
  }
}
