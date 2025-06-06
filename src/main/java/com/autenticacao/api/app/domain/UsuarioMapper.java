package com.autenticacao.api.app.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.entity.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

  Usuario toEntity(CadastroUsuarioRequest request);

  @Mapping(
      target = "nomeCompleto",
      expression = "java(usuario.getNome() + \" \" + usuario.getSobrenome())")
  UsuarioResumoResponse toResumo(Usuario usuario);

  UsuarioDetalhadoResponse toDetalhado(Usuario usuario);

  void atualizarUsuarioFromRequest(AtualizarUsuarioRequest request, @MappingTarget Usuario usuario);

  Usuario cadastroUsuarioRequestToEntity(CadastroUsuarioRequest request);
}
