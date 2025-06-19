package com.autenticacao.api.app.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.entity.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "autenticacao", ignore = true)
  Usuario toEntity(CadastroUsuarioRequest request);

  @Mapping(
      target = "nomeCompleto",
      expression = "java(usuario.getNome() + \" \" + usuario.getSobrenome())")
  UsuarioResumoResponse toResumo(Usuario usuario);

  UsuarioDetalhadoResponse toDetalhado(Usuario usuario);
}
