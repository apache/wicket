/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.images;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import wicket.Component;
import wicket.examples.WicketExampleApplication;
import wicket.markup.html.image.BufferedDynamicImage;

/**
 * WicketServlet class for wicket.examples.linkomatic example.
 * @author Jonathan Locke
 */
public class ImagesApplication extends WicketExampleApplication
{
	private BufferedDynamicImage image5;
	
	/**
	 * Constructor
	 */
    public ImagesApplication()
    {
        getPages().setHomePage(Home.class);
    }
    
    /**
     * @return Gets shared image component
     */
    public BufferedDynamicImage getImage5()
    {
    	if (image5 == null)
    	{
		    image5 = new BufferedDynamicImage("image5");
			final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			drawCircle((Graphics2D)image.getGraphics());
			image5.setImage(image);
			image5.setSharing(Component.APPLICATION_SHARED);
    	}
    	return image5;
    }
    
	/**
	 * Draws a random circle on a graphics
	 * @param graphics The graphics to draw on
	 */
	static void drawCircle(Graphics2D graphics)
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


