package com.autenticacao.api.app.service;

import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequest;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;

public interface AutenticacaoService {
  LoginResponseDTO login(LoginUsuarioRequestDTO dto);

  void alterarSenha(AlterarSenhaRequest requestDTO);
}
