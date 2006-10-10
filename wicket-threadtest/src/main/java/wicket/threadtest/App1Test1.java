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
public class App1Test1 {

	private static final Log log = LogFactory.getLog(App1Test1.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> gets = Arrays.asList(new String[] {
				"/app1?wicket:bookmarkablePage=one:wicket.threadtest.apps.app1.Home",
				"/app1?wicket:bookmarkablePage=two:wicket.threadtest.apps.app1.Home",
				"/app1?wicket:interface=two:${iteration}:link::ILinkListener",
				"/app1?wicket:interface=one:${iteration}:link::ILinkListener",
				"/app1?wicket:interface=two:${iteration}:link::ILinkListener" });

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 1);
		//getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 50, true);
		tester.run();
	}
}
