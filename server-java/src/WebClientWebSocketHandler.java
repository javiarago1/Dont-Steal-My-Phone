import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class WebClientWebSocketHandler {


    @OnWebSocketConnect
    public void onConnect(Session session) {
        String deviceId = session.getUpgradeRequest().getParameterMap().get("deviceId").get(0);
        System.out.println("New client connected with device ID: " + deviceId);
        startTheftProtocol(deviceId);
        SessionManager.addWebClient(deviceId, session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String deviceId = session.getUpgradeRequest().getParameterMap().get("deviceId").get(0);
        if (deviceId != null) {
            SessionManager.removeWebClientById(deviceId);
        }
        System.out.println("Client disconnected with device ID: " + deviceId);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        // Manejar los mensajes entrantes aquí
    }


    public static void startTheftProtocol(String deviceId) {
        AndroidWebSocketHandler.sendToClient(deviceId, "start_theft_alarm");
    }
}

