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
 * The media component is used to provide basic functionality to the video and audo component. The
 * given media streaming resource reference supports Content-Ranges and other stuff to make the
 * audio and video playback smooth.
 * 
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 */
public abstract class MediaComponent extends WebMarkupContainer
{

	private static final long serialVersionUID = 1L;

	/**
	 * To be used for the crossorigin attribute
	 * 
	 * @see {@link #setCrossOrigin(Cors)}
	 */
	public enum Cors
	{
		ANONYMOUS("anonymous"), USER_CREDENTIALS("user-credentials"), NO_CORS("");

		private String realName;

		private Cors(String realName)
		{
			this.realName = realName;
		}

		public String getRealName()
		{
			return realName;
		}
	}

	/**
	 * To be used for the preload attribute
	 * 
	 * @see {@link #setPreload(Preload)}
	 */
	public enum Preload
	{
		NONE("none"), METADATA("metadata"), AUTO("auto");

		public String realName;

		private Preload(String realname)
		{
			realName = realname;
		}

		public String getRealName()
		{
			return realName;
		}
	}

	// use Boolean instead of elementary data types to get a lightweight component
	private Boolean autoplay;

	private Boolean loop;

	private Boolean muted;

	private Boolean controls;

	private Preload preload;

	private String startTime;

	private String endTime;

	private String mediaGroup;

	private Cors crossOrigin;

	private PageParameters pageParameters;

	private MediaStreamingResourceReference mediaStreamingResourceReference;

	private String url;

	public MediaComponent(String id)
	{
		super(id);
	}

	public MediaComponent(String id, IModel<?> model)
	{
		super(id, model);
	}

	public MediaComponent(String id, MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		this(id);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
	}

	public MediaComponent(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		this(id, model);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
	}

	public MediaComponent(String id,
		MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		this(id);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
		this.pageParameters = pageParameters;
	}

	public MediaComponent(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		this(id, model);
		this.mediaStreamingResourceReference = mediaStreamingResourceReference;
		this.pageParameters = pageParameters;
	}

	public MediaComponent(String id, String url)
	{
		this(id);
		this.url = url;
	}

	public MediaComponent(String id, IModel<?> model, String url)
	{
		this(id, model);
		this.url = url;
	}

	public MediaComponent(String id, String url, PageParameters pageParameters)
	{
		this(id);
		this.url = url;
		this.pageParameters = pageParameters;
	}

	public MediaComponent(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		this(id, model);
		this.url = url;
		this.pageParameters = pageParameters;
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
			timeManagement = timeManagement += "#t=" + startTime +
				(endTime != null ? "," + endTime : "");
		}

		if (mediaStreamingResourceReference != null)
		{
			tag.put("src",
				RequestCycle.get().urlFor(mediaStreamingResourceReference, pageParameters) +
					timeManagement);
		}

		if (url != null)
		{
			tag.put("src", url + timeManagement);
		}

		if (mediaGroup != null)
		{
			tag.put("mediagroup", mediaGroup);
		}

		if (autoplay != null && autoplay)
		{
			tag.put("autoplay", "autoplay");
		}

		if (loop != null && loop)
		{
			tag.put("loop", "loop");
		}

		if (muted != null && muted)
		{
			tag.put("muted", "muted");
		}

		// Use getter here because controls should be visible by default
		if (getControls())
		{
			tag.put("controls", "controls");
		}

		if (preload != null)
		{
			tag.put("preload", preload.getRealName());
		}

		if (crossOrigin != null)
		{
			tag.put("crossorigin", crossOrigin.getRealName());
		}
	}

	/**
	 * If the playback is autoplayed on load
	 * 
	 * @return If the playback is autoplayed on load
	 */
	public Boolean getAutoplay()
	{
		return autoplay != null ? autoplay : false;
	}

	/**
	 * Sets the playback to be autoplayed on load
	 * 
	 * @param autoplay
	 *            If the playback is autoplayed on load
	 */
	public void setAutoplay(Boolean autoplay)
	{
		this.autoplay = autoplay;
	}

	/**
	 * If the playback is looped
	 * 
	 * @return If the playback is looped
	 */
	public Boolean getLoop()
	{
		return loop != null ? loop : false;
	}

	/**
	 * Sets the playback to be looped
	 * 
	 * @param loop
	 *            If the playback is looped
	 */
	public void setLoop(Boolean loop)
	{
		this.loop = loop;
	}

	/**
	 * If the playback is muted initially
	 * 
	 * @return If the playback is muted initially
	 */
	public Boolean getMuted()
	{
		return muted != null ? muted : false;
	}

	/**
	 * Sets the playback muted initially
	 * 
	 * @param muted
	 *            If the playback is muted initially
	 */
	public void setMuted(Boolean muted)
	{
		this.muted = muted;
	}

	/**
	 * If the controls are going to be displayed
	 * 
	 * @return if the controls are going to displayed
	 */
	public Boolean getControls()
	{
		return controls != null ? controls : true;
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
	 * Sets the type of preload <br>
	 * <br>
	 * <b>none</b>: Hints to the user agent that either the author does not expect the user to need
	 * the media resource, or that the server wants to minimise unnecessary traffic.<br>
	 * <br>
	 * <b>metadata</b>: Hints to the user agent that the author does not expect the user to need the
	 * media resource, but that fetching the resource metadata (dimensions, first frame, track list,
	 * duration, etc) is reasonable.<br>
	 * <br>
	 * <b>auto</b>: Hints to the user agent that the user agent can put the user's needs first
	 * without risk to the server, up to and including optimistically downloading the entire
	 * resource.
	 * 
	 * @param preload
	 *            the preload
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
