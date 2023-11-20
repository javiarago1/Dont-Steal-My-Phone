import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket(maxIdleTime = Integer.MAX_VALUE)
public class MyWebSocketHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("New client connected: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        // Eliminar la sesión asociada con el ID del dispositivo
        SessionManager.getSessions().entrySet().removeIf(entry -> entry.getValue().equals(session));
        System.out.println("Client disconnected: " + statusCode + " " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            Map<String, String> messageMap = mapper.readValue(message, new TypeReference<>() {});
            String type = messageMap.get("type");
            switch (type) {
                case "register_device":
                    String deviceId = messageMap.get("device_id");
                    if (deviceId != null) {
                        SessionManager.addSession(deviceId, session);
                        System.out.println("Device registered with ID: " + deviceId);
                    }
                    break;

                default:
                    System.out.println("Unknown message type: " + type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendToClient(String deviceId, String message) {
        Session session = SessionManager.getSession(deviceId);
        if (session != null && session.isOpen()) {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
