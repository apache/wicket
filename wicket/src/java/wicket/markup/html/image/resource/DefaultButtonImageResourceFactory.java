/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.image.resource;

/**
 * A factory which creates default button images.
 * 
 * @author Jonathan Locke
 */
public class DefaultButtonImageResourceFactory extends ImageResourceFactory
{
	/** Serial Version ID */
	private static final long serialVersionUID = 5934721258765771884L;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Name of this resource factory
	 */
	public DefaultButtonImageResourceFactory(final String name)
	{
		super(name);
	}

	/**
	 * @see ImageResourceFactory#newImageResource(int, int, String)
	 */
	public ImageResource newImageResource(final int width, final int height, String label)
	{
		return new DefaultButtonImageResource(width, height, label);
	}
}
