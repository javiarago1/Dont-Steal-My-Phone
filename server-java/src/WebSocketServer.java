import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebSocketServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(5000);

        // Handler para Android WebSockets en "/android"
        WebSocketHandler androidWsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(3600000);
                factory.register(AndroidWebSocketHandler.class);
            }
        };

        // Handler para Web Client WebSockets en "/web"
        WebSocketHandler webWsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(3600000);
                factory.register(WebClientWebSocketHandler.class);
            }
        };

        // Configurar los contextos para cada handler
        HandlerList handlers = new HandlerList();

        // Contexto para Android WebSockets
        ServletContextHandler androidContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        androidContext.setContextPath("/android");
        androidContext.setHandler(androidWsHandler);
        handlers.addHandler(androidContext);

        // Contexto para Web Client WebSockets
        ServletContextHandler webContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        webContext.setContextPath("/web");
        webContext.setHandler(webWsHandler);
        handlers.addHandler(webContext);

        // Configura el server con ambos handlers
        server.setHandler(handlers);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.destroy();
        }
    }
}
