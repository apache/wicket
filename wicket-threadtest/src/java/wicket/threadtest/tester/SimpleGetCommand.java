/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleGetCommand extends AbstractGetCommand {

	private static final Log log = LogFactory.getLog(SimpleGetCommand.class);

	public SimpleGetCommand(List<String> urls, int iterations) {
		super(urls, iterations);
	}

	protected void doGet(String url) throws Exception {

		GetMethod method = new GetMethod(url);
		try {
			int code = getClient().executeMethod(method);
			if (code != 200) {
				log.error("ERROR! code: " + code);
				byte[] body = method.getResponseBody();
				throw new Exception(new String(body));
			}
		} finally {
			method.releaseConnection();
		}
	}
}