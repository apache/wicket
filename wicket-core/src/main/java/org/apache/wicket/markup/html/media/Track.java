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

import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * The track tag is used to provide subtitles, captions, descriptions, chapters, metadata to a video
 * media component
 *
 * @author Tobias Soloschenko
 * @since 7.0.0
 */
public class Track extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * To be used for the kind attribute
	 */
	public enum Kind {
		/**
		 * the track is used for subtitles
		 */
		SUBTITLES("subtitles"),
		/**
		 * the track is used for captions
		 */
		CAPTIONS("captions"),
		/**
		 * the track is used for descriptions
		 */
		DESCRIPTIONS("descriptions"),
		/**
		 * the track is used for chapters
		 */
		CHAPTERS("chapters"),
		/**
		 * the track is used to provide metadata
		 */
		METADATA("metadata");

		private String realName;

		Kind(String realName)
		{
			this.realName = realName;
		}

		/**
		 * The real name of the kind
		 * 
		 * @return the real name
		 */
		public String getRealName()
		{
			return realName;
		}
	}

	private Kind kind;

	private String label;

	private boolean defaultTrack;

	private Locale srclang;

	private final ResourceReference resourceReference;

	private final String url;

	private PageParameters pageParameters;

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 */
	public Track(String id)
	{
		this(id, null, null, null, null);
	}

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 */
	public Track(String id, IModel<?> model)
	{
		this(id, model, null, null, null);
	}

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 * @param resourceReference
	 *            the resource reference to provide track information - like .vtt
	 */
	public Track(String id, ResourceReference resourceReference)
	{
		this(id, null, null, null, resourceReference);
	}

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference to provide track information - like .vtt
	 */
	public Track(String id, IModel<?> model, ResourceReference resourceReference)
	{
		this(id, model, null, null, resourceReference);
	}

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 * @param resourceReference
	 *            the resource reference to provide track information - like .vtt
	 * @param pageParameters
	 *            the page parameters applied to the track URL
	 */
	public Track(String id, ResourceReference resourceReference, PageParameters pageParameters)
	{
		this(id, null, null, pageParameters, resourceReference);
	}

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference to provide track information - like .vtt
	 * @param pageParameters
	 *            the page parameters applied to the track URL
	 */
	public Track(String id, IModel<?> model, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		this(id, model, null, pageParameters, resourceReference);
	}

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 * @param url
	 *            an external URL to provide the track information
	 */
	public Track(String id, String url)
	{
		this(id, null, url, null, null);
	}

	/**
	 * Creates a track
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to provide the track information
	 */
	public Track(String id, IModel<?> model, String url)
	{
		this(id, model, url, null, null);
	}

	private Track(String id, IModel<?> model, String url, PageParameters pageParameters,
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
		checkComponentTag(tag, "track");
		super.onComponentTag(tag);

		if (resourceReference != null)
		{
			tag.put("src", RequestCycle.get().urlFor(resourceReference, pageParameters));
		}
		else if (url != null)
		{
			tag.put("src", url);
		}

		Kind _kind = getKind();
		if (_kind != null)
		{
			tag.put("kind", _kind.getRealName());
		}

		String _label = getLabel();
		if (_label != null)
		{
			tag.put("label", _label);
		}

		if (defaultTrack)
		{
			tag.put("default", "default");
		}

		// if the srclang field is set use this, else if the
		// resource reference provides a locale use the language
		// of the resource reference
		Locale _srclang = getSrclang();
		if (_srclang != null)
		{
			tag.put("srclang", _srclang.getLanguage());
		}
		else if (resourceReference != null && resourceReference.getLocale() != null)
		{
			tag.put("srclang", resourceReference.getLocale().getLanguage());
		}
	}

	/**
	 * Gets the kind of the track belongs to the media component
	 *
	 * @see {@link #setKind(Kind)}
	 *
	 * @return the kind
	 */
	public Kind getKind()
	{
		return kind;
	}

	/**
	 * Sets the kind of the track belongs to the media component<br>
	 * <br>
	 * <b>SUBTITLES</b>: Transcription or translation of the dialogue, suitable for when the sound
	 * is available but not understood (e.g. because the user does not understand the language of
	 * the media resource's soundtrack). Displayed over the video.<br>
	 * <br>
	 * <b>CAPTIONS</b>: Transcription or translation of the dialogue, sound effects, relevant
	 * musical cues, and other relevant audio information, suitable for when the soundtrack is
	 * unavailable (e.g. because it is muted or because the user is deaf). Displayed over the video;
	 * labeled as appropriate for the hard-of-hearing.<br>
	 * <br>
	 * <b>DESCRIPTIONS</b>: Textual descriptions of the video component of the media resource,
	 * intended for audio synthesis when the visual component is unavailable (e.g. because the user
	 * is interacting with the application without a screen while driving, or because the user is
	 * blind). Synthesized as separate audio track.<br>
	 * <br>
	 * <b>CHAPTERS</b>: Chapter titles, intended to be used for navigating the media resource.
	 * Displayed as an interactive list in the user agent's interface.<br>
	 * <br>
	 * <b>METADATA</b>: Tracks intended for use from script. Not displayed by the user agent.<br>
	 * <br>
	 *
	 * @param kind
	 *            the kind
	 */
	public void setKind(Kind kind)
	{
		this.kind = kind;
	}

	/**
	 * The label for this track
	 *
	 * @return the label
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Sets the label for this track
	 *
	 * @param label
	 *            the label to be set
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * If the track is the default track
	 *
	 * @return if the track is the default track
	 */
	public boolean isDefaultTrack()
	{
		return defaultTrack;
	}

	/**
	 * Sets if this track is the default track
	 *
	 * @param defaultTrack
	 *            if the track is the default track
	 */
	public void setDefaultTrack(Boolean defaultTrack)
	{
		this.defaultTrack = defaultTrack;
	}

	/**
	 * Gets the src lang
	 *
	 * @return the src lang
	 */
	public Locale getSrclang()
	{
		return srclang;
	}

	/**
	 * Sets the src lang
	 *
	 * @param srclang
	 *            the src lang to set
	 */
	public void setSrclang(Locale srclang)
	{
		this.srclang = srclang;
	}

	/**
	 * Gets the page parameter applied to the URL of the track
	 * 
	 * @return the page parameter applied to the URL of the track
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * Sets the page parameter applied to the URL of the track
	 * 
	 * @param pageParameters
	 *            the page parameter which are going to be applied to the URL of the track
	 */
	public void setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
	}
}
