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
package wicket.examples.images;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import wicket.ISharedResourceFactory;
import wicket.Resource;
import wicket.SharedResource;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.image.Image;
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
	static final DefaultButtonImageResource cancelButtonImageResource = new DefaultButtonImageResource(
			30, 30, "Cancel!");

	/**
	 * Constructor
	 */
	public Home()
	{
		// Get our custom application subclass
		final ImagesApplication application = (ImagesApplication)getApplication();

		// Image as package resource
		add(new Image("image2"));

		// Dynamically created image. Will re-render whenever resource is asked
		// for.
		add(new Image("image3", new RenderedDynamicImageResource(100, 100)
		{
			protected boolean render(Graphics2D graphics)
			{
				drawCircle(graphics);
				return true;
			}
		}));

		// Simple model
		add(new Image("image4", new Model("Image2.gif")));

		// Dynamically created buffered image
		add(new Image("image5", getImage5Resource()));

		// Add cancel button image
		add(new Image("cancelButton", cancelButtonImageResource.getShared(getApplication(),
				Home.class, "cancel")));
	}

	/**
	 * @return Gets shared image component
	 */
	public SharedResource getImage5Resource()
	{
		return getApplication().getSharedResource(Home.class, "image5",
				new ISharedResourceFactory()
				{
					public Resource newResource()
					{
						final BufferedDynamicImageResource resource = new BufferedDynamicImageResource();
						final BufferedImage image = new BufferedImage(100, 100,
								BufferedImage.TYPE_INT_RGB);
						drawCircle((Graphics2D)image.getGraphics());
						resource.setImage(image);
						return resource;
					}
				});
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
