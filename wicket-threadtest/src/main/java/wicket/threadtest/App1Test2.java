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
public class App1Test2 {

	private static final Log log = LogFactory.getLog(App1Test2.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> gets = Arrays
				.asList(new String[] { "/app1?wicket:bookmarkablePage=one:wicket.threadtest.apps.app1.Home" });

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 5);

		// getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 100, false);
		tester.run();
	}
}
