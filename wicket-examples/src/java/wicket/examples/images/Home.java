/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:57:30 +0200 (vr, 26 mei 2006) $
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
package wicket.examples.images;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import wicket.Resource;
import wicket.ResourceReference;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.image.Image;
import wicket.markup.html.image.NonCachingImage;
import wicket.markup.html.image.resource.BufferedDynamicImageResource;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.markup.html.image.resource.RenderedDynamicImageResource;
import wicket.model.Model;

/**
 * Demonstrates different flavors of wicket.examples.images.
 * 
 * @author Jonathan Locke
 */
public final class Home extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public Home()
	{
		// Image as package resource
		new Image(this, "image2");

		// Dynamically created image. Will re-render whenever resource is asked
		// for.
		new Image(this, "image3", new RenderedDynamicImageResource(100, 100)
		{
			@Override
			protected boolean render(Graphics2D graphics)
			{
				drawCircle(graphics);
				return true;
			}
		});

		// Simple model
		new Image(this, "image4", new Model<String>("Image2.gif"));

		// Dynamically created buffered image
		new Image(this, "image5", getImage5Resource());

		// Add okay button image
		new Image(this, "okButton", getOkButtonImage());

		// Add cancel button image
		new NonCachingImage(this, "cancelButton", new ResourceReference("cancelButton"));
	}

	final ResourceReference getOkButtonImage()
	{
		return new ResourceReference("okButton")
		{
			@Override
			protected Resource newResource()
			{
				return new DefaultButtonImageResource("Ok");
			}
		};
	}

	/**
	 * @return Gets shared image component
	 */
	public ResourceReference getImage5Resource()
	{
		return new ResourceReference(Home.class, "image5")
		{
			@Override
			public Resource newResource()
			{
				final BufferedDynamicImageResource resource = new BufferedDynamicImageResource();
				final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
				drawCircle((Graphics2D)image.getGraphics());
				resource.setImage(image);
				return resource;
			}
		};
	}

	/**
	 * Draws a random circle on a graphics
	 * 
	 * @param graphics
	 *            The graphics to draw on
	 */
	void drawCircle(Graphics2D graphics)
	{
		// Compute random size for circle
		final Random random = new Random();
		int dx = Math.abs(10 + random.nextInt(80));
		int dy = Math.abs(10 + random.nextInt(80));
		int x = Math.abs(random.nextInt(100 - dx));
		int y = Math.abs(random.nextInt(100 - dy));

		// Draw circle with thick stroke width
		graphics.setStroke(new BasicStroke(5));
		graphics.drawOval(x, y, dx, dy);
	}
}
