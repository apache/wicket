package wicket.threadtest.tester;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs a command.
 * 
 * @author eelcohillenius
 */
public class CommandRunner implements Runnable {

	private static final Log log = LogFactory.getLog(CommandRunner.class);

	private final List<Command> commands;

	/**
	 * Construct.
	 * 
	 * @param commands
	 */
	public CommandRunner(List<Command> commands) {
		this.commands = commands;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		for (Command command : commands) {
			try {
				command.execute();
			} catch (Exception e) {
				log.fatal("execution of command " + command + ", thread " + Thread.currentThread() + " failed", e);
				return;
			} finally {
				command.release();
			}
		}
	}
}