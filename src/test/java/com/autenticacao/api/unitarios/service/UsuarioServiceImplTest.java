package com.autenticacao.api.unitarios.service;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioMapper;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.HistoricoUsuarioService;
import com.autenticacao.api.app.service.impl.AutenticacaoServiceImpl;
import com.autenticacao.api.app.service.impl.UsuarioServiceImpl;
import com.autenticacao.api.exception.ValidacaoException;
import com.autenticacao.api.exception.ValidacaoNotFoundException;
import com.autenticacao.api.util.ValidatorUsuarioUtil;
import com.autenticacao.api.util.enums.EValidacao;
import com.autenticacao.api.util.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AutenticacaoServiceImpl autenticacaoServiceImpl;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private HistoricoUsuarioService historicoUsuarioService;

    @Mock
    private ValidatorUsuarioUtil usuarioValidator;

    private final UUID usuarioId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final Usuario usuario = obterUsuario();
    private final AtualizarUsuarioRequest atualizarRequest = obterAtualizarUsuarioRequest();
    private final CadastroUsuarioRequest cadastroRequest = obterCadastroUsuarioRequest();
    private final UsuarioDetalhadoResponse detalhadoResponse = obterUsuarioDetalhadoResponse();
    private final UsuarioResumoResponse resumoResponse = obterUsuarioResumoResponse();

    // ==================== BUSCAR POR ID ====================
    @Nested
    class BuscarPorIdTests {
        @Test
        void deveRetornarDetalhadoQuandoUsuarioExiste() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
            when(usuarioMapper.toDetalhado(usuario)).thenReturn(detalhadoResponse);

            UsuarioDetalhadoResponse response = usuarioService.buscarPorId(usuarioId);

            assertThat(response).isEqualTo(detalhadoResponse);
            verify(usuarioRepository).findById(usuarioId);
        }

        @Test
        void deveLancarNotFoundExceptionQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

            ValidacaoNotFoundException exception =
                    assertThrows(ValidacaoNotFoundException.class, () -> usuarioService.buscarPorId(usuarioId));

            assertThat(exception.getMessage())
                    .isEqualTo(EValidacao.USUARIO_NAO_ENCONTRADO_POR_ID.getMessageKey());
        }
    }

    // ==================== CRIAR USUÁRIO ====================
    @Nested
    class CriarUsuarioTests {
        @Test
        void deveCriarUsuarioComSucesso() {
            when(usuarioRepository.findByEmail(cadastroRequest.email())).thenReturn(Optional.empty());
            when(usuarioRepository.findByTelefone(cadastroRequest.telefone())).thenReturn(Optional.empty());
            when(usuarioMapper.toEntity(cadastroRequest)).thenReturn(usuario);
            when(usuarioRepository.save(usuario)).thenReturn(usuario);
            when(usuarioMapper.toResumo(usuario)).thenReturn(resumoResponse);

            UsuarioResumoResponse response = usuarioService.criarUsuario(cadastroRequest);

            assertThat(response).isEqualTo(resumoResponse);
            verify(autenticacaoServiceImpl).criarAutenticacao(cadastroRequest, usuario);
        }

        @Test
        void deveLancarQuandoEmailJaCadastrado() {
            when(usuarioRepository.findByEmail(cadastroRequest.email())).thenReturn(Optional.of(usuario));

            ValidacaoException exception =
                    assertThrows(ValidacaoException.class, () -> usuarioService.criarUsuario(cadastroRequest));

            assertThat(exception.getMessage()).isEqualTo(EValidacao.EMAIL_JA_CADASTRADO.getMessageKey());
        }

        @Test
        void deveLancarQuandoTelefoneJaCadastrado() {
            when(usuarioRepository.findByEmail(cadastroRequest.email())).thenReturn(Optional.empty());
            when(usuarioRepository.findByTelefone(cadastroRequest.telefone())).thenReturn(Optional.of(usuario));

            ValidacaoException exception =
                    assertThrows(ValidacaoException.class, () -> usuarioService.criarUsuario(cadastroRequest));

            assertThat(exception.getMessage())
                    .isEqualTo(EValidacao.TELEFONE_JA_CADASTRADO.getMessageKey());
        }
    }

    // ==================== ATUALIZAR USUÁRIO ====================
    @Nested
    class AtualizarUsuarioTests {
        @Test
        void deveAtualizarCamposParciaisComSucesso() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
            doNothing().when(usuarioValidator).validarFormatoEmailETelefone(atualizarRequest);

            // Simulamos que o save retorna um novo objeto atualizado (builder)
            Usuario usuarioAtualizado = usuario.toBuilder()
                    .nome(atualizarRequest.nome())
                    .sobrenome(atualizarRequest.sobrenome())
                    .email(atualizarRequest.email())
                    .telefone(atualizarRequest.telefone())
                    .build();

            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAtualizado);

            UsuarioDetalhadoResponse esperado = new UsuarioDetalhadoResponse(
                    usuarioId,
                    atualizarRequest.nome(),
                    atualizarRequest.sobrenome(),
                    atualizarRequest.email(),
                    atualizarRequest.telefone(),
                    UserRole.USER,
                    true);

            when(usuarioMapper.toDetalhado(usuarioAtualizado)).thenReturn(esperado);

            UsuarioDetalhadoResponse response = usuarioService.atualizarUsuario(usuarioId, atualizarRequest);

            assertThat(response).isEqualTo(esperado);

            // Captura o usuário salvo para validar campos alterados
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            Usuario usuarioSalvo = captor.getValue();

            assertThat(usuarioSalvo.getNome()).isEqualTo(atualizarRequest.nome());
            assertThat(usuarioSalvo.getSobrenome()).isEqualTo(atualizarRequest.sobrenome());
            assertThat(usuarioSalvo.getEmail()).isEqualTo(atualizarRequest.email());
            assertThat(usuarioSalvo.getTelefone()).isEqualTo(atualizarRequest.telefone());

            verify(usuarioValidator).validarFormatoEmailETelefone(atualizarRequest);
            verify(usuarioRepository).findById(usuarioId);
            verify(usuarioMapper).toDetalhado(usuarioAtualizado);
        }

        @Test
        void deveLancarQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

            ValidacaoNotFoundException exception =
                    assertThrows(
                            ValidacaoNotFoundException.class,
                            () -> usuarioService.atualizarUsuario(usuarioId, atualizarRequest));

            assertThat(exception.getMessage())
                    .isEqualTo(EValidacao.USUARIO_NAO_ENCONTRADO_POR_ID.getMessageKey());
        }
    }

    // ==================== DESATIVAR USUÁRIO ====================
    @Nested
    class DesativarUsuarioTests {
        @Test
        void deveDesativarComSucesso() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(usuario)).thenReturn(usuario);
            doNothing().when(autenticacaoServiceImpl).desativarAutenticacao(usuarioId);

            usuarioService.desativarUsuario(usuarioId);

            assertThat(usuario.isAtivo()).isFalse();
            assertThat(usuario.getDataHoraExclusao()).isNotNull();
            verify(autenticacaoServiceImpl).desativarAutenticacao(usuarioId);
            verify(usuarioRepository).save(usuario);
        }

        @Test
        void deveLancarQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

            ValidacaoNotFoundException exception =
                    assertThrows(
                            ValidacaoNotFoundException.class,
                            () -> usuarioService.desativarUsuario(usuarioId));

            assertThat(exception.getMessage())
                    .isEqualTo(EValidacao.USUARIO_NAO_ENCONTRADO_POR_ID.getMessageKey());
        }
    }

    // ==================== LISTAR TODOS ====================
    @Nested
    class ListarTodosTests {
        @Test
        void deveRetornarListaDeResumo() {
            List<Usuario> usuarios = List.of(usuario);
            List<UsuarioResumoResponse> resumoList = List.of(resumoResponse);

            when(usuarioRepository.findAll()).thenReturn(usuarios);
            when(usuarioMapper.toResumo(usuario)).thenReturn(resumoResponse);

            List<UsuarioResumoResponse> responseList = usuarioService.listarTodos();

            assertThat(responseList).hasSize(1).containsExactlyElementsOf(resumoList);

            verify(usuarioRepository).findAll();
            verify(usuarioMapper, times(usuarios.size())).toResumo(any());
        }
    }

    // ======= MÉTODOS AUXILIARES PARA CRIAR OBJETOS DE TESTE =======
    private Usuario obterUsuario() {
        return Usuario.builder()
                .id(usuarioId)
                .nome("Maria")
                .sobrenome("Silva")
                .email("maria@gmail.com")
                .telefone("11999999999")
                .dataNascimento(LocalDate.of(1990, 6, 20))
                .role(UserRole.USER)
                .ativo(true)
                .build();
    }

    private AtualizarUsuarioRequest obterAtualizarUsuarioRequest() {
        return new AtualizarUsuarioRequest(
                "NomeAtualizado",
                "SobrenomeAtualizado",
                "988888888",
                "email2@teste.com",
                LocalDate.of(1990, 1, 1),
                LocalDateTime.of(2025, 6, 6, 10, 0, 0),
                LocalDateTime.of(2025, 6, 1, 12, 30, 0));
    }

    private CadastroUsuarioRequest obterCadastroUsuarioRequest() {
        return new CadastroUsuarioRequest(
                "NomeTeste",
                "SobrenomeTeste",
                "email@teste.com",
                "Senha@123",
                "999999999",
                LocalDate.of(1990, 1, 1),
                true,
                UserRole.USER,
                LocalDateTime.of(2025, 6, 6, 10, 0),
                LocalDateTime.of(2025, 6, 1, 12, 30));
    }

    private UsuarioDetalhadoResponse obterUsuarioDetalhadoResponse() {
        return new UsuarioDetalhadoResponse(
                usuarioId,
                "Maria",
                "Silva",
                "maria@gmail.com",
                "11999999999",
                UserRole.USER,
                true
        );
    }

    private UsuarioResumoResponse obterUsuarioResumoResponse() {
        return new UsuarioResumoResponse(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "NomeTeste SobrenomeTeste",
                "email@teste.com",
                "999999999",
                true);
    }
}
