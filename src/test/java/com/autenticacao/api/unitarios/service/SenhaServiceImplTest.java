package com.autenticacao.api.unitarios.service;

import static com.autenticacao.api.app.util.enums.MensagemSistema.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.AutenticacaoApiRunTimeException;
import com.autenticacao.api.app.exception.SenhaExpiradaException;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.service.impl.SenhaServiceImpl;

@ExtendWith(MockitoExtension.class)
class SenhaServiceImplTest {

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AutenticacaoRepository autenticacaoRepository;

  @InjectMocks private SenhaServiceImpl service;

  private Usuario usuario;
  private Autenticacao autenticacao;

  private final long validadeSenhaDias = 90L;

  @BeforeEach
  void setup() {
    usuario = new Usuario();
    autenticacao = new Autenticacao();
    usuario.setAutenticacao(autenticacao);
    ReflectionTestUtils.setField(service, "validadeSenhaDias", validadeSenhaDias);

    // mocka o usuário logado no contexto de segurança
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authToken);
    SecurityContextHolder.setContext(context);
  }

  // --- TESTES senhaExpirada ---

  @Test
  @DisplayName("Retorna true quando a senha está expirada (mais antiga que validade)")
  void deveRetornarTrueQuandoSenhaExpirada() {
    autenticacao.setDataHoraAtualizacao(LocalDateTime.now().minusDays(validadeSenhaDias + 1));
    assertTrue(service.senhaExpirada(autenticacao));
  }

  @Test
  @DisplayName("Retorna false quando a senha não está expirada (dentro do prazo)")
  void deveRetornarFalseQuandoSenhaNaoExpirada() {
    autenticacao.setDataHoraAtualizacao(LocalDateTime.now().minusDays(validadeSenhaDias - 1));
    assertFalse(service.senhaExpirada(autenticacao));
  }

  // --- TESTES validarSenhaExpirada ---

  @Test
  @DisplayName("Valida senha sem lançar exceção quando senha não expirada")
  void deveValidarSenhaSemExcecaoQuandoNaoExpirada() {
    autenticacao.setDataHoraAtualizacao(LocalDateTime.now().minusDays(validadeSenhaDias - 1));
    assertDoesNotThrow(() -> service.validarSenhaExpirada(usuario));
  }

  @Test
  @DisplayName("Lança SenhaExpiradaException quando senha está expirada")
  void deveLancarExcecaoSenhaExpirada() {
    autenticacao.setDataHoraAtualizacao(LocalDateTime.now().minusDays(validadeSenhaDias + 1));
    usuario.setAutenticacao(autenticacao);

    SenhaExpiradaException ex =
        assertThrows(SenhaExpiradaException.class, () -> service.validarSenhaExpirada(usuario));
    assertEquals(SENHA_EXPIRADA.getChave(), ex.getMessage());
  }

  @Test
  @DisplayName("Lança AutenticacaoApiRunTimeException quando ocorre erro inesperado na validação")
  void deveLancarExcecaoGenericaSeErroNoMetodo() {
    usuario.setAutenticacao(null); // força NullPointerException no método

    AutenticacaoApiRunTimeException ex =
        assertThrows(
            AutenticacaoApiRunTimeException.class, () -> service.validarSenhaExpirada(usuario));
    assertTrue(ex.getMessage().contains(ERRO_VALIDAR_EXPIRACAO_SENHA.getChave()));
  }

  // --- TESTES alterarSenha ---

  @Test
  @DisplayName("Altera a senha com sucesso, codifica e atualiza data de modificação")
  void deveAlterarSenhaComSucesso() {
    String senhaAntiga = "senhaAntiga123";
    String novaSenha = "novaSenha123";
    String senhaCodificada = "senhaCodificada";

    when(passwordEncoder.encode(novaSenha)).thenReturn(senhaCodificada);
    when(autenticacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> service.alterarSenha(senhaAntiga, novaSenha));

    assertEquals(senhaCodificada, autenticacao.getSenha());
    assertNotNull(autenticacao.getDataHoraAtualizacao());
    verify(autenticacaoRepository).save(autenticacao);
  }

  @Test
  @DisplayName("Lança AutenticacaoApiRunTimeException quando erro ocorre ao salvar senha")
  void deveLancarExcecaoGenericaSeErroNoAlterarSenha() {
    String senhaAntiga = "senhaAntiga123";
    String novaSenha = "novaSenha123";

    when(passwordEncoder.encode(novaSenha)).thenReturn("codificada");
    doThrow(new RuntimeException("Erro banco")).when(autenticacaoRepository).save(any());

    AutenticacaoApiRunTimeException ex =
        assertThrows(
            AutenticacaoApiRunTimeException.class,
            () -> service.alterarSenha(senhaAntiga, novaSenha));
    assertTrue(ex.getMessage().contains(ERRO_ALTERAR_SENHA.getChave()));
  }
}
