package wicket.threadtest;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.threadtest.tester.SimpleGetCommand;
import wicket.threadtest.tester.Tester;

/**
 * @author eelcohillenius
 */
public class App1Test {

	private static final Log log = LogFactory.getLog(App1Test.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> gets = Arrays.asList(new String[] {
				"http://localhost:8090/app1?wicket:bookmarkablePage=one:wicket.threadtest.apps.app1.Home",
				"http://localhost:8090/app1?wicket:bookmarkablePage=two:wicket.threadtest.apps.app1.Home",
				"http://localhost:8090/app1?wicket:interface=two:${iteration}:link::ILinkListener",
				"http://localhost:8090/app1?wicket:interface=one:${iteration}:link::ILinkListener",
				"http://localhost:8090/app1?wicket:interface=two:${iteration}:link::ILinkListener" });

		Tester tester = new Tester(new SimpleGetCommand(gets, 10), 8090, 10);
		tester.run();
	}
}
