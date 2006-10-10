package wicket.threadtest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author eelcohillenius
 */
public class ThreadingTest {

	private static final Log log = LogFactory.getLog(ThreadingTest.class);

	private static class PageRunner implements Runnable {

		private final HttpClient client;

		public PageRunner() {
			HttpClientParams params = new HttpClientParams();
			params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
			client = new HttpClient(params);
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {

			try {

				for (int i = 0; i < 10; i++) {

					doGet("http://localhost:8090/app?wicket:bookmarkablePage=one:wicket.threadtest.Home");
					doGet("http://localhost:8090/app?wicket:bookmarkablePage=two:wicket.threadtest.Home");
					doGet("http://localhost:8090/app?wicket:interface=two:" + i + ":link::ILinkListener");
					doGet("http://localhost:8090/app?wicket:interface=one:" + i + ":link::ILinkListener");
					doGet("http://localhost:8090/app?wicket:interface=two:" + i + ":link::ILinkListener");
				}

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		private void doGet(String url) throws Exception {

			GetMethod method = new GetMethod(url);
			try {
				int code = client.executeMethod(method);
				if (code != 200) {
					log.error("ERROR! code: " + code);
					byte[] body = method.getResponseBody();
					throw new Exception(new String(body));
				}
			} finally {
				method.releaseConnection();
			}
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Server server = new Server(8090);
		WebAppContext ctx = new WebAppContext("./src/webapp", "/app");
		server.addHandler(ctx);
		server.start();

		long start = System.currentTimeMillis();
		ThreadGroup g = new ThreadGroup("runners");
		for (int i = 0; i < 20; i++) {
			Thread t = new Thread(g, new PageRunner());
			t.start();
		}

		while (g.activeCount() > 0) {
			Thread.yield();
		}

		long end = System.currentTimeMillis();
		log.info("\n******** finished in " + (end - start) + " miliseconds\n");

		server.stop();
	}
}
