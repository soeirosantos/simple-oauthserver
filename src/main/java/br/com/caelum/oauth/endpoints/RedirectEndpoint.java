package br.com.caelum.oauth.endpoints;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Para um exemplo mais próximo de uma situação real, 
 * utilizando autenticação via browser p. ex.,
 * este endpoint deve estar localizado no client
 *
 */
@Path("/redirect")
public class RedirectEndpoint {

    @Context
    HttpHeaders httpHeaders;
    
    @Context
    UriInfo uriInfo;

    @GET
    public String redirect() {
        
    	JSONObject object = new JSONObject();
        JSONObject headers = new JSONObject(); 
        JSONObject queryParameteres = new JSONObject();
        
        String json = "error!";
        
        try {
            for (Map.Entry<String, List<String>> entry : httpHeaders.getRequestHeaders().entrySet()) {
                headers.put(entry.getKey(), entry.getValue().get(0));
            }
            
            object.put("headers", headers);
            
            for (Map.Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
                queryParameteres.put(entry.getKey(), entry.getValue().get(0));
            }
            object.put("queryParameters", queryParameteres);
            
            json = object.toString(4);
            
        } catch (JSONException ex) {
        
        	ex.printStackTrace();
        
        }
        
        return json;
    }
}
