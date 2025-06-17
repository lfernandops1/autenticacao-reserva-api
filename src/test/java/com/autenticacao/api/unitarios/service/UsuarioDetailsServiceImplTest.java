package com.autenticacao.api.unitarios.service;

import static com.autenticacao.api.app.util.enums.MensagemSistema.EMAIL_OU_SENHA_INVALIDOS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.impl.UsuarioDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceImplTest {

  @Mock private UsuarioRepository usuarioRepository;
  @InjectMocks private UsuarioDetailsServiceImpl service;

  private static final String EMAIL = "teste@exemplo.com";

  private Usuario usuario;

  @BeforeEach
  void setUpd() {
    usuario = Usuario.builder().email(EMAIL).build();
  }

  @Test
  @DisplayName("Deve retornar UserDetails quando usuário existir")
  void deveRetornarUserDetailsQuandoUsuarioExistir() {

    when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(this.usuario));

    UserDetails userDetails = service.loadUserByUsername(EMAIL);

    assertNotNull(userDetails);
    assertEquals(this.usuario, userDetails);
    verify(usuarioRepository).findByEmail(EMAIL);
  }

  @Test
  @DisplayName("Deve Lançar exceção quando tentar retornar UserDetails quando usuário não existir")
  void deveLancarUsernameNotFoundExceptionQuandoUsuarioNaoExistir() {

    when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    UsernameNotFoundException exception =
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(EMAIL));

    assertEquals(EMAIL_OU_SENHA_INVALIDOS.getChave(), exception.getMessage());
    verify(usuarioRepository).findByEmail(EMAIL);
  }
}
