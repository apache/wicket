/*
 * Copyright Teachscape
 */
package wicket.threadtest.tester;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractCommand implements Command {

	private static final Log log = LogFactory.getLog(AbstractCommand.class);

	private static HttpClientParams params;
	static {
		params = new HttpClientParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
	}

	private ThreadLocal<HttpClient> CURRENT_CLIENT = new ThreadLocal<HttpClient>() {
		@Override
		protected HttpClient initialValue() {
			HttpClient client = new HttpClient(params);
			return client;
		}
	};

	private final int iterations;

	private final List<String> urls;

	public AbstractCommand(List<String> urls, int iterations) {
		this.urls = urls;
		this.iterations = iterations;
	}

	/**
	 * Gets client.
	 * 
	 * @return client
	 */
	public HttpClient getClient() {
		return CURRENT_CLIENT.get();
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

	public void release() {
		CURRENT_CLIENT.remove();
	}
}