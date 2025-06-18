package com.autenticacao.api.unitarios.service;

import com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider;
import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.HistoricoAutenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.AutenticacaoApiRunTimeException;
import com.autenticacao.api.app.exception.SenhaExpiradaException;
import com.autenticacao.api.app.exception.ValidacaoException;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.repository.HistoricoAutenticacaoRepository;
import com.autenticacao.api.app.service.impl.SenhaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.autenticacao.api.app.util.enums.MensagemSistema.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SenhaServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AutenticacaoRepository autenticacaoRepository;

    @Mock
    private HistoricoAutenticacaoRepository historicoAutenticacaoRepository;

    @InjectMocks
    private SenhaServiceImpl service;

    private Usuario usuario;
    private Autenticacao autenticacao;

    private final long validadeSenhaDias = 90L;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        autenticacao = new Autenticacao();
        usuario.setAutenticacao(autenticacao);
        ReflectionTestUtils.setField(service, "validadeSenhaDias", validadeSenhaDias);

        // mocka o usuário logado no contexto de segurança (caso necessário)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(usuario, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    // --- TESTES validarSenhaExpirada ---

    @Test
    @DisplayName("Valida senha sem lançar exceção quando senha não expirada")
    void deveValidarSenhaSemExcecaoQuandoNaoExpirada() {
        when(historicoAutenticacaoRepository
                .findTopByUsuarioResponsavelOrderByDataHoraCriacaoDesc(usuario))
                .thenReturn(Optional.of(criarHistoricoComData(LocalDateTime.now().minusDays(validadeSenhaDias - 1))));

        assertDoesNotThrow(() -> service.validarSenhaExpirada(usuario));
    }

    @Test
    @DisplayName("Lança SenhaExpiradaException quando senha está expirada")
    void deveLancarExcecaoSenhaExpirada() {
        when(historicoAutenticacaoRepository
                .findTopByUsuarioResponsavelOrderByDataHoraCriacaoDesc(usuario))
                .thenReturn(Optional.of(criarHistoricoComData(LocalDateTime.now().minusDays(validadeSenhaDias + 1))));

        SenhaExpiradaException ex =
                assertThrows(SenhaExpiradaException.class, () -> service.validarSenhaExpirada(usuario));
        assertEquals(SENHA_EXPIRADA.getChave(), ex.getMessage());
    }

    @Test
    @DisplayName("Lança AutenticacaoApiRunTimeException quando ocorre erro inesperado na validação")
    void deveLancarExcecaoGenericaSeErroNoMetodo() {
        when(historicoAutenticacaoRepository
                .findTopByUsuarioResponsavelOrderByDataHoraCriacaoDesc(usuario))
                .thenThrow(new RuntimeException("Erro inesperado"));

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

        usuario.setAutenticacao(autenticacao);
        autenticacao.setSenha("senhaCodificadaAntiga");

        when(passwordEncoder.matches(senhaAntiga, autenticacao.getSenha())).thenReturn(true);
        when(passwordEncoder.encode(novaSenha)).thenReturn(senhaCodificada);
        when(autenticacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Mocka método estático obterUsuarioLogado para retornar o usuário criado
        try (MockedStatic<UsuarioAutenticadoProvider> mockedStatic = mockStatic(UsuarioAutenticadoProvider.class)) {
            mockedStatic.when(UsuarioAutenticadoProvider::obterUsuarioLogado).thenReturn(usuario);

            assertDoesNotThrow(() -> service.alterarSenha(senhaAntiga, novaSenha));

            assertEquals(senhaCodificada, autenticacao.getSenha());
            verify(autenticacaoRepository).save(autenticacao);
            verify(historicoAutenticacaoRepository).save(any(HistoricoAutenticacao.class));
        }
    }

    @Test
    @DisplayName("Lança ValidacaoException quando senha atual está incorreta")
    void deveLancarValidacaoExceptionSeSenhaAtualIncorreta() {
        String senhaAntiga = "senhaAntigaErrada";
        String novaSenha = "novaSenha123";

        usuario.setAutenticacao(autenticacao);
        autenticacao.setSenha("senhaCodificadaAntiga");

        when(passwordEncoder.matches(senhaAntiga, autenticacao.getSenha())).thenReturn(false);

        try (MockedStatic<UsuarioAutenticadoProvider> mockedStatic = mockStatic(UsuarioAutenticadoProvider.class)) {
            mockedStatic.when(UsuarioAutenticadoProvider::obterUsuarioLogado).thenReturn(usuario);

            ValidacaoException ex = assertThrows(ValidacaoException.class, () -> service.alterarSenha(senhaAntiga, novaSenha));
            assertEquals(SENHA_ATUAL_INCORRETA.getChave(), ex.getMessage());

            verify(autenticacaoRepository, never()).save(any());
            verify(historicoAutenticacaoRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Lança AutenticacaoApiRunTimeException quando erro ocorre ao salvar senha")
    void deveLancarExcecaoGenericaSeErroNoAlterarSenha() {
        String senhaAntiga = "senhaAntiga123";
        String novaSenha = "novaSenha123";

        usuario.setAutenticacao(autenticacao);
        autenticacao.setSenha("senhaCodificadaAntiga");

        when(passwordEncoder.matches(senhaAntiga, autenticacao.getSenha())).thenReturn(true);
        when(passwordEncoder.encode(novaSenha)).thenReturn("codificada");
        doThrow(new RuntimeException("Erro banco")).when(autenticacaoRepository).save(any());

        try (MockedStatic<UsuarioAutenticadoProvider> mockedStatic = mockStatic(UsuarioAutenticadoProvider.class)) {
            mockedStatic.when(UsuarioAutenticadoProvider::obterUsuarioLogado).thenReturn(usuario);

            AutenticacaoApiRunTimeException ex =
                    assertThrows(
                            AutenticacaoApiRunTimeException.class,
                            () -> service.alterarSenha(senhaAntiga, novaSenha));
            assertTrue(ex.getMessage().contains(ERRO_ALTERAR_SENHA.getChave()));
        }
    }

    // --- Método auxiliar para criar HistoricoAutenticacao com data ---

    private HistoricoAutenticacao criarHistoricoComData(LocalDateTime data) {
        return HistoricoAutenticacao.builder().dataAlteracao(data).build();
    }
}
