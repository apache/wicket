package org.apache.wicket.markupdriventree;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.junit.Test;

/**
 *
 */
public class MarkupDrivenTreeTest extends WicketTestCase
{
	@Test
	public void page1()
	{
		tester.startPage(Page1.class);

		tester.assertComponent("a", WebMarkupContainer.class);
		tester.assertComponent("a:b", WebMarkupContainer.class);
		tester.assertComponent("a:b:c", WebMarkupContainer.class);
	}

	@Test
	public void page2()
	{
		tester.startPage(Page2.class);

		tester.assertComponent("c", WebMarkupContainer.class);
		tester.assertComponent("c:b", WebMarkupContainer.class);
		tester.assertComponent("c:b:a", WebMarkupContainer.class);
	}

	@Test
	public void page3()
	{
		tester.startPage(Page3.class);

		tester.assertComponent("c", WebMarkupContainer.class);
		tester.assertComponent("b", WebMarkupContainer.class);
		tester.assertComponent("b:a", WebMarkupContainer.class);
	}
}
