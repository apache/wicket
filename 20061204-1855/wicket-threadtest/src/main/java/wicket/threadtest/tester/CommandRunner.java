package wicket.threadtest.tester;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs a command.
 * 
 * @author eelcohillenius
 */
public class CommandRunner implements Runnable {

	public static interface CommandRunnerObserver {

		void onDone(CommandRunner runner);

		void onError(CommandRunner runner, Exception e);
	}

	private static final Log log = LogFactory.getLog(CommandRunner.class);

	private HttpClient client;

	private final List<Command> commands;

	private final CommandRunnerObserver observer;

	/**
	 * Construct.
	 * 
	 * @param commands
	 * @param client
	 */
	public CommandRunner(List<Command> commands, HttpClient client, CommandRunnerObserver observer) {
		this.commands = commands;
		this.client = client;
		this.observer = observer;
	}

	/**
	 * Gets the HTTP client.
	 * 
	 * @return the HTTP client
	 */
	public HttpClient getClient() {
		return this.client;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		for (Command command : commands) {
			try {
				command.execute(this);
			} catch (Exception e) {
				log.fatal("execution of command " + command + ", thread " + Thread.currentThread() + " failed", e);
				observer.onError(this, e);
				return;
			}
		}
		observer.onDone(this);
	}
}