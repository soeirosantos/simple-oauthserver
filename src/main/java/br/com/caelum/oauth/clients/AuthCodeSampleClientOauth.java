package br.com.caelum.oauth.clients;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AuthCodeSampleClientOauth {

	private static Client client = ClientBuilder.newClient();

	public static void main(String[] args) throws Exception {

		// primeiro tenta acessar o servidor de recursos diretamente
		//makeFailResourceRequest();

		// testa obtenção do auth code após usuário se autenticar 
		//via user agent (browser) diretamente no auth server
		//Response response = makeAuthCodeRequest();
		 
		makeEndToEndWithAuthCode();

	}

	private static void makeFailResourceRequest() throws URISyntaxException {
		WebTarget target = client.target(new URI(ClientParams.RESOURCE_SERVER_URL + "pagamentos"));
		Response response = target.request(MediaType.TEXT_HTML).get();
		System.out.println(response.getStatus());
		response.close();
	}
	
	private static Response makeAuthCodeRequest() throws OAuthSystemException,
			MalformedURLException, URISyntaxException {

		OAuthClientRequest request = OAuthClientRequest
				.authorizationLocation(ClientParams.OAUTH_SERVER_URL + "oauth/auth")
				.setClientId(ClientParams.CLIENT_ID)
				.setRedirectURI(ClientParams.OAUTH_SERVER_URL + "oauth/redirect") // Simplificação para teste. A rigor a uri de redirecionamento deve estar no client
				.setResponseType(ResponseType.CODE.toString())
				.setState("state")
				.buildQueryMessage();

		WebTarget target = client.target(new URI(request.getLocationUri()));
		Response response = target.request(MediaType.TEXT_HTML).get();

		System.out.println(response.getLocation());
		
		return response;
	}

	private static OAuthAccessTokenResponse makeTokenRequestWithAuthCode(
			String authCode) throws OAuthProblemException, OAuthSystemException {

		OAuthClientRequest request = OAuthClientRequest
				.tokenLocation(ClientParams.OAUTH_SERVER_URL + "oauth/token")
				.setClientId(ClientParams.CLIENT_ID)
				.setClientSecret(ClientParams.CLIENT_SECRET)
				.setGrantType(GrantType.AUTHORIZATION_CODE)
				.setCode(authCode)
				.setRedirectURI(ClientParams.OAUTH_SERVER_URL + "oauth/redirect") // Simplificação para teste. A rigor a uri de redirecionamento deve estar no client
				.buildBodyMessage();

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		
		OAuthAccessTokenResponse oauthResponse = oAuthClient
				.accessToken(request);

		System.out.println("Access Token: " + oauthResponse.getAccessToken());
		System.out.println("Expires In: " + oauthResponse.getExpiresIn());
		
		return oauthResponse;
	}

	public static void makeEndToEndWithAuthCode() throws URISyntaxException {
        try {
        	 Response response = makeAuthCodeRequest();
        	 response.close();

        	 // browser behavior
        	 Response redirectionByHeaderLocation = client.target(response.getLocation())
		 			  .request()
                      .get();

        	 
             String authCode = getAuthCode(redirectionByHeaderLocation);
             
             OAuthAccessTokenResponse oauthResponse = makeTokenRequestWithAuthCode(authCode);
             String accessToken = oauthResponse.getAccessToken();
             
             Response res = client.target(new URI(ClientParams.RESOURCE_SERVER_URL + "pagamentos"))
            		 			  .request(MediaType.APPLICATION_JSON)
                                  .header(ClientParams.HEADER_AUTHORIZATION, "Bearer " + accessToken)
                                  .get();
             
             System.out.println(res.getStatus());
             
             String entity = res.readEntity(String.class);
             
             System.out.println("Response: " + entity);
        
        } catch (OAuthProblemException | OAuthSystemException | JSONException | MalformedURLException ex) {
            
        	ex.printStackTrace();
        
        }
    }
	
	private static String getAuthCode(Response response) throws JSONException {
		JSONObject obj = new JSONObject(response.readEntity(String.class));
        JSONObject qp = obj.getJSONObject("queryParameters");
        String authCode = null;
        if (qp != null) {
            authCode = qp.getString("code");
        }

		return authCode;
	}

}
