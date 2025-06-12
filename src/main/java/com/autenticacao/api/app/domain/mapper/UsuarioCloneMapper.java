package com.autenticacao.api.app.domain.mapper;

import org.mapstruct.Mapper;

import com.autenticacao.api.app.domain.entity.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioCloneMapper {
  Usuario copy(Usuario source);
}
