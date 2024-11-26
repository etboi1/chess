package websocket;

import exception.ResponseException;
import org.glassfish.grizzly.http.server.Response;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;

    public WebSocketFacade(String url) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //YOU NEED TO FIGURE OUT WHAT'S GOING ON WITH NOTIFICATION HANDLER AT SOME POINT
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    public void connect() throws ResponseException {

    }

    public void makeMove() throws ResponseException {

    }

    public void leave() throws ResponseException {

    }

    public void resign() throws ResponseException {

    }
}
