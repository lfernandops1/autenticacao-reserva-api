package com.autenticacao.api.app.service.impl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.HistoricoUsuario;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioCloneMapper;
import com.autenticacao.api.app.repository.HistoricoUsuarioRepository;
import com.autenticacao.api.app.service.HistoricoUsuarioService;
import com.autenticacao.api.util.enums.TipoMovimentacao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoUsuarioServiceImpl implements HistoricoUsuarioService {

  private final HistoricoUsuarioRepository historicoRepository;
  private final UsuarioCloneMapper cloneMapper;

  @Override
  public void registrarAlteracaoUsuario(
      Usuario usuarioAntesModificacao, Usuario responsavel, TipoMovimentacao tipo) {
    Usuario original = cloneMapper.copy(usuarioAntesModificacao);

    String camposAlterados = extrairDiferencas(original, usuarioAntesModificacao);

    HistoricoUsuario historico = new HistoricoUsuario();
    historico.setUsuario(usuarioAntesModificacao);
    historico.setUsuarioResponsavel(responsavel);
    historico.setTipoAlteracao(tipo);
    historico.setDataAlteracao(LocalDateTime.now());
    historico.setCamposAlterados(camposAlterados);

    historicoRepository.save(historico);
  }

  private String extrairDiferencas(Usuario original, Usuario modificado) {
    StringBuilder diff = new StringBuilder();

    for (Field field : Usuario.class.getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object antes = field.get(original);
        Object depois = field.get(modificado);

        if (!Objects.equals(antes, depois)) {
          diff.append(
              String.format("Campo '%s': de '%s' para '%s'%n", field.getName(), antes, depois));
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Erro ao comparar campos", e);
      }
    }

    return diff.toString().trim();
  }
}
