import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebSocketServer {
    public static void main(String[] args) {
        Server server = new Server(5000);

        WebSocketHandler androidWsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(3600000);
                factory.register(AndroidWebSocketHandler.class);
            }
        };

        WebSocketHandler webWsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.getPolicy().setIdleTimeout(3600000);
                factory.register(WebClientWebSocketHandler.class);
            }
        };

        HandlerList handlers = new HandlerList();

        ServletContextHandler androidContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        androidContext.setContextPath("/android");
        androidContext.setHandler(androidWsHandler);
        handlers.addHandler(androidContext);

        ServletContextHandler webContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        webContext.setContextPath("/web");
        webContext.setHandler(webWsHandler);
        handlers.addHandler(webContext);

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
