package com.autenticacao.api.integracao.controller;

import static com.autenticacao.api.app.Constantes.Rotas.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.util.enums.UserRole;
import com.autenticacao.api.config.BaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;

class UsuarioControllerIntegrationTest extends BaseTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private CadastroUsuarioRequest criarCadastroUsuarioRequest() {
    return new CadastroUsuarioRequest(
        "joao",
        "silva",
        "joao.silva@email.com",
        "senhaForte123@",
        "82993834503",
        LocalDate.of(1990, 6, 15),
        true,
        UserRole.USER);
  }

  private CadastroUsuarioRequest criarCadastroUsuarioRequestAdmin() {
    return new CadastroUsuarioRequest(
        "maria",
        "silva",
        "maria.silva@email.com",
        "senhaForte123@",
        "82993834505",
        LocalDate.of(1990, 6, 15),
        true,
        UserRole.ADMIN);
  }

  private AtualizarUsuarioRequest criarAtualizarUsuarioRequest() {
    return new AtualizarUsuarioRequest(
        "João Atualizado",
        "Silva Atualizado",
        "82993834504",
        "joao.silvaatualizado@email.com",
        LocalDate.of(1990, 6, 15),
        LocalDateTime.now(),
        false,
        "SenhaForte123@");
  }

  @Test
  @DisplayName("criarUsuario deve retornar 201 com usuario criado")
  void criarUsuarioDeveRetornar201() throws Exception {
    var request = criarCadastroUsuarioRequest();

    mockMvc
        .perform(
            post(API_USUARIOS + CRIAR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(request.email()))
        .andExpect(
            jsonPath("$.nomeCompleto")
                .value(request.nome() + " " + request.sobrenome())); // espaço aqui!
  }

  @Test
  @DisplayName("criarUsuarioAdmin deve retornar 201 com usuario admin criado")
  void criarUsuarioAdminDeveRetornar201() throws Exception {
    var request = criarCadastroUsuarioRequestAdmin();

    mockMvc
        .perform(
            post(API_USUARIOS + CRIAR_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(request.email()));
  }
}
