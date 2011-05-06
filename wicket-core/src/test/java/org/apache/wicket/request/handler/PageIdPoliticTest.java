package org.apache.wicket.request.handler;

import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.IPageManagerContext;
import org.apache.wicket.page.PersistentPageManager;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.versioning.InMemoryPageStore;

/**
 * @author Pedro Santos
 */
public class PageIdPoliticTest extends TestCase
{
	private WicketTester tester;
	private InMemoryPageStore dataStore;
	private MockApplication application;
	private int storeCount;

	/**
	 * Asserting that page don't get touched in an AJAX request that is only repaint its children. <br />
	 * In this case no new page id is being generated and none new page needs to be touched to be
	 * stored again.<br />
	 * By asserting this requirement we improve memory usage and avoid problems of exhaust it.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3667">WICKET-3667</a>
	 */
	public void testPageNotTouchedInAjaxRequest()
	{
		TestPage testPage = new TestPage();
		Url ajaxUrl = Url.parse(testPage.eventBehavior.getCallbackUrl().toString(),
			Charset.forName(tester.getRequest().getCharacterEncoding()));
		tester.startPage(TestPage.class);
		int referenceStoreCount = storeCount;
		tester.executeAjaxUrl(ajaxUrl);
		assertEquals(referenceStoreCount, storeCount);
	}

	@Override
	protected void setUp() throws Exception
	{
		application = new MockApplication();
		dataStore = new InMemoryPageStore()
		{
			@Override
			public void storeData(String sessionId, int pageId, byte[] pageAsBytes)
			{
				super.storeData(sessionId, pageId, pageAsBytes);
				storeCount++;
			}
		};
		tester = new WicketTester(application)
		{
			@Override
			protected IPageManagerProvider newTestPageManagerProvider()
			{
				return new IPageManagerProvider()
				{
					public IPageManager get(IPageManagerContext pageManagerContext)
					{
						IPageStore pageStore = new DefaultPageStore(application.getName(),
							dataStore, 4);
						return new PersistentPageManager(application.getName(), pageStore,
							pageManagerContext);
					}
				};
			};
		};
	}

	@Override
	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/** */
		private static final long serialVersionUID = 1L;
		AjaxEventBehavior eventBehavior;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			WebComponent component;
			component = new WebComponent("component");
			component.add(eventBehavior = new AjaxEventBehavior("onclick")
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
				}
			});
			add(component);
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"component\"></a></body></html>");
		}

	}
}
