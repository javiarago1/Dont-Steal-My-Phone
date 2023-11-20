import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static SessionManager instance;
    private static final Map<String, Session> clientSessions = new ConcurrentHashMap<>();

    private SessionManager() {
        // Constructor privado para evitar la creación de instancias
    }


    public static Map<String, Session> getSessions(){
        return clientSessions;
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public static void addSession(String deviceId, Session session) {
        clientSessions.put(deviceId, session);
    }


    public static void removeSessionByValue(String deviceId) {
        clientSessions.remove(deviceId);
    }

    public static Session getSession(String deviceId) {
        return clientSessions.get(deviceId);
    }



}
