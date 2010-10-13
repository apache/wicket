package org.apache.wicket;

import junit.framework.TestCase;

import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;


public class BehaviorUrlTest extends TestCase
{
	/**
	 * @see https://issues.apache.org/jira/browse/WICKET-3097
	 */
	public void testUrlRemainsStable()
	{
		WicketTester tester = new WicketTester();

		TestPage page = new TestPage();

		int indexBeforeRender = page.container.getBehaviorsRawList().indexOf(page.callbackBehavior);

		tester.startPage(page);

		page = (TestPage)tester.getLastRenderedPage();
		int indexAfterRender = page.container.getBehaviorsRawList().indexOf(page.callbackBehavior);

		assertEquals("index of behavior in the raw list should not have changed",
			indexBeforeRender, indexAfterRender);

	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private WebMarkupContainer container;
		private TestCallbackBehavior callbackBehavior;

		public TestPage()
		{
			callbackBehavior = new TestCallbackBehavior();

			container = new WebMarkupContainer("container");
			container.add(new TestTemporaryBehavior());
			container.add(callbackBehavior);
			add(container);

		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><a wicket:id=\"container\">container</a></html>");
		}
	}

	private static class TestTemporaryBehavior extends AbstractBehavior
	{
		@Override
		public boolean isTemporary()
		{
			return true;
		}
	}

	private static class TestCallbackBehavior extends AbstractBehavior implements IBehaviorListener
	{
		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			super.onComponentTag(component, tag);
			tag.put("href", component.urlFor(this, IBehaviorListener.INTERFACE));
		}

		public void onRequest()
		{
		}
	}
}