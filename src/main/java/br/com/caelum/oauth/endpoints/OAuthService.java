package br.com.caelum.oauth.endpoints;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * can be used by JBoss/WildFly
 * in this case configs in web.xml
 * it's not necessary 
 *
 */
@ApplicationPath("/oauth")
public class OAuthService extends Application { }