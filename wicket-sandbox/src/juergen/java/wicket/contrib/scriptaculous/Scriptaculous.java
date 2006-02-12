/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.contrib.scriptaculous;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.internal.HtmlHeaderContainer;

/**
 * Indicator panel.
 */
public class Scriptaculous 
{
	private static final Scriptaculous scriptaculuous = new Scriptaculous(); 
	
	private final PackageResourceReference refPrototype;
	private final PackageResourceReference refScriptaculous;
	private final PackageResourceReference refBehavior;
	
	/**
	 * 
	 * Construct.
	 */
	private Scriptaculous()
	{
    	refPrototype = new PackageResourceReference(Application.get(), Indicator.class, "prototype.js");
    	refScriptaculous = new PackageResourceReference(Application.get(), Indicator.class, "scriptaculous.js");
    	refBehavior = new PackageResourceReference(Application.get(), Indicator.class, "behavior.js");
    	
        PackageResource.bind(Application.get(), Indicator.class, "controls.js");
        PackageResource.bind(Application.get(), Indicator.class, "dragdrop.js");
        PackageResource.bind(Application.get(), Indicator.class, "effects.js");
        PackageResource.bind(Application.get(), Indicator.class, "builder.js");
        PackageResource.bind(Application.get(), Indicator.class, "slider.js");
        PackageResource.bind(Application.get(), Indicator.class, "unittest.js");
        PackageResource.bind(Application.get(), Indicator.class, "util.js");
	}
	
	public static final Scriptaculous get()
	{
		return scriptaculuous;
	}
	
	public void renderHead(HtmlHeaderContainer container)
	{
		addJavascriptReference(container, refPrototype);
		addJavascriptReference(container, refScriptaculous);
		addJavascriptReference(container, refBehavior);
	}

	private void addJavascriptReference(final HtmlHeaderContainer container, final PackageResourceReference ref)
	{
		write(container, "\t<script language='JavaScript' type='text/javascript' " + "src='" + container.urlFor(ref.getPath()) + "'></script>\n");
	}
	
	/**
	 * Writes the given string to the header container.
	 * 
	 * @param container
	 *            the header container
	 * @param s
	 *            the string to write
	 */
	protected void write(MarkupContainer container, String s)
	{
		container.getResponse().write(s);
	}
}
