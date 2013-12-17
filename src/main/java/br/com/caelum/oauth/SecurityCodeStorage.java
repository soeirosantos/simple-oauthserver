package br.com.caelum.oauth;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

/**
 * Simula um storage para armazenamento de tokens 
 * e codigos de autenticacao 
 */
@ApplicationScoped
public class SecurityCodeStorage {

	private Set<String> tokens = new HashSet<String>();
	private Set<String> authCodes = new HashSet<String>();

	public void addToken(String token) {
		if ( tokens.size() > 1000 ) {
			tokens.clear();
		}
		tokens.add(token);
	}

	public void addAuthCode(String token) {
		if ( authCodes.size() > 1000 ) {
			authCodes.clear();
		}
		authCodes.add(token);
	}

	public boolean isValidToken(String token) {
		return tokens.contains(token);
	}

	public boolean isValidAuthCode(String authCode) {
		return authCodes.contains(authCode);
	}
	
}