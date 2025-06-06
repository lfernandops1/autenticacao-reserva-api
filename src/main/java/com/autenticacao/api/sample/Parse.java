package com.autenticacao.api.sample;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.stream.Collectors;

import com.autenticacao.api.exception.AutenticacaoApiRunTimeException;

public interface Parse<RequestDTO, Entity, ResponseDTO> {

  default ResponseDTO toResponse(Entity entity) {
    throw new AutenticacaoApiRunTimeException("Necessita programação!");
  }

  default List<Entity> toEntityList(List<RequestDTO> requestDTOS) {
    if (isNull(requestDTOS) || requestDTOS.isEmpty()) return null;
    return requestDTOS.stream().map(this::toEntity).collect(Collectors.toList());
  }

  default List<ResponseDTO> toResponseList(List<Entity> entities) {
    if (isNull(entities) || entities.isEmpty()) return null;
    return entities.stream().map(this::toResponse).collect(Collectors.toList());
  }

  default Entity toEntity(RequestDTO requestDTO) {
    throw new AutenticacaoApiRunTimeException("Necessita programação!");
  }
}
