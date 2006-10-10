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
	 * @throws Exception
	 */
	void execute() throws Exception;

	/**
	 * Clean up; called by the runner.
	 */
	void release();
}