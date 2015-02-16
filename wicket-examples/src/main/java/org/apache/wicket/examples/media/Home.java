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
package org.apache.wicket.examples.media;

import java.util.UUID;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.media.MediaComponent.Cors;
import org.apache.wicket.markup.html.media.MediaStreamingResourceReference;
import org.apache.wicket.markup.html.media.Source;
import org.apache.wicket.markup.html.media.video.Video;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;


/**
 * Demonstrates different flavors of org.apache.wicket.examples.videos.<br>
 * <br>
 * 
 * Videos are from: http://media.w3.org/2010/05/video/<br>
 * <br>
 * Images are from: http://search.creativecommons.org/ with check on commercial use and modify...
 * 
 * @author Tobias Soloschenko
 */
public final class Home extends WicketExamplePage
{

	/**
	 * Constructor
	 */
	public Home()
	{
		// Internal video with several options

		Video video1 = new Video("video1", new MediaStreamingResourceReference(Home.class,
			"video.mp4"));
		video1.setAutoplay(false);
		video1.setControls(true);
		video1.setLooping(false);
		video1.setWidth(320);
		video1.setHeight(240);
		video1.setPoster(new PackageResourceReference(Home.class, "novideo.gif"));
		add(video1);

		// External video
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("random", UUID.randomUUID().toString());
		pageParameters.add("test", "test");
		Video video2 = new Video("video2", "http://media.w3.org/2010/05/video/movie_300.mp4",
			pageParameters);
		video2.setCrossOrigin(Cors.ANONYMOUS);
		add(video2);

		// video with source

		Video video3 = new Video("video3");
		video3.setPoster(new PackageResourceReference(Home.class, "novideo.gif"));

		Source source3 = new Source("source3", new MediaStreamingResourceReference(Home.class,
			"video.mp4"));
		// Need to be set to true to show the type
		source3.setDisplayType(true);
		// the default type is the mime type of the image with no codec information
		source3.setType("video/mp4; codecs=\"avc1.42E01E, mp4a.40.2\"");
		video3.add(source3);

		add(video3);

		// video with track

		/*
		 * Video video4 = new Video("video4", new MediaStreamingResourceReference(Home.class,
		 * "dummyVideo.m4a"));
		 * 
		 * // source tag Source source4 = new Source("source4",
		 * "http://www.mytestpage.xc/video.m4a");
		 * source4.setMedia("screen and (device-width:500px)"); source4.setType("video/mp4");
		 * source4.setDisplayType(true); video4.add(source4);
		 * 
		 * // tack tag Track track4 = new Track("track4", new PackageResourceReference(Home.class,
		 * "dummySubtitles.vtt")); track4.setKind(Kind.subtitles);
		 * track4.setLabel("Subtitles of video"); track4.setSrclang(Locale.GERMANY);
		 * track4.setDefaultTrack(true); video4.add(track4);
		 * 
		 * add(video4);
		 */
	}
}
