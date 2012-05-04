package org.apache.wicket.atmosphere;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;

/**
 * Resource reference for the jquery.atmosphere.js module and the wicket glue.
 * 
 * @author papegaaij
 */
public class JQueryWicketAtmosphereResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final JQueryWicketAtmosphereResourceReference INSTANCE = new JQueryWicketAtmosphereResourceReference();

	/**
	 * @return the singleton instance of this resource reference.
	 */
	public static JQueryWicketAtmosphereResourceReference get()
	{
		return INSTANCE;
	}

	private JQueryWicketAtmosphereResourceReference()
	{
		super(JQueryWicketAtmosphereResourceReference.class, "jquery.wicketatmosphere.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies()
	{
		return Arrays.asList(JavaScriptHeaderItem.forReference(new JQueryPluginResourceReference(
			JQueryWicketAtmosphereResourceReference.class, "jquery.atmosphere.js")));
	}
}
