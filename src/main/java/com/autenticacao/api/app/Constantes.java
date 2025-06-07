package com.autenticacao.api.app;

import java.time.LocalDateTime;

public interface Constantes {
  interface Util {
    String caracters =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
    String SEM_DESCRICAO = "SEM DESCRIÇÃO";
    String STRING_VAZIA = "";
    LocalDateTime AGORA = LocalDateTime.now();
    int ANO_ATUAL = LocalDateTime.now().getYear();

    String GMT = "-03:00";
    String ASTERISTICO = "*";
    String BARRA_ALL = "/**";

    String DE = "de";
  }

  interface Web {
    String GET = "GET";
    String PUT = "PUT";
    String POST = "POST";
    String DELETE = "DELETE";
    String PATCH = "PATCH";
    String OPTIONS = "OPTIONS";
    String ENDERECO = "http://localhost:";
    String PORTA_4200 = "4200";
  }

  interface Mensagens {
    String ERRO_AO_ATUALIZAR_DADOS_USUARIO = "Ocorreu um erro ao atualizr usuário";

    String EMAIL_OU_SENHA_INVALIDO = "Email ou senha invalidos";

    String ERRO_AO_ACESSAR_CAMPO = "Erro ao acessar campo: ";
    String ERRO_AO_TENTAR_CRIAR_USUARIO = "Erro ao tentar criar usuario";
    String ERRO_AO_TENTAR_CRIAR_AUTENTICACAO_PARA_USUARIO =
        "Erro ao tentar criar autenticação para o usuário";
    String EMAIL_INVALIDO = "Email inválido.";
    String TELEFONE_INVALIDO = "Telefone inválido.";
    String ERRO_DURANTE_VERIFICACAO_CAMPOS =
        "Ocorreu um problema durante a verificação de campos "
            + "nulos dos dados da classe %s e do campo %s";

    String ERRO_ENQUANTO_GERAVA_TOKEN_DE_ACESSO = "Erro durante a geraçãod o token de acesso";
    String ERRO_JA_EXISTE_AUTENTICACAO_ASSOCIADA_A_ESSSE_USUARIO =
        "Já existe uma autenticação associada a este usuário.";
    String OBTENDO_DADOS_AUTENTICACAO = "Obtendo dados para criar autenticação";
    String ERRO_AO_EXCLUIR_USUARIO = "Erro ao desativar usuário";
  }

  interface PERMISSOES {
    String AUTHORIZATION = "Authorization";
    String BEARER = "Bearer ";
    String AUTH = "auth";
    String SEGREDO = "secret";
    String ROLE_ADMIN = "ROLE_ADMIN";
    String ROLE_USER = "ROLE_USER";
  }

  interface TABELAS {
    String TABELA_USUARIOS = "usuarios";
    String TABELA_REFRESH_TOKEN = "refresh_token";
    String TABELA_AUTENTICACOES = "autenticacoes";
  }

  interface SCHEMA {
    String SCHEMA_AUTENTICACAO = "autenticacao";
  }

  interface TABELA_AUTENTICACAO {
    String USUARIO_ID = "usuario_id";
    String EMAIL = "email";
    String SENHA = "senha";
    String DATA_HORA_CRIACAO = "data_hora_criacao";
    String DATA_HORA_ATUALIZACAO = "data_hora_atualizacao";
    String DATA_HORA_EXCLUSAO = "data_hora_exclusao";
    String ATIVO = "ativo";
    String USUARIO = "usuario";
  }

  interface TABELA_USUARIO {
    String ID = "id";
    String NOME = "nome";
    String SOBRENOME = "sobrenome";
    String EMAIL = "email";
    String TELEFONE = "telefone";
    String ATIVO = "ativo";
    String AUTENTICACAO = "autenticacao";
    String FOLHAS = "folhas";
    String GASTOS = "gastos";
    String USER_ROLE = "user_role";
    String DATA_HORA_CRIACAO = "data_hora_criacao";
    String DATA_HORA_ATUALIZACAO = "data_hora_atualizacao";
    String DATA_NASCIMENTO = "data_nascimento";
  }

  interface ROTAS {
    String LOGIN = "/login";
    String LOGOUT = "/logout";
    String API_AUTENTICAR = "/api/autenticacao";
    String ALTERAR_SENHA = "/alterar-senha";

    String LISTAR_TODOS = "/listar-todos";
    String CRIAR = "/criar";
    String USUARIO = "/usuario";
    String FILTRAR_USUARIO = "/usuario/filtrar";
    String ID = "/{id}";

    String BUSCAR_POR_ID = "/buscar/{id}";
    String BUSCAR = "/buscar";
    String ATUALIZAR_POR_ID = "/atualizar/{id}";
    String SENHA = "/senha";
    String API_USUARIOS = "/api/usuarios";

    String DESATIVAR = "/excluir/{id}";
    String USUARIO_ID = "/{usuarioId}";
  }
}
