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

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A factory which creates images.
 * 
 * @author Jonathan Locke
 */
public abstract class ImageResourceFactory
{
	/** Serial Version ID */
	private static final long serialVersionUID = 5934721258765771884L;

	/** The name of this factory */
	private String name;
	
	/** Map from labels to image resources */
	private static final Map labelToImageResource = new WeakHashMap();

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Name of this factory
	 */
	public ImageResourceFactory(final String name)
	{
		this.name = name;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param label
	 *            Any label to put on the image that's created
	 * @return Image resource
	 */
	public ImageResource imageResource(final String label)
	{
		ImageResource image = (ImageResource)labelToImageResource.get(label);
		if (image == null)
		{
			image = newImageResource(label);
			labelToImageResource.put(label, image);
		}
		return image;
	}

	/**
	 * @param label
	 *            Any label to put on the image that's created
	 * @return Image resource
	 */
	protected abstract ImageResource newImageResource(final String label);
}
