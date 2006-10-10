package wicket.threadtest.tester;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author eelcohillenius
 */
public class ThreadingTest {

	private static final Log log = LogFactory.getLog(ThreadingTest.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> gets = Arrays.asList(new String[] {
				"http://localhost:8090/app?wicket:bookmarkablePage=one:wicket.threadtest.apps.Home",
				"http://localhost:8090/app?wicket:bookmarkablePage=two:wicket.threadtest.apps.Home",
				"http://localhost:8090/app?wicket:interface=two:${iteration}:link::ILinkListener",
				"http://localhost:8090/app?wicket:interface=one:${iteration}:link::ILinkListener",
				"http://localhost:8090/app?wicket:interface=two:${iteration}:link::ILinkListener" });

		ThreadedTester tester = new ThreadedTester(new SimpleGetCommand(gets, 10), 10);
		tester.run();
	}
}
