package com.autenticacao.api.unitarios.service;

import static com.autenticacao.api.app.util.enums.MensagemSistema.CONTA_BLOQUEADA;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.impl.TentativaLoginServiceImpl;
import com.autenticacao.api.app.exception.ContaBloqueadaException;

@ExtendWith(MockitoExtension.class)
class TentativaLoginServiceImplTest {

  @InjectMocks private TentativaLoginServiceImpl tentativaLoginService;

  @Mock private UsuarioRepository usuarioRepository;

  private Usuario usuario;

  @BeforeEach
  void setup() {
    usuario = new Usuario();
    usuario.setTentativasFalhas(0);
    usuario.setBloqueadoAte(null);
  }

  @Test
  @DisplayName("Deve lançar exceção quando usuário estiver bloqueado")
  void deveLancarExcecaoQuandoUsuarioBloqueado() {
    // Configura usuário bloqueado até 1 hora no futuro
    usuario.setBloqueadoAte(LocalDateTime.now().plusHours(1));

    // Verifica que exceção é lançada ao validar bloqueio
    assertThatThrownBy(() -> tentativaLoginService.validarBloqueio(usuario))
        .isInstanceOf(ContaBloqueadaException.class)
        .hasMessageContaining(CONTA_BLOQUEADA.getChave());

    // Verifica que não houve chamada para salvar no repositório
    verifyNoInteractions(usuarioRepository);
  }

  @Test
  @DisplayName("Não deve lançar exceção quando usuário não estiver bloqueado")
  void naoDeveLancarExcecaoQuandoUsuarioNaoBloqueado() {
    // Usuário com bloqueio expirado
    usuario.setBloqueadoAte(LocalDateTime.now().minusMinutes(1));

    // Deve executar sem exceção
    tentativaLoginService.validarBloqueio(usuario);

    // Usuário sem bloqueio (null)
    usuario.setBloqueadoAte(null);
    tentativaLoginService.validarBloqueio(usuario);

    verifyNoInteractions(usuarioRepository);
  }

  @Test
  @DisplayName("Deve registrar tentativa falha e aumentar contador")
  void deveRegistrarTentativaFalhaEAumentarContador() {
    usuario.setTentativasFalhas(2);

    tentativaLoginService.registrarFalha(usuario);

    // Verifica que contador de tentativas aumentou em 1
    assert (usuario.getTentativasFalhas() == 3);
    // Verifica que bloqueio não foi setado pois não atingiu o limite
    assert (usuario.getBloqueadoAte() == null);

    verify(usuarioRepository).save(usuario);
  }

  @Test
  @DisplayName("Deve bloquear usuário após atingir limite de tentativas falhas")
  void deveBloquearUsuarioAposLimiteTentativas() {
    usuario.setTentativasFalhas(4); // Já tem 4 tentativas

    tentativaLoginService.registrarFalha(usuario);

    // Deve aumentar para 5 e bloquear por 15 minutos
    assert (usuario.getTentativasFalhas() == 5);
    assert (usuario.getBloqueadoAte() != null);
    assert (usuario.getBloqueadoAte().isAfter(LocalDateTime.now()));

    verify(usuarioRepository).save(usuario);
  }

  @Test
  @DisplayName("Deve resetar tentativas e remover bloqueio")
  void deveResetarTentativasERemoverBloqueio() {
    usuario.setTentativasFalhas(3);
    usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));

    tentativaLoginService.resetarTentativas(usuario);

    // Verifica se tentativas zeraram e bloqueio removido
    assert (usuario.getTentativasFalhas() == 0);
    assert (usuario.getBloqueadoAte() == null);

    verify(usuarioRepository).save(usuario);
  }
}
