import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

import java.io.IOException;

@WebSocket
public class WebClientWebSocketHandler {


    @OnWebSocketConnect
    public void onConnect(Session session) {
        String deviceId = session.getUpgradeRequest().getParameterMap().get("deviceId").get(0);
        System.out.println("New client connected with device ID: " + deviceId);

        if (SessionManager.getAndroidSession(deviceId) == null) {
            try {
                session.getRemote().sendString("{ \"status\": \"error\", \"message\": \"Device ID not found.\" }");
                session.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                session.getRemote().sendString("{ \"status\": \"success\", \"message\": \"Device ID found.\" }");
                startTheftProtocol(deviceId);
                SessionManager.addWebClient(deviceId, session);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        try {
            System.out.println("Cancel at least?");
            JSONObject jsonMessage = new JSONObject(message);
            String command = jsonMessage.getString("command");
            String deviceId = session.getUpgradeRequest().getParameterMap().get("deviceId").get(0);
            switch (command) {
                case "stop_effects":
                    System.out.println("Stop effects!");
                    Session androidSession = SessionManager.getAndroidSession(deviceId);
                    if (androidSession != null && androidSession.isOpen()) {

                        androidSession.getRemote().sendString("stop_effects");
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void startTheftProtocol(String deviceId) {
        AndroidWebSocketHandler.sendToClient(deviceId, "start_theft_alarm");
    }
}

