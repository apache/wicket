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

import wicket.examples.WicketExampleApplication;
import wicket.markup.html.image.resource.BufferedDynamicImageResource;
import wicket.markup.html.image.resource.DefaultButtonImageResource;

/**
 * WicketServlet class for wicket.examples.linkomatic example.
 * @author Jonathan Locke
 */
public class ImagesApplication extends WicketExampleApplication
{
	private BufferedDynamicImageResource image5Resource;
	private DefaultButtonImageResource cancelButtonImageResource;
	
	/**
	 * Constructor
	 */
    public ImagesApplication()
    {
        getPages().setHomePage(Home.class);
        
	    cancelButtonImageResource = new DefaultButtonImageResource("Cancel");
    }
    
    /**
     * @return Gets shared image component
     */
    public BufferedDynamicImageResource getImage5Resource()
    {
    	if (image5Resource == null)
    	{
		    image5Resource = new BufferedDynamicImageResource();
			final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			drawCircle((Graphics2D)image.getGraphics());
			image5Resource.setImage(image);
    	}
    	return image5Resource;
    }
    
    /**
     * @return Cancel button image
     */
    public DefaultButtonImageResource getCancelButtonImageResource()
    {
    	return cancelButtonImageResource;
    }
    
	/**
	 * Draws a random circle on a graphics
	 * @param graphics The graphics to draw on
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


