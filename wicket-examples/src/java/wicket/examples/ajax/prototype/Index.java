package wicket.examples.ajax.prototype;

import wicket.Component;
import wicket.RequestCycle;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.request.target.ComponentRequestTarget;

/**
 * Example displaying partial page rendering using the counting link
 * example and prototype.js. Prototype.js is a javascript library that provides
 * several handy JavaScript functions, amongst others an Ajax.Updater function,
 * which updates the HTML document with the response of the Ajax call.
 */
public class Index extends WicketExamplePage
{
	/** Counts the number of clicks. */
	private Integer counter = new Integer(0);

	/**
	 * Constructor.
	 */
	public Index()
	{
		// add the Ajaxian link to the page...
		add(new Link("link")
		{
			/**
			 * Handles a click on the link. This method is accessed normally using a standard
			 * http request, but in this example, we use Ajax to perform the call.
			 */
			public void onClick()
			{
				// increase the counter
				counter = new Integer(counter.intValue() + 1);

				// the response should return the label displaying the counter.
				Component component = getPage().get("counter");
				ComponentRequestTarget target = new ComponentRequestTarget(component);
				RequestCycle cycle = RequestCycle.get();
				cycle.setRequestTarget(target);
			}

			/**
			 * Alter the javascript 'onclick' event to emit the Ajax call and update the
			 * counter label.
			 */
			protected String getOnClickScript(String url)
			{
				StringBuffer sb = new StringBuffer();

				sb.append("new Ajax.Updater('counter',");
				sb.append("'" + urlFor(ILinkListener.class) + "',{method:'get'}");
				sb.append(");");

				// always return false, otherwise the submit event gets sent to
				// the server. We are already processing the ajax event.
				sb.append("return false;");

				return sb.toString();
			}
		});
		// add the label
		add(new Label("counter", new PropertyModel(this, "counter")));
	}

	/**
	 * @return Returns the counter.
	 */
	public Integer getCounter()
	{
		return counter;
	}

	/**
	 * @param counter
	 *            The counter to set.
	 */
	public void setCounter(Integer counter)
	{
		this.counter = counter;
	}
}
