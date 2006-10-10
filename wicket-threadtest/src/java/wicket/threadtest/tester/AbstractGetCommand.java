/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.string.Strings;

public abstract class AbstractGetCommand extends AbstractCommand {

	private static final Log log = LogFactory.getLog(AbstractGetCommand.class);

	/**
	 * Construct.
	 * 
	 * @param urls
	 *            URLs to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public AbstractGetCommand(List<String> urls, int iterations) {
		super(urls, iterations);
	}

	/**
	 * @see wicket.threadtest.tester.Command#execute(CommandRunner)
	 */
	public void execute(CommandRunner runner) throws Exception {

		int iterations = getIterations();
		for (int i = 0; i < iterations; i++) {
			List<String> urls = getUrls();
			for (String url : urls) {

				String modUrl = Strings.replaceAll(url, "${iteration}", String.valueOf(i)).toString();
				doGet(runner.getClient(), modUrl);
			}
		}
	}

	/**
	 * Execute a GET request using the provided url.
	 * 
	 * @param url
	 *            The url to GET
	 * @param client
	 *            the http client
	 * @throws Exception
	 */
	protected abstract void doGet(HttpClient client, String url) throws Exception;
}