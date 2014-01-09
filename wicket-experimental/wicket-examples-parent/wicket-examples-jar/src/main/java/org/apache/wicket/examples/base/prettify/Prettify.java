package org.apache.wicket.examples.base.prettify;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Prettify extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final Prettify instance = new Prettify();

	public static Prettify get()
	{
		return instance;
	}

	public static void renderHead(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(Prettify.get()));
		response.render(OnDomReadyHeaderItem.forScript("prettyPrint();"));
	}

	private Prettify()
	{
		super(Prettify.class, "prettify.js");
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(Prettify.class,
				"prettify.css")));
		return dependencies;
	}
}
