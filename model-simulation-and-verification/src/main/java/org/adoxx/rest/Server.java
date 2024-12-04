package org.adoxx.rest;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.adoxx.utils.Utils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Server {

    private HttpServer server = null;
    
    public Server(){
        this("http://0.0.0.0/rest");
    }
    
    public Server(String endpoint){
        int port = getPort();
        URI baseUri = null;
        if(endpoint.split(":").length==3)
            baseUri = UriBuilder.fromUri(endpoint).build();
        else
            baseUri = UriBuilder.fromUri(endpoint).port(port).build();
        ResourceConfig resourceConfig = new ResourceConfig(RESTService.class);
        resourceConfig.register(CORSResponseFilter.class);
        server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig, false);
    }
    
    private int getPort(){
        int port = 9998;
        return port;
    }
    
    public void start() throws Exception{
        server.start();
    }
    
    public static void main(String[] args) {
        try{
            Server server = new Server();
            server.start();
        }catch(Exception ex){ex.printStackTrace(); Utils.log(ex);}
    }
}
