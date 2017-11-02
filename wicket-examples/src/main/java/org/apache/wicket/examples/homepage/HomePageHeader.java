package org.apache.wicket.examples.homepage;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class HomePageHeader extends Panel {

	public HomePageHeader(String id) 
	{
		super(id);
		
		add(new Label("version", () -> 
		{
			/*
			 * Read the specification version from the wicket-core MANIFEST.MF file.
			 */
			Package p = Application.class.getPackage();

			String version = p.getSpecificationVersion();

			if (version == null || version.length() == 0)
			{
				return "Missing Version";
			}
			else
			{
				return version;
			}
		}));
	}
}
