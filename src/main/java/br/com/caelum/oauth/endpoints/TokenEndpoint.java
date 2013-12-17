package br.com.caelum.oauth.endpoints;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import br.com.caelum.oauth.ServerParams;
import br.com.caelum.oauth.SecurityCodeStorage;

@Path("/token")
public class TokenEndpoint {

	public static final String INVALID_CLIENT_DESCRIPTION = "Client authentication failed "
			                                              + "(e.g., unknown client, no client "
			                                              + "authentication included, or unsupported "
			                                              + "authentication method).";

	@Inject
	private SecurityCodeStorage securityCodeStorage;

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response authorize(@Context HttpServletRequest request)
			throws OAuthSystemException {
		try {
			OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
			OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(
					new MD5Generator());

			if (!checkClientId(oauthRequest.getClientId())) {
				return buildInvalidClientIdResponse();
			}

			if (!checkClientSecret(oauthRequest.getClientSecret())) {
				return buildInvalidClientSecretResponse();
			}

			if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
					GrantType.AUTHORIZATION_CODE.toString())) {
				if (!checkAuthCode(oauthRequest.getParam(OAuth.OAUTH_CODE))) {
					return buildBadAuthCodeResponse();
				}
			} else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
					GrantType.PASSWORD.toString())) {
				if (!checkUserPass(oauthRequest.getUsername(),
						oauthRequest.getPassword())) {
					return buildInvalidUserPassResponse();
				}
			} else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
					GrantType.REFRESH_TOKEN.toString())) {
				// not supported in this implementation
				buildInvalidUserPassResponse();
			}

			final String accessToken = oauthIssuerImpl.accessToken();

			securityCodeStorage.addToken(accessToken);

			OAuthResponse response = OAuthASResponse
					.tokenResponse(HttpServletResponse.SC_OK)
					.setAccessToken(accessToken).setExpiresIn("3600")
					.buildJSONMessage();

			return Response.status(response.getResponseStatus())
					.entity(response.getBody()).build();

		} catch (OAuthProblemException e) {
			OAuthResponse res = OAuthASResponse
					.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
					.buildJSONMessage();
			return Response.status(res.getResponseStatus())
					.entity(res.getBody()).build();
		}
	}

	@GET
	@Path("/{token}")
	public Response exists(@PathParam("token") String token) {
		if (securityCodeStorage.isValidToken(token)) {
			return Response.status(Status.OK).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	private Response buildInvalidClientIdResponse() throws OAuthSystemException {
		OAuthResponse response = OAuthASResponse
				.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
				.setError(OAuthError.TokenResponse.INVALID_CLIENT)
				.setErrorDescription(INVALID_CLIENT_DESCRIPTION)
				.buildJSONMessage();
		return Response.status(response.getResponseStatus())
				.entity(response.getBody()).build();
	}

	private Response buildInvalidClientSecretResponse()
			throws OAuthSystemException {
		OAuthResponse response = OAuthASResponse
				.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
				.setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
				.setErrorDescription(INVALID_CLIENT_DESCRIPTION)
				.buildJSONMessage();
		return Response.status(response.getResponseStatus())
				.entity(response.getBody()).build();
	}

	private Response buildBadAuthCodeResponse() throws OAuthSystemException {
		OAuthResponse response = OAuthASResponse
				.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
				.setError(OAuthError.TokenResponse.INVALID_GRANT)
				.setErrorDescription("invalid authorization code")
				.buildJSONMessage();
		return Response.status(response.getResponseStatus())
				.entity(response.getBody()).build();
	}

	private Response buildInvalidUserPassResponse() throws OAuthSystemException {
		OAuthResponse response = OAuthASResponse
				.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
				.setError(OAuthError.TokenResponse.INVALID_GRANT)
				.setErrorDescription("invalid username or password")
				.buildJSONMessage();
		return Response.status(response.getResponseStatus())
				.entity(response.getBody()).build();
	}

	private boolean checkClientId(String clientId) {
		return ServerParams.CLIENT_ID.equals(clientId);
	}

	private boolean checkClientSecret(String secret) {
		return ServerParams.CLIENT_SECRET.equals(secret);
	}

	private boolean checkAuthCode(String authCode) {
		return securityCodeStorage.isValidAuthCode(authCode);
	}

	private boolean checkUserPass(String user, String pass) {
		return ServerParams.PASSWORD.equals(pass) && ServerParams.USERNAME.equals(user);
	}
}