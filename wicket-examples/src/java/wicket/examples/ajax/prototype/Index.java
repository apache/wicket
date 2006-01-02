package wicket.examples.ajax.prototype;

import wicket.RequestCycle;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.request.target.ComponentRequestTarget;

/**
 * Example displaying partial page rendering using the counting link
 * example and prototype.js. Prototype.js is a javascript library
 */
public class Index extends WicketExamplePage
{
	/** Counts the number of clicks. */
	private Integer counter = Integer.valueOf(0);

	/**
	 * Constructor.
	 */
	public Index()
	{
		add(new Link("link")
		{
			public void onClick()
			{
				counter = Integer.valueOf(counter.intValue() + 1);

				// set the request target to the label
				ComponentRequestTarget target = new ComponentRequestTarget(getPage().get("counter"));
				RequestCycle cycle = RequestCycle.get();
				cycle.setRequestTarget(target);
			}

			protected String getOnClickScript(String url)
			{
				StringBuilder sb = new StringBuilder();

				sb.append("new Ajax.Updater('counter',");
				sb.append("'" + urlFor(ILinkListener.class) + "',{method:'get'}");
				sb.append(");");
				// always return false, otherwise the submit event gets sent to
				// the server. We
				// are already processing the ajax event.
				sb.append("return false;");

				return sb.toString();
			}
		});
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
