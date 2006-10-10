/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleGetCommand extends AbstractGetCommand {

	private static final Log log = LogFactory.getLog(SimpleGetCommand.class);

	private boolean printResponse = false;

	/**
	 * Construct.
	 * 
	 * @param urls
	 *            URLs to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public SimpleGetCommand(List<String> urls, int iterations) {
		super(urls, iterations);
	}

	/**
	 * Construct.
	 * 
	 * @param url
	 *            URL to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public SimpleGetCommand(String url, int iterations) {
		super(Arrays.asList(new String[] { url }), iterations);
	}

	/**
	 * Gets printResponse.
	 * 
	 * @return printResponse
	 */
	public boolean getPrintResponse() {
		return printResponse;
	}

	/**
	 * Sets printResponse.
	 * 
	 * @param printResponse
	 *            printResponse
	 */
	public void setPrintResponse(boolean printResponse) {
		this.printResponse = printResponse;
	}

	/**
	 * @see wicket.threadtest.tester.AbstractGetCommand#doGet(org.apache.commons.httpclient.HttpClient,
	 *      java.lang.String)
	 */
	@Override
	protected void doGet(HttpClient client, String url) throws Exception {

		GetMethod method = new GetMethod(url);
		method.setFollowRedirects(true);
		try {
			int code = client.executeMethod(method);
			if (code != 200) {
				log.error("ERROR! code: " + code);
				throw new Exception(new String(method.getResponseBody()));
			}
			if (getPrintResponse()) {
				log.info("\n" + new String(method.getResponseBody()));
			}
		} finally {
			method.releaseConnection();
		}
	}
}