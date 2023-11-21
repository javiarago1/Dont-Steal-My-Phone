import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static SessionManager instance;
    private static final Map<String, Session> androidSessions = new ConcurrentHashMap<>();

    private static final Map<String, Session> webSessions = new ConcurrentHashMap<>();


    public static Map<String, Session> getSessions(){
        return androidSessions;
    }


    public static void addSession(String deviceId, Session session) {
        androidSessions.put(deviceId, session);
    }


    public static void removeSessionByValue(String deviceId) {
        androidSessions.remove(deviceId);
    }

    public static Session getAndroidSession(String deviceId) {
        return androidSessions.get(deviceId);
    }

    public static void removeWebClientById(String deviceId){
        webSessions.remove(deviceId);
    }

    public static void addWebClient(String deviceId, Session session){
        webSessions.put(deviceId, session);
    }

    public static void updateLocationToWebClient(String clientId, String coords){
        Session session = webSessions.get(clientId);
        if (session == null) return ;
        try {
            session.getRemote().sendString(coords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
