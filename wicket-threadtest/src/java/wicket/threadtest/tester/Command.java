/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

/**
 * Command interface.
 * 
 * @author eelcohillenius
 */
public interface Command {

	/**
	 * Execute one iteration.
	 * 
	 * @param runner
	 *            command runner that executes this command
	 * 
	 * @throws Exception
	 */
	void execute(CommandRunner runner) throws Exception;
}