package wicket.session;

import wicket.AbstractRestartResponseException;
import wicket.IPageFactory;
import wicket.PageParameters;
import wicket.WicketTestCase;
import wicket.markup.html.WebPage;

/**
 * Default page facotry tests
 * 
 * @author ivaynberg
 */
public class DefaultPageFactoryTest extends WicketTestCase
{
	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage1 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public AbortAndRespondPage1()
		{
			throw new AbstractRestartResponseException()
			{
				private static final long serialVersionUID = 1L;
			};
		}
	}

	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage2 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param params
		 */
		public AbortAndRespondPage2(PageParameters params)
		{
			throw new AbstractRestartResponseException()
			{
				private static final long serialVersionUID = 1L;
			};
		}
	}

	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage3 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public AbortAndRespondPage3()
		{
			throw new AbstractRestartResponseException()
			{
				private static final long serialVersionUID = 1L;
			};
		}

		/**
		 * Construct.
		 * 
		 * @param params
		 */
		public AbortAndRespondPage3(PageParameters params)
		{
			throw new AbstractRestartResponseException()
			{
				private static final long serialVersionUID = 1L;
			};
		}
	}

	final private IPageFactory pageFactory = new DefaultPageFactory();

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public DefaultPageFactoryTest(String name)
	{
		super(name);
	}

	/**
	 * Verifies page factory bubbles AbortAndRespondException
	 */
	public void testAbortAndRespondContract()
	{
		tester.setupRequestAndResponse();
		tester.createRequestCycle();
		try
		{
			pageFactory.newPage(AbortAndRespondPage1.class);
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage2.class);
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage2.class, new PageParameters());
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage3.class);
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage3.class, new PageParameters());
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}
	}
}
