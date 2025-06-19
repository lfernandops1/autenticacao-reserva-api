package com.autenticacao.api.app.util.enums;

import lombok.Getter;

@Getter
public enum MensagemSistema {
  ENTRADA_DE_DADOS_INVALIDA(-1, "entrada.de.dados.invalida"),
  CAMPO_INVALIDO_NAO_IDENTIFICADO(-2, "campo.invalido.nao.identificado"),
  EMAIL_JA_CADASTRADO(-3, "email.ja.cadastrado"),
  TELEFONE_JA_CADASTRADO(-4, "telefone.ja.cadastrado"),
  EMAIL_NAO_ENCONTRADO(-5, "email.nao.encontrado"),
  USUARIO_NAO_ENCONTRADO_POR_ID(-6, "usuario.nao.encontrado.por.id"),
  USUARIO_NAO_ENCONTRADO_POR_EMAIL(-7, "usuario.nao.encontrado.por.email"),
  USUARIO_DESATIVADO(-8, "usuario.desativado"),
  CAMPO_NAO_ENCONTRADO(-9, "campo.nao.encontrado"),
  LISTA_GENERICO_INVALIDO(-10, "Lista.generico.invalido"),
  CAMPO_GENERICO_INVALIDO(-11, "Campo.generico.invalido"),
  ENTRADA_DADOS_INVALIDA(-12, "Entrada.dados.invalido"),
  USUARIO_JA_POSSUI_AUTENTICACAO(-13, "usuario.ja.possui.autenticacao"),
  ERRO_AO_DESATIVAR_AUTENTICACAO_DO_USUARIO(-14, "erro.ao.desativar.autenticacao.usuario"),
  ERRO_AO_ATUALIZAR_DADOS_USUARIO(-15, "erro.ao.atualizar.dados.usuario"),
  EMAIL_OU_SENHA_INVALIDO(-16, "email.ou.senha.invalido"),
  ERRO_AO_DESATIVAR_USUARIO(-17, "erro.ao.desativar.usuario"),
  ERRO_AO_BUSCAR_USUARIO(-18, "erro.ao.buscar.usuario"),
  ERRO_AO_ACESSAR_CAMPO(-19, "erro.ao.acessar.campo"),
  ERRO_AO_TENTAR_CRIAR_USUARIO(-20, "erro.ao.tentar.criar.usuario"),
  ERRO_AO_TENTAR_CRIAR_AUTENTICACAO_PARA_USUARIO(
      -21, "erro.ao.tentar.criar.autenticacao.para.usuario"),
  EMAIL_INVALIDO(-22, "email.invalido"),
  TELEFONE_INVALIDO(-23, "telefone.invalido"),
  ERRO_DURANTE_VERIFICACAO_CAMPOS(-24, "erro.durante.verificacao.campos"),
  ERRO_ENQUANTO_GERAVA_TOKEN_DE_ACESSO(-25, "erro.enquanto.gerava.token.de.acesso"),
  ERRO_JA_EXISTE_AUTENTICACAO_ASSOCIADA_A_ESSE_USUARIO(
      -26, "erro.ja.existe.autenticacao.associada.a.esse.usuario"),
  OBTENDO_DADOS_AUTENTICACAO(-27, "obtendo.dados.autenticacao"),
  ERRO_AO_EXCLUIR_USUARIO(-28, "erro.ao.excluir.usuario"),
  USUARIO_NAO_AUTENTICADO(-29, "usuario.nao.autenticado"),
  USUARIO_NAO_ENCONTRADO(-30, "usuario.nao.encontrado"),
  ERRO_REALIZAR_LOGIN(-31, "erro.ao.realizar.login"),
  ERRO_ALTERAR_SENHA(-32, "erro.ao.alterar.senha"),
  ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO(-33, "erro.registrar.historico.alteracao.usuario"),
  ERRO_CRIAR_REFRESH_TOKEN(-34, "erro.criar.refresh.token"),
  ERRO_VALIDAR_REFRESH_TOKEN(-35, "erro.validar.refresh.token"),
  ERRO_OBTER_USUARIO_REFRESH_TOKEN(-36, "erro.obter.usuario.refresh.token"),
  ERRO_REMOVER_REFRESH_TOKENS_USUARIO(-37, "erro.remover.refresh.tokens.usuario"),
  TOKEN_NULO_OU_VAZIO(-33, "token.nulo.ou.vazio"),
  REFRESH_TOKEN_INVALIDO_OU_EXPIRADO(-38, "refresh.token.invalido.ou.expirado"),
  ERRO_VALIDAR_EXPIRACAO_SENHA(-39, "erro.validar.expiracao.senha"),
  SENHA_EXPIRADA(-40, "senha.expirada"),
  ERRO_REGISTRAR_TENTATIVA_LOGIN(-41, "erro.registrar.tentativa.login"),
  ERRO_RESETAR_TENTATIVAS_LOGIN(-42, "erro.resetar.tentativas.login"),
  CONTA_BLOQUEADA(-43, "conta.bloqueada"),
  EMAIL_OU_SENHA_INVALIDOS(-44, "email.ou.senha.invalidos"),
  SENHA_ATUAL_INCORRETA(-45, "senha.atual.incorreta"),
  ACESSO_NEGADO(-46, "acesso.negado"),
  ERRO_INSPERADO(-998, "erro.inesperado"),
  NAO_IDENTIFICADO(-999, "erro.nao.identificado");
  private final int codigo;
  private final String chave;

  MensagemSistema(int codigo, String chave) {
    this.codigo = codigo;
    this.chave = chave;
  }
}
