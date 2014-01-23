package org.apache.wicket.markupdriventree;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markupdriventree.components.ComponentA;
import org.apache.wicket.markupdriventree.components.ComponentB;
import org.apache.wicket.markupdriventree.components.ComponentC;
import org.apache.wicket.markupdriventree.components.PanelA;
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

		tester.assertComponent("a", ComponentA.class);
		tester.assertComponent("a:b", ComponentB.class);
		tester.assertComponent("a:b:c", ComponentC.class);
	}

	@Test
	public void page2()
	{
		tester.startPage(Page2.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("c:b", ComponentB.class);
		tester.assertComponent("c:b:a", ComponentA.class);
	}

	@Test
	public void page3()
	{
		tester.startPage(Page3.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);
	}

	@Test
	public void pageWithPanel()
	{
		tester.startPage(PageWithPanel.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);

		tester.assertComponent("panelA", PanelA.class);
		tester.assertComponent("panelA:a", ComponentA.class);
	}
}
