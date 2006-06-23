/*
 * $Id: ResourceLink.java 5231 2006-04-02 01:34:49 +0200 (zo, 02 apr 2006)
 * joco01 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) $
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
import wicket.MarkupContainer;
import wicket.Resource;
import wicket.ResourceReference;
import wicket.util.value.ValueMap;

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

	/** The resource parameters */
	private final ValueMap resourceParameters;


	/**
	 * Constructs an ResourceLink from an resourcereference. That resource
	 * reference will bind its resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference
	 * is pointing to a Resource that isn't guaranteed to be on every server,
	 * for example a dynamic image or resources that aren't added with a
	 * IInitializer at application startup. Then if only that resource is
	 * requested from another server, without the rendering of the page, the
	 * image won't be there and will result in a broken link.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared resource to link to
	 */
	public ResourceLink(MarkupContainer parent, final String id,
			final ResourceReference resourceReference)
	{
		this(parent, id, resourceReference, null);
	}

	/**
	 * Constructs an ResourceLink from an resourcereference. That resource
	 * reference will bind its resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference
	 * is pointing to a Resource that isn't guaranteed to be on every server,
	 * for example a dynamic image or resources that aren't added with a
	 * IInitializer at application startup. Then if only that resource is
	 * requested from another server, without the rendering of the page, the
	 * image won't be there and will result in a broken link.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared resource to link to
	 * @param resourceParameters
	 *            The resource parameters
	 */
	public ResourceLink(MarkupContainer parent, final String id,
			final ResourceReference resourceReference, ValueMap resourceParameters)
	{
		super(parent, id);
		this.resourceReference = resourceReference;
		this.resourceParameters = resourceParameters;
		this.resource = null;
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * This one doesn't have the 'non sticky session clustering' problem that
	 * the ResourceReference constructor has. But this will result in a non
	 * 'stable' url and that url will have request parameters.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param resource
	 *            The resource
	 */
	public ResourceLink(MarkupContainer parent, final String id, final Resource resource)
	{
		super(parent, id);
		this.resource = resource;
		this.resourceReference = null;
		this.resourceParameters = null;
	}

	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
	}

	/**
	 * @see wicket.IResourceListener#onResourceRequested()
	 */
	public final void onResourceRequested()
	{
		onClick();
		resource.onResourceRequested();
	}

	/**
	 * @see wicket.markup.html.link.Link#getURL()
	 */
	@Override
	protected final CharSequence getURL()
	{
		if (resourceReference != null)
		{
			// TODO post 1.2: should we have support for locale changes when the
			// resource reference (or resource??) is set manually..
			// We should get a new resource reference for the current locale
			// then
			// that points to the same resource but with another locale if it
			// exists.
			// something like
			// SharedResource.getResourceReferenceForLocale(resourceReference);

			resourceReference.bind(getApplication());
			return getRequestCycle().urlFor(resourceReference, resourceParameters);
		}
		return urlFor(IResourceListener.INTERFACE);
	}
}
