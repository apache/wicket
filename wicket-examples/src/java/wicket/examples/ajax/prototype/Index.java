package wicket.examples.ajax.prototype;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.request.target.ComponentRequestTarget;

/**
 * Example displaying partial page rendering using the counting link example and
 * prototype.js. Prototype.js is a javascript library that provides several
 * handy JavaScript functions, amongst others an Ajax.Updater function, which
 * updates the HTML document with the response of the Ajax call.
 */
public class Index extends WicketExamplePage
{
	/** Click count. */
	private int count = 0;

	/** Label showing count */
	private final Label counter;

	/**
	 * Constructor.
	 */
	public Index()
	{
		// Add the Ajaxian link to the page...
		add(new Link("link")
		{
			/**
			 * Handles a click on the link. This method is accessed normally
			 * using a standard http request, but in this example, we use Ajax
			 * to perform the call.
			 */
			public void onClick()
			{
				// Increment count
				count++;

				// The response should refresh the label displaying the counter.
				getRequestCycle().setRequestTarget(new ComponentRequestTarget(counter));
			}

			/**
			 * Alter the javascript 'onclick' event to emit the Ajax call and
			 * update the counter label.
			 */
			protected String getOnClickScript(String url)
			{
				return "new Ajax.Updater('counter', '" + urlFor(ILinkListener.class)
						+ "', {method:'get'}); return false;";
			}
		});

		// Add the label
		add(counter = new Label("counter", new PropertyModel(this, "count")));
	}

	/**
	 * @return Returns the count.
	 */
	public int getCount()
	{
		return count;
	}
}
