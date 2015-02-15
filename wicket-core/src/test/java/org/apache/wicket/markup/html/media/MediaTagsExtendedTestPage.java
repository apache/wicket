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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.media.Track.Kind;
import org.apache.wicket.markup.html.media.video.Video;
import org.apache.wicket.request.resource.PackageResourceReference;

public class MediaTagsExtendedTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public MediaTagsExtendedTestPage()
	{
		Video video = new Video("video", new MediaStreamingResourceReference(
			MediaTagsTestPage.class, "dummyVideo.m4a"));
		
		// source tag
		Source source = new Source("source","http://www.mytestpage.xc/video.m4a");
		source.setMedia("screen and (device-width:500px)");
		source.setType("video/mp4");
		source.setDisplayType(true);
		video.add(source);
		
		// tack tag
		Track track = new Track("track", new PackageResourceReference(MediaTagsTestPage.class,"dummySubtitles.vtt"));
		track.setKind(Kind.subtitles);
		track.setLabel("Subtitles of video");
		track.setSrclang(Locale.GERMANY);
		track.setDefaultTrack(true);
		video.add(track);
		
		add(video);
	}

}
