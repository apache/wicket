/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import wicket.examples.WicketExamplePage;
import wicket.markup.html.image.BufferedDynamicImage;
import wicket.markup.html.image.DynamicImage;
import wicket.markup.html.image.Image;

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
		add(new Image("image2"));

		// Dynamically created image.  Will re-render whenever resource is asked for.
		add(new DynamicImage("image3", 100, 100)
		{
			protected void render(Graphics2D graphics)
			{
				drawCircle(graphics);
			}
		});

		add(new Image("image4", "Image2.gif"));

		// Dynamically created buffered image
		final BufferedDynamicImage bufferedDynamicImage = new BufferedDynamicImage("image5");
		add(bufferedDynamicImage);
		final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		drawCircle((Graphics2D)image.getGraphics());
		bufferedDynamicImage.setImage(image);
	}
	
	/**
	 * Draws a random circle on a graphics
	 * @param graphics The graphics to draw on
	 */
	private void drawCircle(Graphics2D graphics)
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
