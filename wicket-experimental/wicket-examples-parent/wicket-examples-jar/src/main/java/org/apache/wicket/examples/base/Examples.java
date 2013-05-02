package org.apache.wicket.examples.base;

import java.util.List;

import org.apache.wicket.bootstrap.Bootstrap;
import org.apache.wicket.examples.base.prettify.Prettify;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Examples extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	private static final Examples instance = new Examples();

	public static Examples get() {
		return instance;
	}

	public static void renderHead(IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(Examples.get()));
	}

	private Examples() {
		super(Examples.class, "examples.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		HeaderItem bootstrap = JavaScriptHeaderItem.forReference(Bootstrap
				.responsive());
		HeaderItem prettify = JavaScriptHeaderItem.forReference(Prettify.get());
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(bootstrap);
		dependencies.add(prettify);
		return dependencies;
	}
}
