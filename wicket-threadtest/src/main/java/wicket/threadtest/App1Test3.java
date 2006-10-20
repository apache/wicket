package wicket.threadtest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.threadtest.apps.app1.ResourceTestPage;
import wicket.threadtest.tester.SimpleGetCommand;
import wicket.threadtest.tester.Tester;

/**
 * @author almaw
 */
public class App1Test3 {

	private static final Log log = LogFactory.getLog(App1Test3.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> gets = new ArrayList<String>();
		gets.add("/app1?wicket:bookmarkablePage=one:wicket.threadtest.apps.app1.ResourceTestPage");
		for (int i = 0; i < ResourceTestPage.IMAGES_PER_PAGE; i++) {
			gets.add("/app1?wicket:interface=one:${iteration}:listView:" + i + ":image::IResourceListener");
		}

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 5);

		// getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 100, false);
		tester.run();
	}
}
