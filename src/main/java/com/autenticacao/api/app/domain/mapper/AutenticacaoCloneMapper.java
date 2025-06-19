package com.autenticacao.api.app.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autenticacao.api.app.domain.entity.Autenticacao;

@Mapper(componentModel = "spring")
public interface AutenticacaoCloneMapper {

  @Mapping(target = "historicoAutenticacoes", ignore = true)
  Autenticacao copy(Autenticacao autenticacao);
}
