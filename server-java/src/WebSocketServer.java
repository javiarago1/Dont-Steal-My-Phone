import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;


public class WebSocketServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(5000);

        // Contexto principal para servir servlets
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Agregar el servlet que maneja el reporte de robo
        context.addServlet(new ServletHolder(new ReportTheftServlet()), "/report-theft");

        // Configurar el WebSocketHandler y agregarlo al contexto
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                // Tiempo de espera de inactividad del socket, en milisegundos.
                factory.getPolicy().setIdleTimeout(10000);
                // Registrar tu clase WebSocket aquí
                factory.register(MyWebSocketHandler.class);
            }
        };
        // Asegúrate de que el contexto maneje los WebSockets también.
        context.setHandler(wsHandler);

        // El manejador de sesiones es necesario si deseas manejar sesiones HTTP
        context.setSessionHandler(new SessionHandler());

        // Establecer el ResourceHandler para servir archivos estáticos.
        // Debe ser una ruta relativa al directorio donde se ejecuta el servidor
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{"report.html"});
        resourceHandler.setResourceBase("src/main/webapp");

        // Agregar los handlers al servidor en orden
        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler); // Primero servir los estáticos
        handlers.addHandler(context); // Luego manejar los servlets y websockets
        server.setHandler(handlers);

        try {
            server.start();
            server.join();
        } finally {
            server.destroy();
        }
    }
}
