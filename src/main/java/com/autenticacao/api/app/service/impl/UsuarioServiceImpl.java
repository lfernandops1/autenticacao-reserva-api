package com.autenticacao.api.app.service.impl;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.UsuarioMapper;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.UsuarioService;
import com.autenticacao.api.exception.ValidacaoException;
import com.autenticacao.api.exception.ValidacaoNotFoundException;
import com.autenticacao.api.util.ValidatorUsuarioUtil;
import com.autenticacao.api.util.enums.EValidacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.autenticacao.api.app.Constantes.Mensagens.ERRO_AO_TENTAR_CRIAR_USUARIO;
import static com.autenticacao.api.app.Constantes.Util.AGORA;
import static com.autenticacao.api.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;


@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AutenticacaoServiceImpl autenticacaoServiceImpl;
    private final UsuarioMapper usuarioMapper;
    private final ValidatorUsuarioUtil validatorUsuarioUtil;

    @Override
    public UsuarioDetalhadoResponse buscarPorId(UUID id) {
        Usuario usuario = buscarUsuarioPorId(id);
        return usuarioMapper.toDetalhado(usuario);
    }

    @Override
    public UsuarioResumoResponse prepararParaCriarUsuario(CadastroUsuarioRequest request) {
        validarNovoUsuario(request);

        Usuario usuario = salvarUsuario(usuarioMapper.toEntity(request));
        autenticacaoServiceImpl.criarAutenticacao(request, usuario);

        return usuarioMapper.toResumo(usuario);
    }

    @Override
    public UsuarioDetalhadoResponse atualizarUsuario(UUID id, AtualizarUsuarioRequest request) {
        this.validatorUsuarioUtil.validarFormatoEmailETelefone(request);

        Usuario usuarioExistente = buscarUsuarioPorId(id);

        aplicarAtualizacoesParciais(request, usuarioExistente);

        Usuario usuarioAtualizado = salvarUsuario(usuarioExistente);

        return usuarioMapper.toDetalhado(usuarioAtualizado);
    }

    @Override
    public void desativarUsuario(UUID id) {
        Usuario usuario = buscarUsuarioPorId(id);

        usuario.setAtivo(false);
        usuario.setDataHoraExclusao(AGORA);
        salvarUsuario(usuario);

        autenticacaoServiceImpl.desativarAutenticacao(usuario.getId());
    }

    @Override
    public List<UsuarioResumoResponse> listarTodos() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResumo).toList();
    }

    // ==============================
    // Métodos Auxiliares Privados
    // ==============================

    private Usuario buscarUsuarioPorId(UUID id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new ValidacaoNotFoundException(EValidacao.USUARIO_NAO_ENCONTRADO_POR_ID, id.toString()));
    }

    private Usuario salvarUsuario(Usuario usuario) {
        return executarComandoComTratamentoErroComMensagem(() -> usuarioRepository.save(usuario), ERRO_AO_TENTAR_CRIAR_USUARIO //
        );
    }

    private void validarNovoUsuario(CadastroUsuarioRequest request) {
        this.validatorUsuarioUtil.validarFormatoEmailETelefone(request);

        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ValidacaoException(EValidacao.EMAIL_JA_CADASTRADO, request.email());
        }

        if (usuarioRepository.findByTelefone(request.telefone()).isPresent()) {
            throw new ValidacaoException(EValidacao.TELEFONE_JA_CADASTRADO, request.telefone());
        }
    }

    private void aplicarAtualizacoesParciais(AtualizarUsuarioRequest request, Usuario usuario) {
        if (request.nome() != null && !request.nome().isBlank()) {
            usuario.setNome(request.nome());
        }

        if (request.sobrenome() != null && !request.sobrenome().isBlank()) {
            usuario.setSobrenome(request.sobrenome());
        }

        if (request.telefone() != null && !request.telefone().isBlank()) {
            usuario.setTelefone(request.telefone());
        }

        if (request.email() != null && !request.email().isBlank()) {
            usuario.setEmail(request.email());
        }

        if (request.dataNascimento() != null) {
            usuario.setDataNascimento(request.dataNascimento());
        }
    }
}
