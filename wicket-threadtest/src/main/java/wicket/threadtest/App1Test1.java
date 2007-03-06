package wicket.threadtest;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.threadtest.tester.SimpleGetCommand;
import wicket.threadtest.tester.Tester;
import wicket.util.io.WicketObjectStreamFactory;
import wicket.util.lang.Objects;

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
				"/app1/?wicket:bookmarkablePage=:wicket.threadtest.apps.app1.Home",
				"/app1/?wicket:interface=:${iteration}:link::ILinkListener:",
				"/app1/?wicket:interface=:${iteration}:link:1:ILinkListener:",
				"/app1/?wicket:interface=:${iteration}:link:2:ILinkListener:" });

		// you can turn this on if you e.g. want to attach to a profiler
		// Thread.sleep(5000);

		Objects.setObjectStreamFactory(new WicketObjectStreamFactory());
		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 10);
		// getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 1000, true);
		tester.run();
	}
}
