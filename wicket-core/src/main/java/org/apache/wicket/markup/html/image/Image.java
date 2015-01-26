/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.image;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.image.resource.LocalizedImageResource;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * An Image component displays localizable image resources.
 * <p>
 * For details of how Images load, generate and manage images, see {@link LocalizedImageResource}.
 * 
 * The first ResourceReference / ImageResource is used for the src attribute within the img tag, all
 * following are applied to the srcset. If setXValues(String... values) is used the values are set
 * behind the srcset elements in the order they are given to the setXValues(String... valus) method.
 * The separated values in the sizes attribute are set with setSizes(String... sizes)
 * 
 * @see NonCachingImage
 * 
 * @author Jonathan Locke
 * @author Tobias Soloschenko
 * 
 */
public class Image extends WebComponent implements IResourceListener
{
	private static final long serialVersionUID = 1L;

	/** The image resource this image component references (src attribute) */
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/** The image resources this image component references (srcset attribute) */
	private final List<LocalizedImageResource> localizedImageResources = new ArrayList<LocalizedImageResource>();

	/** The x values to be used within the srcset */
	private List<String> xValues = null;

	/** The sizes of the responsive images */
	private List<String> sizes = null;
	
	/**
	 * Cross origin settings
	 */
	private Cors crossorigin = null;

	/**
	 * This constructor can be used if you override {@link #getImageResourceReference()} or
	 * {@link #getImageResource()}
	 * 
	 * @param id
	 */
	protected Image(final String id)
	{
		super(id);
	}

	/**
	 * Constructs an image from an image resourcereference. That resource reference will bind its
	 * resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference is pointing to a
	 * Resource that isn't guaranteed to be on every server, for example a dynamic image or
	 * resources that aren't added with a IInitializer at application startup. Then if only that
	 * resource is requested from another server, without the rendering of the page, the image won't
	 * be there and will result in a broken link.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource used in the src attribute
	 * @param resourceReferences
	 *            The shared image resources used in the srcset attribute
	 */
	public Image(final String id, final ResourceReference resourceReference,
		final ResourceReference... resourceReferences)
	{
		this(id, resourceReference, null, resourceReferences);
	}

	/**
	 * Constructs an image from an image resourcereference. That resource reference will bind its
	 * resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference is pointing to a
	 * Resource that isn't guaranteed to be on every server, for example a dynamic image or
	 * resources that aren't added with a IInitializer at application startup. Then if only that
	 * resource is requested from another server, without the rendering of the page, the image won't
	 * be there and will result in a broken link.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource used in the src attribute
	 * @param resourceParameters
	 *            The resource parameters
	 * @param resourceReferences
	 *            The shared image resources used in the srcset attribute
	 */
	public Image(final String id, final ResourceReference resourceReference,
		PageParameters resourceParameters, final ResourceReference... resourceReferences)
	{
		super(id);
		this.setImageResourceReference(resourceParameters, resourceReference);
		this.setImageResourceReferences(resourceParameters, resourceReferences);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * This one doesn't have the 'non sticky session clustering' problem that the ResourceReference
	 * constructor has. But this will result in a non 'stable' url and the url will have request
	 * parameters.
	 * 
	 * @param id
	 *            See Component
	 * 
	 * @param imageResource
	 *            The image resource used in the src attribute
	 * @param imageResources
	 *            The image resource used in the srcset attribute
	 */
	public Image(final String id, final IResource imageResource, final IResource... imageResources)
	{
		super(id);
		this.setImageResource(imageResource);
		this.setImageResources(imageResources);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            See Component
	 * @param string
	 *            Name of image
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final String string)
	{
		this(id, new Model<String>(string));
	}

	/**
	 * @see org.apache.wicket.IResourceListener#onResourceRequested()
	 */
	@Override
	public void onResourceRequested()
	{
		localizedImageResource.onResourceRequested(null);
		for (LocalizedImageResource localizedImageResource : this.localizedImageResources)
		{
			localizedImageResource.onResourceRequested(null);
		}
	}

	/**
	 * @param imageResource
	 *            The new ImageResource to set.
	 */
	public void setImageResource(final IResource imageResource)
	{
		if (imageResource != null)
		{
			this.localizedImageResource.setResource(imageResource);
		}
	}

	/**
	 * 
	 * @param imageResources
	 *            the new ImageResource to set.
	 */
	public void setImageResources(final IResource... imageResources)
	{
		this.localizedImageResources.clear();
		for (IResource imageResource : imageResources)
		{
			LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);
			localizedImageResource.setResource(imageResource);
			this.localizedImageResources.add(localizedImageResource);
		}
	}

