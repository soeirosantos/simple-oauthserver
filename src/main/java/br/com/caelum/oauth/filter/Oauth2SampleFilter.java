package br.com.caelum.oauth.filter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;

/**
 * Sample Filter for be used in a resource server
 */

@WebFilter("/resources/*")
public class Oauth2SampleFilter implements Filter {

	private static final String OAUTH_SERVER_URL = "http://localhost:8080/";
	private static String RESOURCE_SERVER_NAME = "pagamentos";
		
    @Override
	public void destroy() {	}

    @Override
	public void init(FilterConfig fConfig) throws ServletException { }
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletResponse res = (HttpServletResponse) response;
		
		try {
		
			OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest((HttpServletRequest) request, ParameterStyle.HEADER);
			String accessToken = oauthRequest.getAccessToken();
			
			if ( !isValidToken(accessToken) ) {
				 
				OAuthResponse oauthResponse = OAuthRSResponse
	                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
	                        .setRealm(RESOURCE_SERVER_NAME)
	                        .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
	                        .buildHeaderMessage();

				res.addHeader(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
				res.setStatus(oauthResponse.getResponseStatus());
				res.sendError(oauthResponse.getResponseStatus());
			}
		
			chain.doFilter(request, response);
		
		} catch (OAuthSystemException | OAuthProblemException e) {
			
			try {
				
				OAuthResponse oauthResponse = OAuthRSResponse
				        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
				        .setRealm(RESOURCE_SERVER_NAME)
				        .buildHeaderMessage();
 
				res.addHeader(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
				res.setStatus(oauthResponse.getResponseStatus());
		        res.sendError(oauthResponse.getResponseStatus());
				
			} catch (OAuthSystemException e1) {
				
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error trying to handle oauth problem", e1);
			
			}
		
		}
		
	}
	
	private boolean isValidToken(String token) {
		boolean validToken = false;
		try {
			Client c = ClientBuilder.newClient();
			URL restUrl = new URL(OAUTH_SERVER_URL + "oauth/token/" + token);
			
			WebTarget target = c.target(restUrl.toURI());
			
			Response response = target.request().get();
			
			validToken = Status.OK.getStatusCode() == response.getStatus();

		} catch (URISyntaxException | MalformedURLException e) {
	
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error trying to access oauth server", e);
		
		}
		return validToken;
	}
}
