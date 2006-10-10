/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractCommand implements Command {

	private static final Log log = LogFactory.getLog(AbstractCommand.class);

	/** number of executions of the urls. */
	private final int iterations;

	/** URLs to visit. */
	private final List<String> urls;

	/**
	 * Construct.
	 * 
	 * @param urls
	 *            URLs to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public AbstractCommand(List<String> urls, int iterations) {
		this.urls = urls;
		this.iterations = iterations;
	}

	/**
	 * Gets iterations.
	 * 
	 * @return iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * Gets urls.
	 * 
	 * @return urls
	 */
	public List<String> getUrls() {
		return urls;
	}
}