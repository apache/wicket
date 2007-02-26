/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import wicket.threadtest.tester.CommandRunner.CommandRunnerObserver;

/**
 * @author eelcohillenius
 */
public final class Tester implements CommandRunnerObserver {

	private static final Log log = LogFactory.getLog(Tester.class);

	private static HttpClientParams params;

	static {
		params = new HttpClientParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
	}

	/**
	 * Main method for just starting the server
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// start server on its own
		int port = 8090;
		if (args.length > 0) {
			port = Integer.valueOf(args[0]);
		}
		Server server = startServer(port);
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				server.stop();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.exit(1);
		}
	}

	/**
	 * Start Jetty server instance and return the handle.
	 * 
	 * @param port
	 * @return server handle
	 */
	private static Server startServer(int port) {
		Server server;
		// start up server
		server = new Server(port);
		WebAppContext ctx = new WebAppContext("./src/main/webapp", "/");
		server.addHandler(ctx);
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return server;
	}

	private int activeThreads = 0;

	private final List<Command> commands;

	private String host = "localhost";

	/**
	 * if true, each thread will represent a seperate session. If false, the
	 * test behaves like one client issuing multiple concurrent requests.
	 */
	private final boolean multipleSessions;

	private final int numberOfThreads;

	private int port = 8090;

	/**
	 * Construct.
	 * 
	 * @param command
	 *            Command to execute
	 * @param numberOfThreads
	 *            Number of threads to run the commands. Each thread runs all
	 *            commands
	 * @param multipleSessions
	 *            if true, each thread will represent a seperate session. If
	 *            false, the test behaves like one client issuing multiple
	 *            concurrent requests
	 */
	public Tester(Command command, int numberOfThreads, boolean multipleSessions) {
		this(Arrays.asList(new Command[] { command }), numberOfThreads, multipleSessions);
	}

	/**
	 * Construct.
	 * 
	 * @param commands
	 *            Commands to execute
	 * @param numberOfThreads
	 *            Number of threads to run the commands. Each thread runs all
	 *            commands
	 * @param multipleSessions
	 *            if true, each thread will represent a seperate session. If
	 *            false, the test behaves like one client issuing multiple
	 *            concurrent requests
	 */
	public Tester(List<Command> commands, int numberOfThreads, boolean multipleSessions) {
		this.commands = commands;
		this.numberOfThreads = numberOfThreads;
		this.multipleSessions = multipleSessions;
	}

	/**
	 * Gets host.
	 * 
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Gets port.
	 * 
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	public synchronized void onDone(CommandRunner runner) {
		activeThreads--;
		notifyAll();
	}

	public synchronized void onError(CommandRunner runner, Exception e) {
		activeThreads--;
		notifyAll();
	}

	/**
	 * Runs the test.
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {

		activeThreads = 0;

		HttpConnectionManagerParams connManagerParams = new HttpConnectionManagerParams();
		connManagerParams.setDefaultMaxConnectionsPerHost(numberOfThreads * 2);
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		manager.setParams(connManagerParams);

		Server server = null;
		GetMethod getMethod = new GetMethod("http://localhost:" + port + "/");
		try {
			getMethod.setFollowRedirects(true);
			HttpClient httpClient = new HttpClient(params, manager);
			int code = httpClient.executeMethod(getMethod);
			if (code != 200) {
				server = startServer(port);
			}
		} catch (Exception e) {
			server = startServer(port);
		} finally {
			getMethod.releaseConnection();
		}

		try {

			ThreadGroup g = new ThreadGroup("runners");
			Thread[] threads = new Thread[numberOfThreads];
			HttpClient client = null;
			for (int i = 0; i < numberOfThreads; i++) {

				if (multipleSessions) {
					client = new HttpClient(params, manager);
					client.getHostConfiguration().setHost(host, port);
				} else {
					if (client == null) {
						client = new HttpClient(params, manager);
						client.getHostConfiguration().setHost(host, port);
					}
				}
				threads[i] = new Thread(g, new CommandRunner(commands, client, this));
			}

			long start = System.currentTimeMillis();

			for (int i = 0; i < numberOfThreads; i++) {
				activeThreads++;
				threads[i].start();
			}

			while (activeThreads > 0) {
				synchronized (this) {
					wait();
				}
			}

			long end = System.currentTimeMillis();
			log.info("\n******** finished in " + (end - start) + " miliseconds\n");

		} finally {
			MultiThreadedHttpConnectionManager.shutdownAll();
			if (server != null) {
				server.stop();
			}
		}
	}

	/**
	 * Sets host.
	 * 
	 * @param host
	 *            host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Sets port.
	 * 
	 * @param port
	 *            port
	 */
	public void setPort(int port) {
		this.port = port;
	}
}
