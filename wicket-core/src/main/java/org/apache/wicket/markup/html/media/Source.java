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
package org.apache.wicket.markup.html.media;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * The source of an audio or a video media component
 *
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 * @since 7.0.0
 */
public class Source extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private boolean displayType;

	private String type;

	private String media;

	private PageParameters pageParameters;

	private final ResourceReference resourceReference;

	private final String url;

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 */
	public Source(String id)
	{
		this(id, null, null, null, null);
	}

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 */
	public Source(String id, IModel<?> model)
	{
		this(id, model, null, null, null);
	}

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 * @param resourceReference
	 *            the resource reference to provide the source data
	 */
	public Source(String id, ResourceReference resourceReference)
	{
		this(id, null, null, null, resourceReference);
	}

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference to provide the source data
	 */
	public Source(String id, IModel<?> model, ResourceReference resourceReference)
	{
		this(id, model, null, null, resourceReference);
	}

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 * @param pageParameters
	 *            the the page parameters applied to the source URL
	 * @param resourceReference
	 *            the resource reference to provide the source data
	 */
	public Source(String id, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		this(id, null, null, pageParameters, resourceReference);
	}

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference to provide the source data
	 * @param pageParameters
	 *            the the page parameters applied to the source URL
	 */
	public Source(String id, IModel<?> model, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		this(id, model, null, pageParameters, resourceReference);
	}

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 * @param url
	 *            an external URL to provide the source information
	 */
	public Source(String id, String url)
	{
		this(id, null, url, null, null);
	}

	/**
	 * Creates a source
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to provide the source information
	 */
	public Source(String id, IModel<?> model, String url)
	{
		this(id, model, url, null, null);
	}

	private Source(String id, IModel<?> model, String url, PageParameters pageParameters,
		ResourceReference resourceReference)
	{
		super(id, model);
		this.url = url;
		this.pageParameters = pageParameters;
		this.resourceReference = resourceReference;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "source");
		super.onComponentTag(tag);

		if (resourceReference != null)
		{
			CharSequence url = RequestCycle.get().urlFor(resourceReference, pageParameters);
			tag.put("src", url);
		}
		else if (url != null)
		{
			tag.put("src", url);
		}

		if (getDisplayType())
		{
			if (type != null)
			{
				tag.put("type", type);
			}
			else if (resourceReference != null)
			{
				// if package resource reference we can get the content
				// type of the package resource
				if(resourceReference instanceof PackageResourceReference){
					PackageResource resource = ((PackageResourceReference)resourceReference).getResource();
					IResourceStream resourceStream = resource.getResourceStream();
					String contentType = resourceStream.getContentType();
					tag.put("type", contentType);
				}
			}
		}

		String _media = getMedia();
		if (_media != null)
		{
			tag.put("media", _media);
		}

	}

	/**
	 * If the type is going to be displayed
	 *
	 * @return If the type is going to be displayed
	 */
	public boolean getDisplayType()
	{
		return displayType;
	}

	/**
	 * Sets if the type is going to be displayed
	 *
	 * @param displayType
	 *            if the type is going to be displayed
	 */
	public void setDisplayType(boolean displayType)
	{
		this.displayType = displayType;
	}

	/**
	 * Gets the type
	 *
	 * @see {@link #setType(String)}
	 *
	 * @return the type of this media element
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the type<br>
	 * <br>
	 *
	 * * The following list shows some examples of how to use the codecs= MIME parameter in the type
	 * attribute.<br>
	 * <br>
	 *
	 * H.264 Constrained baseline profile video (main and extended video compatible) level 3 and
	 * Low-Complexity AAC audio in MP4 container<br>
	 * &lt;source src='video.mp4' <b>type='video/mp4; codecs="avc1.42E01E, mp4a.40.2"'</b>&gt;<br>
	 * H.264 Extended profile video (baseline-compatible) level 3 and Low-Complexity AAC audio in
	 * MP4 container<br>
	 * &lt;source src='video.mp4' <b>type='video/mp4; codecs="avc1.58A01E, mp4a.40.2"'</b>&gt;<br>
	 * H.264 Main profile video level 3 and Low-Complexity AAC audio in MP4 container<br>
	 * &lt;source src='video.mp4' <b>type='video/mp4; codecs="avc1.4D401E, mp4a.40.2"'</b>&gt;<br>
	 * H.264 'High' profile video (incompatible with main, baseline, or extended profiles) level 3
	 * and Low-Complexity AAC audio in MP4 container<br>
	 * &lt;source src='video.mp4' <b>type='video/mp4; codecs="avc1.64001E, mp4a.40.2"'</b>&gt;<br>
	 * MPEG-4 Visual Simple Profile Level 0 video and Low-Complexity AAC audio in MP4 container<br>
	 * &lt;source src='video.mp4' <b>type='video/mp4; codecs="mp4v.20.8, mp4a.40.2"'</b>&gt;<br>
	 * MPEG-4 Advanced Simple Profile Level 0 video and Low-Complexity AAC audio in MP4 container<br>
	 * &lt;source src='video.mp4' <b>type='video/mp4; codecs="mp4v.20.240, mp4a.40.2"'</b>&gt;<br>
	 * MPEG-4 Visual Simple Profile Level 0 video and AMR audio in 3GPP container<br>
	 * &lt;source src='video.3gp' <b>type='video/3gpp; codecs="mp4v.20.8, samr"'</b>&gt;<br>
	 * Theora video and Vorbis audio in Ogg container<br>
	 * &lt;source src='video.ogv' <b>type='video/ogg; codecs="theora, vorbis"'</b>&gt;<br>
	 * Theora video and Speex audio in Ogg container<br>
	 * &lt;source src='video.ogv' <b>type='video/ogg; codecs="theora, speex"'</b>&gt;<br>
	 * Vorbis audio alone in Ogg container<br>
	 * &lt;source src='audio.ogg' <b>type='audio/ogg; codecs=vorbis'</b>&gt;<br>
	 * Speex audio alone in Ogg container<br>
	 * &lt;source src='audio.spx' <b>type='audio/ogg; codecs=speex'</b>&gt;<br>
	 * FLAC audio alone in Ogg container<br>
	 * &lt;source src='audio.oga' <b>type='audio/ogg; codecs=flac'</b>&gt;<br>
	 * Dirac video and Vorbis audio in Ogg container<br>
	 * &lt;source src='video.ogv' <b>type='video/ogg; codecs="dirac, vorbis"'</b>&gt;<br>
	 * Theora video and Vorbis audio in Matroska container<br>
	 * &lt;source src='video.mkv' <b>type='video/x-matroska; codecs="theora, vorbis"'</b>&gt;<br>
	 *
	 * @param type
	 *            the type of this media element
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * The media for which the content of this source should be shown
	 *
	 * @See {@link #setMedia(String)}
	 * @return The media for which the content of this source should be shown
	 */
	public String getMedia()
	{
		return media;
	}

	/**
	 * Sets the media for which the content of this source should be shown<br>
	 * <br>
	 *
	 * &lt;source src="movie.ogg" type="video/ogg" <b>media="screen and (min-width:320px)"&gt;<br>
	 *
	 * @param media
	 *            the media for which to content of this source should be shown
	 */
	public void setMedia(String media)
	{
		this.media = media;
	}

	/**
	 * Gets the page parameter applied to the URL of the source
	 * 
	 * @return the page parameter applied to the URL of the source
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * Sets the page parameter applied to the URL of the source
	 * 
	 * @param pageParameters
	 *            the page parameter which are going to be applied to the URL of the source
	 */
	public void setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
	}
}
