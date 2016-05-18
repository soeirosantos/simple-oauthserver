package br.com.caelum.oauth.clients;

public class ClientParams {

	// Alguns dados necessários para simular o 
	// fluxo de autenticação
	
	public static String CLIENT_ID = "oauth2_client_id";
	public static String CLIENT_SECRET = "oauth2_client_secret";
	
	public static String USERNAME = "fake_user";
	public static String PASSWORD = "passwd";
	
	public static String RESOURCE_SERVER_NAME = "pagamentos";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	
	public static final String OAUTH_SERVER_URL =  "http://localhost:8080/";
	public static final String RESOURCE_SERVER_URL = "http://localhost:8080/resource-server/";

}
