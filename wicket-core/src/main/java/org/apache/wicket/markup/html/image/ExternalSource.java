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
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CrossOrigin;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A component which displays external images within a picture tag.
 * 
 * @see org.apache.wicket.markup.html.image.Source
 * 
 * @author Tobias Soloschenko
 * @author Sebastien Briquet
 * @author Sven Meier
 * @author Martin Grigorov
 *
 */
public class ExternalSource extends ExternalImage
{

	private static final long serialVersionUID = 1L;

	private String media = null;

	/**
	 * Creates an external source
	 * 
	 * @param id
	 *            the component id
	 */
	public ExternalSource(String id)
	{
		super(id, null, Model.ofList(Collections.<Serializable> emptyList()));
	}
	
	/**
	 * Creates an external source
	 * 
	 * @param id
	 *            the component id
	 * @param srcSet
	 *            a list of URLs placed in the srcset attribute
	 */
	public ExternalSource(String id, List<Serializable> srcSet)
	{
		super(id, null, Model.ofList(srcSet));
	}

	/**
	 * Creates an external source
	 * 
	 * @param id
	 *            the component id
	 * @param srcSetModel
	 *            a model list of URLs placed in the srcset attribute
	 */
	public ExternalSource(String id, IModel<List<Serializable>> srcSetModel)
	{
		super(id, null, srcSetModel);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "source");
		super.onComponentTag(tag);
		if (getMedia() != null)
		{
			tag.put("media", getMedia());
		}
	}

	/**
	 * Sets the media attribute information
	 *
	 * @param media
	 *            the media attribute information
	 */
	public void setMedia(String media)
	{
		this.media = media;
	}

	/**
	 * Gets the media attribute information
	 *
	 * @return the media attribute information
	 */
	public String getMedia()
	{
		return media;
	}

	/**
	 * Unsupported for source tag
	 */
	@Override
	public void setCrossOrigin(CrossOrigin crossorigin)
	{
		throw new UnsupportedOperationException(
			"It is not allowed to set the crossorigin attribute for source tag");
	}

	/**
	 * Unsupported for source tag
	 */
	@Override
	public final CrossOrigin getCrossOrigin()
	{
		return null;
	}
}