	/**
	 * @param resourceReference
	 *            The resource reference to set.
	 */
	public void setImageResourceReference(final PageParameters parameters,
		final ResourceReference resourceReference)
	{
		if (localizedImageResource != null)
		{
			if (parameters != null)
			{
				this.localizedImageResource.setResourceReference(resourceReference, parameters);
			}
			else
			{
				this.localizedImageResource.setResourceReference(resourceReference);
			}
		}
	}

	/**
	 * @param parameters
	 *            Set the resource parameters for the resource.
	 * @param resourceReferences
	 *            The resource references to set.
	 */
	public void setImageResourceReferences(final PageParameters parameters,
		final ResourceReference... resourceReferences)
	{
		this.localizedImageResources.clear();
		for (ResourceReference resourceReference : resourceReferences)
		{
			LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);
			if (parameters != null)
			{
				localizedImageResource.setResourceReference(resourceReference, parameters);
			}
			else
			{
				localizedImageResource.setResourceReference(resourceReference);
			}
			this.localizedImageResources.add(localizedImageResource);
		}
	}

	/**
	 * @param values
	 *            the x values to be used in the srcset
	 */
	public void setXValues(String... values)
	{
		if (this.xValues == null)
		{
			xValues = new ArrayList<String>();
		}
		this.xValues.clear();
		this.xValues.addAll(Arrays.asList(values));
	}

	/**
	 * @param sizes
	 *            the sizes to be used in the size
	 */
	public void setSizes(String... sizes)
	{
		if (this.sizes == null)
		{
			this.sizes = new ArrayList<String>();
		}
		this.sizes.clear();
		this.sizes.addAll(Arrays.asList(sizes));
	}

	/**
	 * @see org.apache.wicket.Component#setDefaultModel(org.apache.wicket.model.IModel)
	 */
	@Override
	public Component setDefaultModel(IModel<?> model)
	{
		// Null out the image resource, so we reload it (otherwise we'll be
		// stuck with the old model.
		for (LocalizedImageResource localizedImageResource : this.localizedImageResources)
		{
			localizedImageResource.setResourceReference(null);
			localizedImageResource.setResource(null);
		}
		localizedImageResource.setResourceReference(null);
		localizedImageResource.setResource(null);
		return super.setDefaultModel(model);
	}

	/**
	 * @return Resource returned from subclass
	 */
	protected IResource getImageResource()
	{
		return localizedImageResource.getResource();
	}

	/**
	 * @return ResourceReference returned from subclass
	 */
	protected ResourceReference getImageResourceReference()
	{
		return localizedImageResource.getResourceReference();
	}

	/**
	 * @see org.apache.wicket.Component#initModel()
	 */
	@Override
	protected IModel<?> initModel()
	{
		// Images don't support Compound models. They either have a simple
		// model, explicitly set, or they use their tag's src or value
		// attribute to determine the image.
		return null;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		if (tag.getName().equals("source"))
		{
			this.buildSrcSetAttribute(tag);
			tag.remove("src");
		}
		else
		{
			this.checkComponentTag(tag, "img");
			String srcAttribute = this.buildSrcAttribute(tag);
			this.buildSrcSetAttribute(tag);
			tag.put("src", srcAttribute);
			
		}
		this.buildSizesAttribute(tag);
		
		if (this.crossorigin != null) {
			tag.put("crossorigin", this.crossorigin.getRealName());
		}
	}

	/**
	 * Builds the srcset attribute if multiple localizedImageResources are found as varargs
	 * 
	 * @param tag
	 *            the component tag
	 */
	protected void buildSrcSetAttribute(final ComponentTag tag)
	{
		int srcSetPosition = 0;
		for (LocalizedImageResource localizedImageResource : this.localizedImageResources)
		{
			localizedImageResource.setSrcAttribute(tag);

			if (this.shouldAddAntiCacheParameter())
			{
				this.addAntiCacheParameter(tag);
			}

			String srcset = tag.getAttribute("srcset");
			String xValue = "";
			
			// If there are xValues set process them in the applied order to the srcset attribute.
			if (this.xValues != null)
			{
				xValue = this.xValues.size() > srcSetPosition &&
					this.xValues.get(srcSetPosition) != null ? " " +
					this.xValues.get(srcSetPosition) : "";
			}
			tag.put("srcset", (srcset != null ? srcset + ", " : "") + tag.getAttribute("src") +
				xValue);
			srcSetPosition++;
		}
	}

	/**
	 * Builds the src attribute
	 * 
	 * @param tag
	 *            the component tag
	 * @return the value of the src attribute
	 */
	protected String buildSrcAttribute(final ComponentTag tag)
	{
		final IResource resource = this.getImageResource();
		if (resource != null)
		{
			this.localizedImageResource.setResource(resource);
		}
		final ResourceReference resourceReference = this.getImageResourceReference();
		if (resourceReference != null)
		{
			this.localizedImageResource.setResourceReference(resourceReference);
		}
		this.localizedImageResource.setSrcAttribute(tag);

		if (this.shouldAddAntiCacheParameter())
		{
			this.addAntiCacheParameter(tag);
		}
		return tag.getAttribute("src");
	}

	/**
	 * builds the sizes attribute of the img tag
	 * 
	 * @param tag
	 *            the component tag
	 */
	protected void buildSizesAttribute(final ComponentTag tag)
	{
		// if no sizes have been set then don't build the attribute
		if (this.sizes == null)
		{
			return;
		}
		String sizes = "";
		for (String size : this.sizes)
		{
			sizes += size + ",";
		}
		int lastIndexOf = sizes.lastIndexOf(",");
		if (lastIndexOf != -1)
		{
			sizes = sizes.substring(0, lastIndexOf);
		}
		if (!"".equals(sizes))
		{
			tag.put("sizes", sizes);
		}
	}

	/**
	 * Adding an image to {@link org.apache.wicket.ajax.AjaxRequestTarget} most of the times mean
	 * that the image has changes and must be re-rendered.
	 * <p>
	 * With this method the user may change this default behavior for some of her images.
	 * </p>
	 * 
	 * @return {@code true} to add the anti cache request parameter, {@code false} - otherwise
	 */
	protected boolean shouldAddAntiCacheParameter()
	{
		return this.getRequestCycle().find(AjaxRequestTarget.class) != null;
	}

	/**
	 * Adds random noise to the url every request to prevent the browser from caching the image.
	 * 
	 * @param tag
	 */
	protected final void addAntiCacheParameter(final ComponentTag tag)
	{
		String url = tag.getAttributes().getString("src");
		url = url + (url.contains("?") ? "&" : "?");
		url = url + "antiCache=" + System.currentTimeMillis();

		tag.put("src", url);
	}

	/**
	 * @see org.apache.wicket.Component#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		boolean stateless = (getImageResource() == null || getImageResource() == localizedImageResource.getResource()) &&
			localizedImageResource.isStateless();
		boolean statelessList = false;
		for (LocalizedImageResource localizedImageResource : this.localizedImageResources)
		{
			if (localizedImageResource.isStateless())
			{
				statelessList = true;
			}
		}
		return stateless || statelessList;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}

	@Override
	public boolean canCallListenerInterface(Method method)
	{
		boolean isResource = method != null &&
			IResourceListener.class.isAssignableFrom(method.getDeclaringClass());
		if (isResource && this.isVisibleInHierarchy())
		{
			// when the image data is requested we do not care if this component
			// is enabled in
			// hierarchy or not, only that it is visible
			return true;
		}
		else
		{
			return super.canCallListenerInterface(method);
		}
	}
	
	/**
	 * Gets the cross origin settings
	 * 
	 * @see {@link #setCrossorigin(Cors)}
	 * 
	 * @return the cross origins settings
	 */
	public Cors getCrossorigin() {
		return this.crossorigin;
	}

	/**
	 * Sets the cross origin settings<br>
	 * <br>
	 * 
	 * <b>anonymous</b>: Cross-origin CORS requests for the element will not have the credentials flag set.<br>
	 * <br>
	 * <b>use_credentials</b>: Cross-origin CORS requests for the element will have the credentials flag set.<br>
	 * <br>
	 * <b>no_cores</b>: The empty string is also a valid keyword, and maps to the Anonymous state. The attribute's invalid value default is the
	 * Anonymous state. The missing value default, used when the attribute is omitted, is the No CORS state
	 * 
	 * @param crossorigin
	 *            the cross origins settings to set
	 */
	public void setCrossorigin(Cors crossorigin) {
		this.crossorigin = crossorigin;
	}

	/**
	 * To be used for the crossorigin attribute
	 * 
	 * @see {@link #setCrossorigin(Cors)}
	 */
	public enum Cors {
		anonymous("anonymous"),
		use_credentials("user-credentials"),
		no_cors("");

		private String realName;

		private Cors(String realName) {
			this.realName = realName;
		}

		public String getRealName() {
			return this.realName;
		}
	}
}
