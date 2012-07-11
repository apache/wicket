import org.apache.wicket.examples.base.ExamplesApplication;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Start {
	public static void main(String[] args) {
		Server server = new Server();

		SelectChannelConnector connector = new SelectChannelConnector();

		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(8080);
		server.setConnectors(new Connector[] { connector });

		/* Setup server (port, etc.) */
		ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS);
		ServletHolder sh = new ServletHolder(WicketServlet.class);
		sh.setInitParameter(ContextParamWebApplicationFactory.APP_CLASS_PARAM, ExamplesApplication.class.getName());
		sh.setInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");

//		/* Define a variable DEV_MODE and set to false
//		 * if wicket should be used in deployment mode
//		 */
//		if(!DEV_MODE) {
//			sh.setInitParameter("wicket.configuration", "deployment");
//		}
		sch.addServlet(sh, "/*");
		server.setHandler(sch);
		try
		{
//			mBeanContainer.start();
			server.start();
			server.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(100);
		}
	}
}
