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
public class App2Test1 {

	private static final Log log = LogFactory.getLog(App2Test1.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> gets = Arrays
				.asList(new String[] { "/app2?wicket:bookmarkablePage=one:wicket.threadtest.apps.app2.Home" });

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 5);

		// getCmd.setPrintResponse(true);
		
		// AS OF OCTOBER 9 2006, THIS TYPICALLY RESULTS IN A DEADLOCK
		Tester tester = new Tester(getCmd, 50, false);
		
		// new Tester(.., .., false) would not give a deadlock, as then
		// all threads point to seperate sessions
		
		tester.run();
	}
}
