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
 * 
 */
public class Track extends WebMarkupContainer
{

	private static final long serialVersionUID = 1L;

	private Kind kind;

	private String label;

	private Boolean defaultTrack;

	private Locale srclang;

	private ResourceReference resourceReference;

	private String url;

	private PageParameters pageParameters;

	public Track(String id)
	{
		super(id);
	}

	public Track(String id, IModel<?> model)
	{
		super(id, model);
	}

	public Track(String id, ResourceReference resourceReference)
	{
		this(id);
		this.resourceReference = resourceReference;
	}

	public Track(String id, IModel<?> model, ResourceReference resourceReference)
	{
		this(id, model);
		this.resourceReference = resourceReference;
	}

	public Track(String id, ResourceReference resourceReference, PageParameters pageParameters)
	{
		this(id);
		this.resourceReference = resourceReference;
		this.pageParameters = pageParameters;
	}

	public Track(String id, IModel<?> model, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		this(id, model);
		this.resourceReference = resourceReference;
		this.pageParameters = pageParameters;
	}

	public Track(String id, String url)
	{
		this(id);
		this.url = url;
	}

	public Track(String id, IModel<?> model, String url)
	{
		this(id, model);
		this.url = url;
	}

	public Track(String id, String url, PageParameters pageParameters)
	{
		this(id);
		this.url = url;
		this.pageParameters = pageParameters;
	}

	public Track(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		this(id, model);
		this.url = url;
		this.pageParameters = pageParameters;
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

		if (url != null)
		{
			tag.put("src", url);
		}

		if (kind != null)
		{
			tag.put("kind", kind.name());
		}

		if (label != null)
		{
			tag.put("label", label);
		}

		if (defaultTrack != null && defaultTrack)
		{
			tag.put("default", "default");
		}

		// if the srclang field is set use this, else if the
		// resource reference provides a locale use the language
		// of the resource reference
		if (srclang != null)
		{
			tag.put("srclang", srclang.getLanguage());
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
	 * <b>subtitles</b>: Transcription or translation of the dialogue, suitable for when the sound
	 * is available but not understood (e.g. because the user does not understand the language of
	 * the media resource's soundtrack). Displayed over the video.<br>
	 * <br>
	 * <b>captions</b>: Transcription or translation of the dialogue, sound effects, relevant
	 * musical cues, and other relevant audio information, suitable for when the soundtrack is
	 * unavailable (e.g. because it is muted or because the user is deaf). Displayed over the video;
	 * labeled as appropriate for the hard-of-hearing.<br>
	 * <br>
	 * <b>descriptions</b>: Textual descriptions of the video component of the media resource,
	 * intended for audio synthesis when the visual component is unavailable (e.g. because the user
	 * is interacting with the application without a screen while driving, or because the user is
	 * blind). Synthesized as separate audio track.<br>
	 * <br>
	 * <b>chapters</b>: Chapter titles, intended to be used for navigating the media resource.
	 * Displayed as an interactive list in the user agent's interface.<br>
	 * <br>
	 * <b>metadata</b>: Tracks intended for use from script. Not displayed by the user agent.<br>
	 * <br>
	 * 
	 * @param the
	 *            kind
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
	public Boolean getDefaultTrack()
	{
		return defaultTrack != null ? defaultTrack : false;
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
	 * To be used for the kind attribute
	 */
	public enum Kind
	{
		subtitles, captions, descriptions, chapters, metadata
	}
}
