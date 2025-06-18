package com.autenticacao.api.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.autenticacao.api.app.Constantes.ColunasAutenticacao.*;
import static com.autenticacao.api.app.Constantes.Schema.AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.Tabelas.AUTENTICACOES;

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

    @Column(name = ATIVO, nullable = false)
    private Boolean ativo;

    @OneToMany(mappedBy = "autenticacao", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<HistoricoAutenticacao> historicoAutenticacoes = new ArrayList<>();
}
