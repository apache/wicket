/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author eelcohillenius
 */
public final class ThreadedTester {

	private static final Log log = LogFactory.getLog(App1Test.class);

	private final List<Command> commands;

	private final int numberOfThreads;

	/**
	 * Construct.
	 * 
	 * @param command
	 *            Command to execute
	 * @param numberOfThreads
	 *            Number of threads to run the commands. Each thread runs all
	 *            commands
	 */
	public ThreadedTester(Command command, int numberOfThreads) {
		this(Arrays.asList(new Command[] { command }), numberOfThreads);
	}

	/**
	 * Construct.
	 * 
	 * @param commands
	 *            Commands to execute
	 * @param numberOfThreads
	 *            Number of threads to run the commands. Each thread runs all
	 *            commands
	 */
	public ThreadedTester(List<Command> commands, int numberOfThreads) {
		this.commands = commands;
		this.numberOfThreads = numberOfThreads;
	}

	/**
	 * Runs the test.
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {

		Server server = new Server(8090);
		WebAppContext ctx = new WebAppContext("./src/webapp", "/");
		server.addHandler(ctx);
		server.start();

		try {
			long start = System.currentTimeMillis();
			ThreadGroup g = new ThreadGroup("runners");
			for (int i = 0; i < numberOfThreads; i++) {
				Thread t = new Thread(g, new CommandRunner(commands));
				t.start();
			}

			while (g.activeCount() > 0) {
				Thread.yield();
			}

			long end = System.currentTimeMillis();
			log.info("\n******** finished in " + (end - start) + " miliseconds\n");

		} finally {
			server.stop();
		}
	}
}
