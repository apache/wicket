/*
 * $Id$ $Revision:
 * 1.9 $ $Date$
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
package wicket.markup.html.tab;

import java.awt.Graphics2D;

import wicket.markup.html.image.DynamicImage;

/**
 * Draws a basic tab component, varying the image depending on which page is active.
 * 
 * @author Jonathan Locke
 */
public final class TabNavigator extends DynamicImage
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 209001445308790198L;

	/**
	 * @see DynamicImage#DynamicImage(String, int, int)
	 */
	public TabNavigator(final String name, final int width, final int height)
	{
		super(name, width, height);
	}

	/**
	 * @see wicket.markup.html.image.DynamicImage#render(java.awt.Graphics2D)
	 */
	protected void render(Graphics2D graphics)
	{
		
	}
}
