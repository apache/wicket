package org.apache.wicket.request.cycle;

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.cycle.RerenderPage.Supplier;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for re-rendering pages.
 */
public class RerenderPageTest extends WicketTestCase
{
	/**
	 * A testcase for WICKET-5960.
	 * 
	 * Due to the changes in WICKET-5309, a page is re-rendered when any of the URL segments is
	 * modified during the request. The re-render causes the {@code <head>} section to be empty
	 * because it was already rendered in the first try.
	 */
	@Test
	@Ignore("Committed reproduction case, but added disabled to not fail the build")
	public void wicket5960()
	{
		// mount the page so we have URL segments
		tester.getApplication().mount(new MountedMapper("/rerender/${value}", RerenderPage.class));

		// start the page with a value of 1
		PageParameters pars = new PageParameters();
		pars.add("value", 1);

		// render the page
		RerenderPage page = tester.startPage(RerenderPage.class, pars);
		tester.assertRenderedPage(RerenderPage.class);
		tester.assertContains("<!-- I should be present 1 -->");

		// add a supplier to modify the URL during render
		page.setNewValueHandler(new Supplier<Integer>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Integer get()
			{
				return 2;
			}
		});

		// rerender the page
		tester.startPage(page);
		tester.assertRenderedPage(RerenderPage.class);

		// due to the mentioned issue, no headers are rendered at all.
		tester.assertContains("<!-- I should be present 2 -->");
	}
}
