package com.autenticacao.api.app.domain.mapper;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutenticacaoCloneMapper {

    @Mapping(target = "historicoAutenticacoes", ignore = true)
    Autenticacao copy(Autenticacao autenticacao);
}
