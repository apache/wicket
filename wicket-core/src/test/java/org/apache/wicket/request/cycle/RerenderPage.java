package org.apache.wicket.request.cycle;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;

@SuppressWarnings("javadoc")
public class RerenderPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public static abstract class Supplier<T> implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public abstract T get();
	}

	private Integer newValue = 1;

	private Supplier<Integer> handler = null;

	public RerenderPage(PageParameters pars)
	{
		super(pars);
		this.newValue = pars.get("value").toInteger();
		
		// make page statefull
		add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_DAY));
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		if (handler != null)
			setNewValue(handler.get());
	}

	private void setNewValue(Integer newValue)
	{
		this.newValue = newValue;
		getPageParameters().set("value", newValue);
	}

	public void setNewValueHandler(Supplier<Integer> s)
	{
		this.handler = s;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(new StringHeaderItem("<!-- I should be present " + newValue + " -->"));
	}
}
