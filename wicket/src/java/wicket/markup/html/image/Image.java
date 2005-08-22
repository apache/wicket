/*
 * $Id$ $Revision:
 * 1.27 $ $Date$
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
package wicket.markup.html.image;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IResourceListener;
import wicket.Resource;
import wicket.ResourceReference;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebResource;
import wicket.markup.html.image.resource.LocalizedImageResource;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * An Image component displays a localizable image resource.
 * <p>
 * For details of how Images load, generate and manage images, see
 * {@link LocalizedImageResource}.
 * 
 * @author Jonathan Locke
 */
public class Image extends WebComponent implements IResourceListener
{
	private static final Log log = LogFactory.getLog(Image.class);
	
	/** The image resource this image component references */
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * @see wicket.Component#Component(String)
	 */
	public Image(final String id)
	{
		super(id);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource
	 */
	public Image(final String id, final ResourceReference resourceReference)
	{
		super(id);
		localizedImageResource.setResourceReference(resourceReference);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * @param id
	 *            See Component
	 * 
	 * @param imageResource
	 *            The image resource
	 */
	public Image(final String id, final WebResource imageResource)
	{
		super(id);
		setImageResource(imageResource);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            See Component
	 * @param string
	 *            Name of image
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final String string)
	{
		this(id, new Model(string));
	}

	/**
	 * @see wicket.IResourceListener#onResourceRequested()
	 */
	public void onResourceRequested()
	{
		localizedImageResource.onResourceRequested();
	}

	/**
	 * @param imageResource
	 *            The new ImageResource to set.
	 */
	public void setImageResource(final WebResource imageResource)
	{
		this.localizedImageResource.setResource(imageResource);
	}

	/**
	 * @param resourceReference
	 *            The shared ImageResource to set.
	 */
	public void setImageResourceReference(final ResourceReference resourceReference)
	{
		this.localizedImageResource.setResourceReference(resourceReference);
	}

	/**
	 * @return Resource returned from subclass
	 */
	protected Resource getImageResource()
	{
		return null;
	}

	/**
	 * @return ResourceReference returned from subclass
	 */	
	protected ResourceReference getImageResourceReference()
	{
		return null;
	}

	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		// Images don't support Compound models. They either have a simple
		// model, explicitly set, or they use their tag's src or value
		// attribute to determine the image.
		return null;
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "img");
		super.onComponentTag(tag);
		final Resource resource = getImageResource();
		if (resource != null)
		{
			localizedImageResource.setResource(resource);
		}
		final ResourceReference resourceReference = getImageResourceReference();
		if (resourceReference != null)
		{
			localizedImageResource.setResourceReference(resourceReference);
		}
		localizedImageResource.setSrcAttribute(tag);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}

	/**
	 * @see wicket.Component#onSessionAttach()
	 */
	protected void onSessionAttach()
	{
		try
		{
			localizedImageResource.bind();
		} 
		catch(WicketRuntimeException wre)
		{
			// If this exceptions happens here then the locale is maybe changed
			// and there is no image for that locale you are in now.
			log.error("Localized Image Resource not found for the current locale " + getLocale(), wre);
		}
	}
}
