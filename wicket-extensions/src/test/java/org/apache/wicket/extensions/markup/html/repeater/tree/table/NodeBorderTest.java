package org.apache.wicket.extensions.markup.html.repeater.tree.table;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * Test for {@link NodeBorder}.
 */
public class NodeBorderTest
{

	/**
	 * WICKET-5447
	 */
	@Test
	public void properlyClosed() throws Exception
	{
		WicketTester tester = new WicketTester();

		Label label = new Label("label");
		label.add(new NodeBorder(new boolean[] { true, false, true }));

		tester.startComponentInPage(label);

		tester
			.assertResultPage("<div class=\"tree-branch tree-branch-mid\"><div class=\"tree-subtree\"><div class=\"tree-branch tree-branch-last\"><div class=\"tree-subtree\"><div class=\"tree-branch tree-branch-mid\"><span wicket:id=\"label\" class=\"tree-node\"></span></div></div></div></div></div>");
	}
}
