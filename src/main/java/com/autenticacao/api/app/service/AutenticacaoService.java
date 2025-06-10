package com.autenticacao.api.app.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;

import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;

public interface AutenticacaoService {
  LoginResponseDTO login(LoginUsuarioRequestDTO data, AuthenticationManager authenticationManager);

  void criarAutenticacao(CadastroUsuarioRequest requestDTO, Usuario usuario);

  void alterarSenha(AlterarSenhaRequestDTO requestDTO);

  void desativarAutenticacao(UUID usuarioId);

  boolean senhaExpirada(Autenticacao autenticacao);
}
