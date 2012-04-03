package org.apache.wicket.atmosphere;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;

public class JQueryWicketAtmosphereResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	public JQueryWicketAtmosphereResourceReference()
	{
		super(JQueryWicketAtmosphereResourceReference.class, "jquery.wicketatmosphere.js");
	}

	@Override
	public Iterable< ? extends HeaderItem> getDependencies()
	{
		return Arrays.asList(JavaScriptHeaderItem.forReference(new JQueryPluginResourceReference(
			JQueryWicketAtmosphereResourceReference.class, "jquery.atmosphere.js")));
	}
}
