package com.autenticacao.api.unitarios.service;

import static com.autenticacao.api.app.util.enums.MensagemSistema.EMAIL_JA_CADASTRADO;
import static com.autenticacao.api.app.util.enums.MensagemSistema.TELEFONE_JA_CADASTRADO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioMapper;
import com.autenticacao.api.app.exception.ValidacaoException;
import com.autenticacao.api.app.exception.ValidacaoNotFoundException;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.AutenticacaoCadastroService;
import com.autenticacao.api.app.service.HistoricoUsuarioService;
import com.autenticacao.api.app.util.ValidatorUsuarioUtil;
import com.autenticacao.api.app.util.enums.TipoMovimentacao;
import com.autenticacao.api.app.util.enums.UserRole;
import com.autenticacao.api.app.service.impl.UsuarioServiceImpl;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

  @InjectMocks private UsuarioServiceImpl usuarioService;

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private AutenticacaoCadastroService autenticacaoCadastroService;
  @Mock private UsuarioMapper usuarioMapper;
  @Mock private HistoricoUsuarioService historicoUsuarioService;
  @Mock private ValidatorUsuarioUtil usuarioValidator;

  private UUID usuarioId;
  private Usuario usuario;
  private AtualizarUsuarioRequest atualizarRequest;
  private CadastroUsuarioRequest cadastroRequest;
  private UsuarioDetalhadoResponse detalhadoResponse;
  private UsuarioResumoResponse resumoResponse;

  @BeforeEach
  void setup() {
    usuarioId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    usuario = obterUsuario();
    atualizarRequest = obterAtualizarUsuarioRequest();
    cadastroRequest = obterCadastroUsuarioRequest();
    detalhadoResponse = obterUsuarioDetalhadoResponse();
    resumoResponse = obterUsuarioResumoResponse();
  }

  @Test
  @DisplayName("Deve buscar usuário por ID com sucesso")
  void deveBuscarUsuarioPorIdComSucesso() {
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    when(usuarioMapper.toDetalhado(usuario)).thenReturn(detalhadoResponse);

    UsuarioDetalhadoResponse response = usuarioService.buscarPorId(usuarioId);

    assertThat(response).isEqualTo(detalhadoResponse);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar usuário inexistente por ID")
  void deveLancarExcecaoAoBuscarUsuarioInexistentePorId() {
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

    assertThrows(ValidacaoNotFoundException.class, () -> usuarioService.buscarPorId(usuarioId));
  }

  @Test
  @DisplayName("Deve criar usuário com sucesso sem registrar histórico")
  void deveCriarUsuarioComSucessoSemRegistrarHistorico() {
    when(usuarioRepository.findByEmail(cadastroRequest.email())).thenReturn(Optional.empty());
    when(usuarioRepository.findByTelefone(cadastroRequest.telefone())).thenReturn(Optional.empty());
    when(usuarioMapper.toEntity(cadastroRequest)).thenReturn(usuario);
    when(usuarioRepository.save(usuario)).thenReturn(usuario);
    when(usuarioMapper.toResumo(usuario)).thenReturn(resumoResponse);

    var response = usuarioService.criarUsuario(cadastroRequest);

    assertThat(response).isEqualTo(resumoResponse);
    verify(autenticacaoCadastroService).criar(cadastroRequest, usuario);
    verify(historicoUsuarioService).registrarHistoricoCompleto(null, usuario);
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar criar usuário com email duplicado")
  void deveLancarExcecaoAoCriarUsuarioComEmailDuplicado() {
    when(usuarioRepository.findByEmail(cadastroRequest.email())).thenReturn(Optional.of(usuario));

    ValidacaoException ex =
            assertThrows(ValidacaoException.class, () -> usuarioService.criarUsuario(cadastroRequest));
    assertThat(ex.getMessage()).contains(EMAIL_JA_CADASTRADO.getChave());
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar criar usuário com telefone duplicado")
  void deveLancarExcecaoAoCriarUsuarioComTelefoneDuplicado() {
    when(usuarioRepository.findByEmail(cadastroRequest.email())).thenReturn(Optional.empty());
    when(usuarioRepository.findByTelefone(cadastroRequest.telefone())).thenReturn(Optional.of(usuario));

    ValidacaoException ex =
            assertThrows(ValidacaoException.class, () -> usuarioService.criarUsuario(cadastroRequest));
    assertThat(ex.getMessage()).contains(TELEFONE_JA_CADASTRADO.getChave());
  }

  @Test
  @DisplayName("Deve atualizar usuário e registrar histórico de alteração")
  void deveAtualizarUsuarioERegistrarHistorico() {
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    doNothing().when(usuarioValidator).validarFormatoEmailETelefone(atualizarRequest);

    Usuario usuarioAtualizado =
            usuario.toBuilder()
                    .nome(atualizarRequest.nome())
                    .sobrenome(atualizarRequest.sobrenome())
                    .email(atualizarRequest.email())
                    .telefone(atualizarRequest.telefone())
                    .dataNascimento(atualizarRequest.dataNascimento())
                    .ativo(atualizarRequest.ativo())
                    .build();

    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAtualizado);

    UsuarioDetalhadoResponse esperado =
            new UsuarioDetalhadoResponse(
                    usuarioId,
                    atualizarRequest.nome(),
                    atualizarRequest.sobrenome(),
                    atualizarRequest.email(),
                    atualizarRequest.telefone(),
                    UserRole.USER,
                    atualizarRequest.ativo());

    when(usuarioMapper.toDetalhado(usuarioAtualizado)).thenReturn(esperado);

    UsuarioDetalhadoResponse response =
            usuarioService.atualizarUsuario(usuarioId, atualizarRequest);

    assertThat(response).isEqualTo(esperado);

    verify(historicoUsuarioService)
            .registrarHistoricoCompleto(any(Usuario.class), any(Usuario.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
  void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

    assertThrows(
            ValidacaoNotFoundException.class,
            () -> usuarioService.atualizarUsuario(usuarioId, atualizarRequest));
  }

  @Test
  @DisplayName("Deve listar todos os usuários")
  void deveListarTodosUsuarios() {
    List<Usuario> usuarios = List.of(usuario);
    List<UsuarioResumoResponse> resumos = List.of(resumoResponse);

    when(usuarioRepository.findAll()).thenReturn(usuarios);
    when(usuarioMapper.toResumo(usuario)).thenReturn(resumoResponse);

    List<UsuarioResumoResponse> resultado = usuarioService.listarTodos();

    assertThat(resultado).isEqualTo(resumos);
  }

  // Auxiliares

  private Usuario obterUsuario() {
    return Usuario.builder()
            .id(usuarioId)
            .nome("João")
            .sobrenome("Silva")
            .email("joao.silva@email.com")
            .telefone("123456789")
            .ativo(true)
            .dataNascimento(LocalDate.of(1990, 1, 1))
            .build();
  }

  private AtualizarUsuarioRequest obterAtualizarUsuarioRequest() {
    return new AtualizarUsuarioRequest(
            "João Atualizado",
            "Silva Atualizado",
            "987654321",
            "joao.atualizado@email.com",
            LocalDate.of(1991, 2, 2),
            LocalDateTime.now(),
            true, "SenhaForte123@");
  }

  private CadastroUsuarioRequest obterCadastroUsuarioRequest() {
    return new CadastroUsuarioRequest(
            "Maria",
            "Fernandes",
            "maria@email.com",
            "senhaSegura123",
            "111222333",
            LocalDate.of(1995, 5, 5),
            true,
            UserRole.USER);
  }

  private UsuarioDetalhadoResponse obterUsuarioDetalhadoResponse() {
    return new UsuarioDetalhadoResponse(
            usuarioId, "João", "Silva", "joao.silva@email.com", "123456789", UserRole.USER, true);
  }

  private UsuarioResumoResponse obterUsuarioResumoResponse() {
    return new UsuarioResumoResponse(
            usuarioId, "Maria Fernandes", "maria@email.com", "111222333", true);
  }
}
