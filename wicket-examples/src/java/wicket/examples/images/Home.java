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

import java.awt.Graphics2D;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.image.Image;
import wicket.markup.html.image.resource.RenderedDynamicImageResource;

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
		// Get our custom application subclass
		final ImagesApplication application = (ImagesApplication)getApplication();
		
		// Image as package resource
		add(new Image("image2"));

		// Dynamically created image.  Will re-render whenever resource is asked for.
		add(new Image("image3", new RenderedDynamicImageResource(100, 100)
		{
			protected void render(Graphics2D graphics)
			{
				application.drawCircle(graphics);
			}
		}));

		add(new Image("image4", "Image2.gif"));
		

		// Dynamically created buffered image
		add(new Image("image5", application.getImage5Resource()));
		
		// Add cancel button image
		add(new Image("cancelButton", application.getCancelButtonImageResource()));
	}
}
