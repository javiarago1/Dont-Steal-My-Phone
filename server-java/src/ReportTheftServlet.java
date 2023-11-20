import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/report-theft")
public class ReportTheftServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String deviceId = req.getParameter("deviceId");
        System.out.println("Theft reported for device ID: " + deviceId);

        MyWebSocketHandler.sendToClient(deviceId, "start_theft_alarm");

        resp.setContentType("text/html");
        resp.getWriter().println("<html><body>");
        resp.getWriter().println("<p>Theft reported for device ID: " + deviceId + ". Actions have been triggered.</p>");
        resp.getWriter().println("<a href='/'>Return to home</a>");
        resp.getWriter().println("</body></html>");
    }
}

