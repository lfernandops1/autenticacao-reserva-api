package com.autenticacao.api.unitarios.service;

import com.autenticacao.api.app.domain.entity.ControleAcessoUsuario;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.ContaBloqueadaException;
import com.autenticacao.api.app.repository.ControleAcessoUsuarioRepository;
import com.autenticacao.api.app.service.impl.TentativaLoginServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.autenticacao.api.app.util.enums.MensagemSistema.CONTA_BLOQUEADA;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TentativaLoginServiceImplTest {

    @InjectMocks
    private TentativaLoginServiceImpl tentativaLoginService;

    @Mock
    private ControleAcessoUsuarioRepository controleAcessoUsuarioRepository;

    private Usuario usuario;
    private ControleAcessoUsuario controle;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        controle = new ControleAcessoUsuario();
        controle.setUsuario(usuario);
        controle.setTentativasFalhas(0);
        controle.setBloqueadoAte(null);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário estiver bloqueado")
    void deveLancarExcecaoQuandoUsuarioBloqueado() {
        controle.setBloqueadoAte(LocalDateTime.now().plusHours(1));

        when(controleAcessoUsuarioRepository.findByUsuario(usuario)).thenReturn(Optional.of(controle));

        assertThatThrownBy(() -> tentativaLoginService.validarBloqueio(usuario))
                .isInstanceOf(ContaBloqueadaException.class)
                .hasMessageContaining(CONTA_BLOQUEADA.getChave());

        verify(controleAcessoUsuarioRepository).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Não deve lançar exceção quando usuário não estiver bloqueado")
    void naoDeveLancarExcecaoQuandoUsuarioNaoBloqueado() {
        controle.setBloqueadoAte(LocalDateTime.now().minusMinutes(1));
        when(controleAcessoUsuarioRepository.findByUsuario(usuario)).thenReturn(Optional.of(controle));
        tentativaLoginService.validarBloqueio(usuario);

        controle.setBloqueadoAte(null);
        when(controleAcessoUsuarioRepository.findByUsuario(usuario)).thenReturn(Optional.of(controle));
        tentativaLoginService.validarBloqueio(usuario);

        verify(controleAcessoUsuarioRepository, times(2)).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Deve registrar tentativa falha e aumentar contador")
    void deveRegistrarTentativaFalhaEAumentarContador() {
        controle.setTentativasFalhas(2);
        when(controleAcessoUsuarioRepository.findByUsuario(usuario)).thenReturn(Optional.of(controle));
        when(controleAcessoUsuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        tentativaLoginService.registrarFalha(usuario);

        assertEquals(3, controle.getTentativasFalhas());
        // Ainda não bloqueado
        assertEquals(null, controle.getBloqueadoAte());

        verify(controleAcessoUsuarioRepository).save(controle);
    }

    @Test
    @DisplayName("Deve bloquear usuário após atingir limite de tentativas falhas")
    void deveBloquearUsuarioAposLimiteTentativas() {
        controle.setTentativasFalhas(4);
        when(controleAcessoUsuarioRepository.findByUsuario(usuario)).thenReturn(Optional.of(controle));
        when(controleAcessoUsuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        tentativaLoginService.registrarFalha(usuario);

        assertEquals(5, controle.getTentativasFalhas());
        assertThat(controle.getBloqueadoAte()).isAfter(LocalDateTime.now());

        verify(controleAcessoUsuarioRepository).save(controle);
    }

    @Test
    @DisplayName("Deve resetar tentativas e remover bloqueio")
    void deveResetarTentativasERemoverBloqueio() {
        controle.setTentativasFalhas(3);
        controle.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));
        when(controleAcessoUsuarioRepository.findByUsuario(usuario)).thenReturn(Optional.of(controle));
        when(controleAcessoUsuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        tentativaLoginService.resetarTentativas(usuario);

        assertEquals(0, controle.getTentativasFalhas());
        assertEquals(null, controle.getBloqueadoAte());

        verify(controleAcessoUsuarioRepository).save(controle);
    }

    @Test
    @DisplayName("Deve criar novo controle se não existir ao buscar")
    void deveCriarNovoControleSeNaoExistir() {
        when(controleAcessoUsuarioRepository.findByUsuario(usuario)).thenReturn(Optional.empty());

        List<ControleAcessoUsuario> salvos = new ArrayList<>();

        // Intercepta a chamada a save e captura o estado atual do objeto salvo
        when(controleAcessoUsuarioRepository.save(any())).thenAnswer(invocation -> {
            ControleAcessoUsuario controle = invocation.getArgument(0);
            // Cria uma cópia para garantir estado no momento da chamada (não referência mutável)
            ControleAcessoUsuario copia = new ControleAcessoUsuario();
            copia.setUsuario(controle.getUsuario());
            copia.setTentativasFalhas(controle.getTentativasFalhas());
            copia.setBloqueadoAte(controle.getBloqueadoAte());
            salvos.add(copia);
            return controle;
        });

        // Chama o método testado
        tentativaLoginService.registrarFalha(usuario);

        // Verifica que houve duas chamadas ao save
        verify(controleAcessoUsuarioRepository, times(2)).save(any());

        // Agora verifica os estados salvos
        ControleAcessoUsuario primeiroSalvo = salvos.get(0);
        assertEquals(usuario, primeiroSalvo.getUsuario());
        assertEquals(0, primeiroSalvo.getTentativasFalhas());

        ControleAcessoUsuario segundoSalvo = salvos.get(1);
        assertEquals(usuario, segundoSalvo.getUsuario());
        assertEquals(1, segundoSalvo.getTentativasFalhas());
    }
}
