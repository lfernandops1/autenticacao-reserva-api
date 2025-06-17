package com.autenticacao.api.app;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public interface Constantes {

  interface Util {
    // Caracteres usados em senhas ou ‘tokens’
    String CARACTERES_SENHA =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

    String SEM_DESCRICAO = "SEM DESCRIÇÃO";
    String STRING_VAZIA = "";

    // Formatos úteis
    DateTimeFormatter FORMATO_DATA_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    DateTimeFormatter FORMATO_DATA_HORA_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    ZoneOffset ZONE_OFFSET_BR = ZoneOffset.ofHours(-3);

    String ASTERISCO = "*";
    String BARRA_ALL = "/**";

    // Métodos dinâmicos são melhores que constantes para data/ano atual
    static int anoAtual() {
      return java.time.LocalDate.now().getYear();
    }
  }

  interface Http {
    // Métodos HTTP
    String GET = "GET";
    String POST = "POST";
    String PUT = "PUT";
    String DELETE = "DELETE";
    String PATCH = "PATCH";
    String OPTIONS = "OPTIONS";

    // Códigos HTTP comuns
    int HTTP_200_OK = 200;
    int HTTP_201_CREATED = 201;
    int HTTP_400_BAD_REQUEST = 400;
    int HTTP_401_UNAUTHORIZED = 401;
    int HTTP_403_FORBIDDEN = 403;
    int HTTP_404_NOT_FOUND = 404;
    int HTTP_500_INTERNAL_ERROR = 500;

    // Headers comuns
    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_BEARER_PREFIX = "Bearer ";
  }

  interface Web {
    // URLs e portas
    String LOCALHOST = "http://localhost:";
    String PORTA_4200 = "4200";
  }

  interface Permissoes {
    String ROLE_ADMIN = "ROLE_ADMIN";
    String ROLE_USER = "ROLE_USER";

    String AUTH = "auth";
    String SEGREDO = "secret"; // cuidado com segredos em código
  }

  interface Schema {
    String AUTENTICACAO = "autenticacao";
  }

  interface Tabelas {
    String USUARIOS = "usuarios";
    String TOKEN_BLACK_LIST = "token_black_list";
    String REFRESH_TOKEN = "refresh_token";
    String AUTENTICACOES = "autenticacoes";
    String HISTORICO_USUARIO = "historico_usuarios";
  }

  interface ColunasUsuario {
    String ID = "id";
    String NOME = "nome";
    String SOBRENOME = "sobrenome";
    String EMAIL = "email";
    String TELEFONE = "telefone";
    String ATIVO = "ativo";
    String DATA_HORA_CRIACAO = "data_hora_criacao";
    String DATA_HORA_ATUALIZACAO = "data_hora_atualizacao";
    String DATA_NASCIMENTO = "data_nascimento";
  }

  interface ColunasAutenticacao {
    String USUARIO_ID = "usuario_id";
    String EMAIL = "email"; // repetido, mas pode ficar para contexto
    String SENHA = "senha";
    String DATA_HORA_CRIACAO = "data_hora_criacao";
    String DATA_HORA_ATUALIZACAO = "data_hora_atualizacao";
    String SENHA_ATUALIZACAO = "senha_alterada_em";
    String DATA_HORA_EXCLUSAO = "data_hora_exclusao";
    String ATIVO = "ativo";
    String USUARIO = "usuario";
  }

  interface ColunasHistoricoUsuario {
    String TIPO_MOVIMENTACAO = "tipo_alteracao";
    String USUARIO_RESPONSAVEL = "usuario_responsavel";
    String CAMPOS_ALTERADOS = "campos_alterados";
  }

  interface Rotas {
    // Autenticação
    String LOGIN = "/login";
    String REFRESH_TOKEN = "/refresh-token";
    String LOGOUT = "/logout";
    String API_AUTENTICAR = "/api/autenticacao";
    String ALTERAR_SENHA = "/alterar-senha";
    String REVOKE_REFRESH_TOKEN = "/revoke-refresh-token";

    // Usuários
    String LISTAR_TODOS = "/listar-todos";
    String CRIAR = "/criar";
    String GET_USUARIO_LOGADO = "LOGADO";
    String CRIAR_ADMIN = "/criar-admin";
    String USUARIO = "/usuario";
    String FILTRAR_USUARIO = "/usuario/filtrar";
    String ID = "/{id}";
    String BUSCAR_POR_ID = "/buscar/{id}";
    String BUSCAR = "/buscar";
    String ATUALIZAR_POR_ID = "/atualizar/{id}";
    String SENHA = "/senha";
    String API_USUARIOS = "/api/usuarios";
    String DESATIVAR = "/{id}/desativar";
    String USUARIO_ID = "/{usuarioId}";
  }
}
