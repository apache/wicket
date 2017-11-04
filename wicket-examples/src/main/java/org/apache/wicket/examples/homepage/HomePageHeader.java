package org.apache.wicket.examples.homepage;

import org.apache.wicket.Application;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
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
		
		ExternalLink externalLink = new ExternalLink("headerhomelink",
			Model.of(WebApplication.get().getServletContext().getContextPath()));
		
		externalLink.add(new Image("headerimage", new PackageResourceReference(WicketExamplePage.class, "logo-apachewicket-examples-white.svg")));
		
		add(externalLink);
	}
}
