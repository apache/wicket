package org.apache.wicket.examples.homepage;

import org.apache.wicket.Application;
import org.apache.wicket.examples.resources.ResourcesLocator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;

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
		add(new Image("headerimage", new PackageResourceReference(ResourcesLocator.class, "logo-apachewicket-examples-white.svg")));
	}
}
