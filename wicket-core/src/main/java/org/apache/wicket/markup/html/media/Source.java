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

/**
 * The source of an audio or a video media component
 * 
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 * 
 */
public class Source extends WebMarkupContainer
{

	private static final long serialVersionUID = 1L;

	private Boolean displayType;

	private String type;

	private String media;

	private MediaStreamingResourceReference mediaStreamingResourceReference;

	private PageParameters pageParameters;

	private String url;

	public Source(String id)
	{
		super(id);
	}

	public Source(String id, IModel<?> model)
	{
		super(id, model);
	}

	public Source(String id, MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		this(id);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
	}

	public Source(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		this(id, model);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
	}

	public Source(String id, MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		this(id);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
		this.pageParameters = pageParameters;
	}

	public Source(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		this(id, model);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
		this.pageParameters = pageParameters;
	}

	public Source(String id, String url)
	{
		this(id);
		this.url = url;
	}

	public Source(String id, IModel<?> model, String url)
	{
		this(id, model);
		this.url = url;
	}

	public Source(String id, String url, PageParameters pageParameters)
	{
		this(id);
		this.url = url;
		this.pageParameters = pageParameters;
	}

	public Source(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		this(id, model);
		this.url = url;
		this.pageParameters = pageParameters;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		this.checkComponentTag(tag, "source");
		super.onComponentTag(tag);

		if (this.mediaStreamingResourceReference != null)
		{
			tag.put("src",
				RequestCycle.get()
					.urlFor(this.mediaStreamingResourceReference, this.pageParameters));
		}

		if (this.url != null)
		{
			tag.put("src", this.url);
		}

		if (this.getDisplayType())
		{
			if (this.type != null)
			{
				tag.put("type", this.type);
			}
			else if (this.mediaStreamingResourceReference != null)
			{
				tag.put("type", this.mediaStreamingResourceReference.getType());
			}
		}

		if (this.media != null)
		{
			tag.put("media", this.media);
		}

	}

	/**
	 * If the type is going to be displayed
	 * 
	 * @return If the type is going to be displayed
	 */
	public Boolean getDisplayType()
	{
		return this.displayType != null ? this.displayType : false;
	}

	/**
	 * Sets if the type is going to be displayed
	 * 
	 * @param displayType
	 *            if the type is going to be displayed
	 */
	public void setDisplayType(Boolean displayType)
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
		return this.type;
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
		return this.media;
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
}
