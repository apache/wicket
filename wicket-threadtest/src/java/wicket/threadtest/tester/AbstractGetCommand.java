/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.string.Strings;

public abstract class AbstractGetCommand extends AbstractCommand {

	private static final Log log = LogFactory.getLog(AbstractGetCommand.class);

	public AbstractGetCommand(List<String> urls, int iterations) {
		super(urls, iterations);
	}

	/**
	 * @see wicket.threadtest.tester.Command#execute()
	 */
	public void execute() throws Exception {

		int iterations = getIterations();
		for (int i = 0; i < iterations; i++) {
			List<String> urls = getUrls();
			for (String url : urls) {

				String modUrl = Strings.replaceAll(url, "${iteration}", String.valueOf(i)).toString();
				doGet(modUrl);
			}
		}
	}

	/**
	 * Execute a GET request using the provided url.
	 * 
	 * @param url
	 * @throws Exception
	 */
	protected abstract void doGet(String url) throws Exception;
}