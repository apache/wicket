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
package wicket.markup.html.link;

import wicket.IResourceListener;
import wicket.Resource;
import wicket.ResourceReference;

/**
 * A link to any ResourceReference.
 * 
 * @author Jonathan Locke
 */
public class ResourceLink extends Link implements IResourceListener
{
	private static final long serialVersionUID = 1L;
	
	/** The Resource reference */
	private final ResourceReference resourceReference;
	
	/** The Resource */
	private final Resource resource;

	/**
	 * Constructs an ResourceLink from an resourcereference.
	 * That resource reference will bind its resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference
     * is pointing to a Resource that isn't guaranteed to be on every server,
     * for example a dynamic image or resources that aren't added with a IInitializer
     * at application startup. Then if only that resource is requested from another
     * server, without the rendering of the page, the image won't be there and will
     * result in a broken link.
     *     
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared resource to link to
	 */
	public ResourceLink(final String id, final ResourceReference resourceReference)
	{
		super(id);
		this.resourceReference = resourceReference;
		this.resource = null;
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * This one doesn't have the 'non sticky session clustering' problem that the 
	 * ResourceReference constructor has.
	 * But this will result in a non 'stable' url and that url will have request parameters. 
	 * 
	 * @param id
	 *            See Component
	 * @param resource
	 *            The resource
	 */
	public ResourceLink(final String id, final Resource resource)
	{
		super(id);
		this.resource = resource;
		this.resourceReference = null;
	}
	
	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick()
	{
	}

	/**
	 * @see wicket.Component#onSessionAttach()
	 */
	protected void onSessionAttach()
	{
	}
	
	/**
	 * @see wicket.markup.html.link.Link#getURL()
	 */
	protected final String getURL()
	{
		if (resourceReference != null)
		{
			resourceReference.setLocale(getLocale());
			resourceReference.setStyle(getStyle());
			resourceReference.bind(getApplication());
			return getPage().urlFor(resourceReference.getPath());
		}
		return urlFor(IResourceListener.class);
	}

	/**
	 * @see wicket.IResourceListener#onResourceRequested()
	 */
	public final void onResourceRequested()
	{
		resource.onResourceRequested();
	}
}
