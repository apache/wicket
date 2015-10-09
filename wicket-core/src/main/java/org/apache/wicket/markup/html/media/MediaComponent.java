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
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * The media component is used to provide basic functionality to the video and audio component.
 *
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 * @since 7.0.0
 */
public abstract class MediaComponent extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * To be used for the <em>crossorigin</em> attribute
	 *
	 * @see {@link #setCrossOrigin(Cors)}
	 */
	public enum Cors {
		/**
		 * no authentication required
		 */
		ANONYMOUS("anonymous"),
		/**
		 * user credentials required
		 */
		USER_CREDENTIALS("user-credentials"),
		/**
		 * no cross origin
		 */
		NO_CORS("");

		private final String realName;

		private Cors(String realName)
		{
			this.realName = realName;
		}

		/**
		 * Gets the real name for the cors option
		 * 
		 * @return the real name
		 */
		public String getRealName()
		{
			return realName;
		}
	}

	/**
	 * To be used for the <em>preload</em> attribute
	 *
	 * @see {@link #setPreload(Preload)}
	 */
	public enum Preload {
		/**
		 * preloads nothing
		 */
		NONE("none"),
		/**
		 * preloads only meta data like first picture, etc.
		 */
		METADATA("metadata"),
		/**
		 * auto detection what is going to be preload
		 */
		AUTO("auto");

		private final String realName;

		private Preload(String realname)
		{
			realName = realname;
		}

		/**
		 * Gets the real name for the preload option
		 * 
		 * @return the real name
		 */
		public String getRealName()
		{
			return realName;
		}
	}

	private boolean autoplay;

	private boolean loop;

	private boolean muted;

	private boolean controls = true;

	private Preload preload;

	private String startTime;

	private String endTime;

	private String mediaGroup;

	private Cors crossOrigin;

	private PageParameters pageParameters;

	private final ResourceReference resourceReference;

	private final String url;

	/**
	 * Constructor.
	 *
	 * @param id
	 *            The component id
	 */
	public MediaComponent(String id)
	{
		this(id, null, null, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            The component id
	 * @param model
	 *            The component model
	 */
	public MediaComponent(String id, IModel<?> model)
	{
		this(id, model, null, null, null);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            The component id
	 * @param resourceReference
	 *            the resource reference of the media file
	 */
	public MediaComponent(String id, ResourceReference resourceReference)
	{
		this(id, null, null, null, resourceReference);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            The component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference of the media file
	 */
	public MediaComponent(String id, IModel<?> model, ResourceReference resourceReference)
	{
		this(id, model, null, null, resourceReference);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            The component id
	 * @param resourceReference
	 *            the resource reference of the media file
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the media URL
	 */
	public MediaComponent(String id, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		this(id, null, null, pageParameters, resourceReference);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            The component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference of the media file
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the media URL
	 */
	public MediaComponent(String id, IModel<?> model, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		this(id, model, null, pageParameters, resourceReference);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            The component id
	 * @param url
	 *            an external URL to be used for the media component
	 */
	public MediaComponent(String id, String url)
	{
		this(id, null, url, null, null);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            The component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to be used for the media component
	 */
	public MediaComponent(String id, IModel<?> model, String url)
	{
		this(id, model, url, null, null);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            The component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to be used for the media component
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the media URL
	 */
	public MediaComponent(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		this(id, model, url, pageParameters, null);
	}

	private MediaComponent(String id, IModel<?> model, String url, PageParameters pageParameters,
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
		super.onComponentTag(tag);

		// The time management is used to set the start / stop
		// time in seconds of the movie to be played back
		String timeManagement = "";
		if (startTime != null)
		{
			timeManagement += "#t=" + startTime + (endTime != null ? "," + endTime : "");
		}

		if (resourceReference != null)
		{
			CharSequence urlToMediaReference = RequestCycle.get().urlFor(resourceReference,
				pageParameters);
			tag.put("src", urlToMediaReference + timeManagement);
		}
		else if (url != null)
		{
			Url encoded = new PageParametersEncoder().encodePageParameters(pageParameters);
			String queryString = encoded.getQueryString();
			tag.put("src", url + (queryString != null ? "?" + queryString : "") + timeManagement);
		}

		String mg = getMediaGroup();
		if (mg != null)
		{
			tag.put("mediagroup", mg);
		}

		if (isAutoplay())
		{
			tag.put("autoplay", "autoplay");
		}

		if (isLooping())
		{
			tag.put("loop", "loop");
		}

		if (isMuted())
		{
			tag.put("muted", "muted");
		}

		if (hasControls())
		{
			tag.put("controls", "controls");
		}

		Preload _preload = getPreload();
		if (_preload != null)
		{
			tag.put("preload", _preload.getRealName());
		}

		Cors cors = getCrossOrigin();
		if (cors != null)
		{
			tag.put("crossorigin", cors.getRealName());
		}
	}

	/**
	 * If the playback is autoplayed on load
	 *
	 * @return If the playback is autoplayed on load
	 */
	public boolean isAutoplay()
	{
		return autoplay;
	}

	/**
	 * Sets the playback to be autoplayed on load
	 *
	 * @param autoplay
	 *            If the playback is autoplayed on load
	 */
	public void setAutoplay(boolean autoplay)
	{
		this.autoplay = autoplay;
	}

	/**
	 * If the playback is looped
	 *
	 * @return If the playback is looped
	 */
	public boolean isLooping()
	{
		return loop;
	}

	/**
	 * Sets the playback to be looped
	 *
	 * @param loop
	 *            If the playback is looped
	 */
	public void setLooping(boolean loop)
	{
		this.loop = loop;
	}

	/**
	 * Gets the page parameter applied to the URL of the media component
	 * 
	 * @return the page parameter applied to the URL of the media component
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * Sets the page parameter applied to the URL of the media component
	 * 
	 * @param pageParameters
	 *            the page parameter which are going to be applied to the URL of the media component
	 */
	public void setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
	}

	/**
	 * If the playback is muted initially
	 *
	 * @return If the playback is muted initially
	 */
	public boolean isMuted()
	{
		return muted;
	}

	/**
	 * Sets the playback muted initially
	 *
	 * @param muted
	 *            If the playback is muted initially
	 */
	public void setMuted(boolean muted)
	{
		this.muted = muted;
	}

	/**
	 * If the controls are going to be displayed
	 *
	 * @return if the controls are going to displayed
	 */
	public boolean hasControls()
	{
		return controls;
	}

	/**
	 * Sets if the controls are going to be displayed
	 *
	 * @param controls
	 *            if the controls are going to displayed
	 */
	public void setControls(Boolean controls)
	{
		this.controls = controls;
	}

	/**
	 * The type of preload
	 *
	 * @see {@link #setPreload(Preload)}
	 *
	 * @return the preload
	 */
	public Preload getPreload()
	{
		return preload;
	}

	/**
	 * Sets the type of preload.
	 * <ul>
	 * <li><b>none</b>: Hints to the user agent that either the author does not expect the user to
	 * need the media resource, or that the server wants to minimise unnecessary traffic.</li>
	 *
	 * <li><b>metadata</b>: Hints to the user agent that the author does not expect the user to need
	 * the media resource, but that fetching the resource metadata (dimensions, first frame, track
	 * list, duration, etc) is reasonable.</li>
	 *
	 * <li><b>auto</b>: Hints to the user agent that the user agent can put the user's needs first
	 * without risk to the server, up to and including optimistically downloading the entire
	 * resource.</li>
	 * </ul>
	 * </p>
	 *
	 * @param preload
	 *            the type of the preload
	 */
	public void setPreload(Preload preload)
	{
		this.preload = preload;
	}

	/**
	 * Gets the position at which the media component starts the playback
	 *
	 * @see {@link #setStartTime(String)}
	 *
	 * @return the time at which position the media component starts the playback
	 */
	public String getStartTime()
	{
		return startTime;
	}

	/**
	 * Sets the position at which the media component starts the playback<br>
	 * <br>
	 * t=<b>10</b>,20<br>
	 * t=<b>npt:10</b>,20<br>
	 * <br>
	 *
	 * t=<b>120s</b>,121.5s<br>
	 * t=<b>npt:120</b>,0:02:01.5<br>
	 * <br>
	 *
	 * t=<b>smpte-30:0:02:00</b>,0:02:01:15<br>
	 * t=<b>smpte-25:0:02:00:00</b>,0:02:01:12.1<br>
	 * <br>
	 *
	 * t=<b>clock:20090726T111901Z</b>,20090726T121901Z
	 *
	 * @param startTime
	 *            the time at which position the media component starts the playback
	 */
	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * Gets the position at which the media component stops the playback
	 *
	 * @see {@link #setEndTime(String)}
	 *
	 * @return the time at which position the media component stops the playback
	 */
	public String getEndTime()
	{
		return endTime;
	}

	/**
	 * Sets the position at which the media component stops the playback<br>
	 * <br>
	 * t=10,<b>20</b><br>
	 * t=npt:10,<b>20</b><br>
	 * <br>
	 *
	 * t=120s,<b>121.5s</b><br>
	 * t=npt:120,<b>0:02:01.5</b><br>
	 * <br>
	 *
	 * t=smpte-30:0:02:00,<b>0:02:01:15</b><br>
	 * t=smpte-25:0:02:00:00,<b>0:02:01:12.1</b><br>
	 * <br>
	 *
	 * t=clock:20090726T111901Z,<b>20090726T121901Z</b>
	 *
	 * @param endTime
	 *            the time at which position the media component stops the playback
	 */
	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}

	/**
	 * Gets the media group.
	 *
	 * @return the media group
	 */
	public String getMediaGroup()
	{
		return mediaGroup;
	}

	/**
	 * Sets the media group
	 *
	 * @param mediaGroup
	 *            to be set
	 */
	public void setMediaGroup(String mediaGroup)
	{
		this.mediaGroup = mediaGroup;
	}

	/**
	 * Gets the cross origin settings
	 *
	 * @see {@link #setCrossOrigin(Cors)}
	 *
	 * @return the cross origins settings
	 */
	public Cors getCrossOrigin()
	{
		return crossOrigin;
	}

	/**
	 * Sets the cross origin settings<br>
	 * <br>
	 *
	 * <b>ANONYMOUS</b>: Cross-origin CORS requests for the element will not have the credentials
	 * flag set.<br>
	 * <br>
	 * <b>USER_CREDENTIALS</b>: Cross-origin CORS requests for the element will have the credentials
	 * flag set.<br>
	 * <br>
	 * <b>NO_CORS</b>: The empty string is also a valid keyword, and maps to the Anonymous state.
	 * The attribute's invalid value default is the Anonymous state. The missing value default, used
	 * when the attribute is omitted, is the No CORS state
	 *
	 * @param crossOrigin
	 *            the cross origins settings to set
	 */
	public void setCrossOrigin(Cors crossOrigin)
	{
		this.crossOrigin = crossOrigin;
	}
}
