package br.com.caelum.oauth.clients;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

public class DirectTokenSampleClientOAuth {

	private static final Client client = ClientBuilder.newClient();

	public static void main(String[] args) throws Exception {

		// primeiro tenta acessar o servidor de recursos diretamente
		//makeFailResourceRequest();
		
		makeEndToEndWithDirectTokenRequest();

	}

	private static OAuthAccessTokenResponse makeDirectTokenRequest() throws OAuthSystemException, OAuthProblemException {
		
		OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(ClientParams.OAUTH_SERVER_URL + "oauth/token")
                .setGrantType(GrantType.PASSWORD)
                .setClientId(ClientParams.CLIENT_ID)
                .setClientSecret(ClientParams.CLIENT_SECRET)
                .setUsername(ClientParams.USERNAME)
                .setPassword(ClientParams.PASSWORD)
                .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);

        System.out.println("access token: " + oauthResponse.getAccessToken());
        System.out.println("expira em: " + oauthResponse.getExpiresIn());
        
        return oauthResponse;
	}
	
	private static Response makeFailResourceRequest() throws URISyntaxException {
		WebTarget target = client.target(new URI(ClientParams.RESOURCE_SERVER_URL + "pagamentos"));
		Response response = target.request(MediaType.TEXT_HTML).get();
		System.out.println(response.getStatus());
		response.close();
		return response;
	}

	private static void makeEndToEndWithDirectTokenRequest() throws OAuthSystemException, OAuthProblemException, MalformedURLException, URISyntaxException {
            
		OAuthAccessTokenResponse oauthResponse = makeDirectTokenRequest();
        String accessToken = oauthResponse.getAccessToken();
        
        URL restUrl = new URL(ClientParams.RESOURCE_SERVER_URL + "pagamentos");
        
        WebTarget target = client.target(restUrl.toURI());
        String entity = target.request(MediaType.APPLICATION_JSON)
                .header(ClientParams.HEADER_AUTHORIZATION, "Bearer " + accessToken)
                .get(String.class);
        
        System.out.println("Response: " + entity);
    }

}
