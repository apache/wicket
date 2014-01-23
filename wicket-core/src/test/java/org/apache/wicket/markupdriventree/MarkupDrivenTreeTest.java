package org.apache.wicket.markupdriventree;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.internal.Enclosure;
import org.apache.wicket.markupdriventree.components.ComponentA;
import org.apache.wicket.markupdriventree.components.ComponentB;
import org.apache.wicket.markupdriventree.components.ComponentC;
import org.apache.wicket.markupdriventree.components.PanelA;
import org.hamcrest.Matchers;
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
	public void pageWithAutoPanel()
	{
		tester.startPage(PageWithAutoPanel.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);

		tester.assertComponent("panelA", PanelA.class);
		tester.assertComponent("panelA:a", ComponentA.class);
	}

	@Test
	public void pageWithManuallyAddedPanel()
	{
		tester.startPage(PageWithManuallyAddedPanel.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);

		tester.assertComponent("panelA", PanelA.class);
		tester.assertComponent("panelA:a", ComponentA.class);
	}

	@Test
	public void pageWithManuallyAddedPanelPlusEnclosure()
	{
		PageWithAutoPanelWithinEnclosure page = tester.startPage(PageWithAutoPanelWithinEnclosure.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);

		Component panelA = page.panelA;
		MarkupContainer parent = panelA.getParent();
		assertThat("PanelA's parent must be the Enclosure", parent, Matchers.instanceOf(Enclosure.class));
	}
}
