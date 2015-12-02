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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.image.Image.Cors;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A component to display external images. The src / srcSet information are hold in models
 * 
 * @see org.apache.wicket.markup.html.image.Image
 * 
 * @author Tobias Soloschenko
 * @author Sebastien Briquet
 * @author Sven Meier
 * @author Martin Grigorov
 *
 */
public class ExternalImage extends WebComponent
{

	private static final long serialVersionUID = 1L;

	/** The x values to be used within the srcset */
	private List<String> xValues = null;

	/** The sizes of the responsive images */
	private List<String> sizes = null;

	/**
	 * Cross origin settings
	 */
	private Cors crossOrigin = null;

	private IModel<List<Serializable>> srcSetModel;

	/**
	 * Creates an external image
	 * 
	 * @param id
	 *            the component id
	 */
	public ExternalImage(String id)
	{
		this(id, null, Model.ofList(Collections.<Serializable> emptyList()));
	}

	/**
	 * Creates an external image
	 * 
	 * @param id
	 *            the component id
	 * @param src
	 *            the source URL
	 */
	public ExternalImage(String id, Serializable src)
	{
		this(id, Model.of(src), Model.ofList(Collections.<Serializable> emptyList()));
	}

	/**
	 * Creates an external image
	 * 
	 * @param id
	 *            the component id
	 * @param src
	 *            the source URL
	 * @param srcSet
	 *            a list of URLs placed in the srcSet attribute
	 */
	public ExternalImage(String id, Serializable src, List<Serializable> srcSet)
	{
		this(id, Model.of(src), Model.ofList(srcSet));
	}

	/**
	 * Creates an external image
	 * 
	 * @param id
	 *            the component id
	 * @param srcModel
	 *            the model source URL
	 */
	public ExternalImage(String id, IModel<Serializable> srcModel)
	{
		this(id, srcModel, Model.ofList(Collections.<Serializable> emptyList()));
	}

	/**
	 * Creates an external image
	 * 
	 * @param id
	 *            the component id
	 * @param srcModel
	 *            the model source URL
	 * @param srcSetModel
	 *            a model list of URLs placed in the srcSet attribute
	 */
	public ExternalImage(String id, IModel<Serializable> srcModel,
		IModel<List<Serializable>> srcSetModel)
	{
		super(id, srcModel);
		this.srcSetModel = srcSetModel;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if ("source".equals(tag.getName()))
		{
			buildSrcSetAttribute(tag, getSrcSetModel());
		}
		else
		{
			checkComponentTag(tag, "img");
			buildSrcAttribute(tag, getDefaultModel());
			buildSrcSetAttribute(tag, getSrcSetModel());
		}

		buildSizesAttribute(tag);

		Cors crossOrigin = getCrossOrigin();
		if (crossOrigin != null && Cors.NO_CORS != crossOrigin)
		{
			tag.put("crossOrigin", crossOrigin.getRealName());
		}
	}

	/**
	 * Builds the src attribute
	 *
	 * @param tag
	 *            the component tag
	 * @param srcModel
	 *            the model containing the src URL
	 */
	protected void buildSrcAttribute(final ComponentTag tag, IModel<?> srcModel)
	{
		tag.put("src", srcModel.getObject().toString());
	}

	/**
	 * Builds the srcset attribute if multiple models are found as varargs
	 *
	 * @param tag
	 *            the component tag
	 * @param srcSetModel
	 *            the models containing the src set URLs
	 */
	protected void buildSrcSetAttribute(final ComponentTag tag,
		IModel<List<Serializable>> srcSetModel)
	{
		int srcSetPosition = 0;
		List<Serializable> srcSetItems = srcSetModel.getObject();
		for (Serializable srcSet : srcSetItems)
		{
			String srcset = tag.getAttribute("srcset");
			String xValue = "";

			// If there are xValues set process them in the applied order to the srcset
			// attribute.
			if (xValues != null)
			{
				xValue = xValues.size() > srcSetPosition && xValues.get(srcSetPosition) != null
					? " " + xValues.get(srcSetPosition) : "";
			}
			tag.put("srcset", (srcset != null ? srcset + ", " : "") + srcSet + xValue);
			srcSetPosition++;
		}
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
		if (sizes == null)
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
	 * @param values
	 *            the x values to be used in the srcset
	 */
	public void setXValues(String... values)
	{
		if (xValues == null)
		{
			xValues = new ArrayList<>();
		}
		else
		{
			xValues.clear();
		}
		xValues.addAll(Arrays.asList(values));
	}

	/**
	 * @param sizes
	 *            the sizes to be used in the size
	 */
	public void setSizes(String... sizes)
	{
		if (this.sizes == null)
		{
			this.sizes = new ArrayList<>();
		}
		else
		{
			this.sizes.clear();
		}
		this.sizes.addAll(Arrays.asList(sizes));
	}

	/**
	 * Gets the cross origin settings
	 * 
	 * @see {@link org.apache.wicket.markup.html.image.Image#setCrossOrigin(Cors)}
	 *
	 * @return the cross origins settings
	 */
	public Cors getCrossOrigin()
	{
		return crossOrigin;
	}

	/**
	 * Sets the cross origin settings
	 * 
	 * @see {@link org.apache.wicket.markup.html.image.Image#setCrossOrigin(Cors)}
	 * @param crossOrigin
	 *            the cross origins settings to set
	 */
	public void setCrossOrigin(Cors crossOrigin)
	{
		this.crossOrigin = crossOrigin;
	}

	/**
	 * Gets a list of models containing the src set values
	 * 
	 * @return a list of models containing the src set values
	 */
	public IModel<List<Serializable>> getSrcSetModel()
	{
		return srcSetModel;
	}

	/**
	 * Sets the source set model
	 * 
	 * @param srcSetModel
	 *            the model of a list of src set entries
	 */
	public void setSrcSetModel(IModel<List<Serializable>> srcSetModel)
	{
		this.srcSetModel = srcSetModel;
	}

	/**
	 * Detaches the srcSetModels
	 */
	@Override
	protected void onDetach()
	{
		if (srcSetModel != null)
		{
			srcSetModel.detach();
		}
		super.onDetach();
	}
}
