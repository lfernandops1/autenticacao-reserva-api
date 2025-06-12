package com.autenticacao.api.app.config.security.util;

public class Roles {
  public static final String ADMIN = "hasRole('ADMIN')";
  public static final String ADMIN_OR_SELF = "hasRole('ADMIN') or #id == principal.id";
}
