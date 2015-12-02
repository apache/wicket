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

import java.util.Arrays;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * page to test the external components
 * 
 * @author Tobias Soloschenko
 *
 */
public class ExternalImageTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the external test page
	 */
	public ExternalImageTestPage()
	{
		add(new ExternalImage("externalImage1", "http://wicket.apache.org/img/wicket-7-bg.jpg"));
		ExternalImage externalImage2 = new ExternalImage("externalImage2",
			"http://wicket.apache.org/img/wicket-7-bg.jpg",
			Arrays.asList("http://wicket.apache.org/img/wicket-7-bg-1.jpg",
				"http://wicket.apache.org/img/wicket-7-bg-2.jpg"));
		externalImage2.setSizes("s1", "s2");
		externalImage2.setXValues("x1", "x2");
		add(externalImage2);
		Picture picture = new Picture("externalPicture");
		ExternalSource externalSource = new ExternalSource("externalSource",
			Model.ofList(Arrays.asList("http://wicket.apache.org/img/wicket-7-bg-1.jpg",
				"http://wicket.apache.org/img/wicket-7-bg-2.jpg")));
		externalSource.setXValues("", "x2");
		externalSource.setSizes("1");
		externalSource.setMedia("(min-width: 650px)");
		picture.add(externalSource);
		ExternalImage externalImage3 = new ExternalImage("externalImage3",
			"http://wicket.apache.org/img/wicket-7-bg.jpg",
			Arrays.asList("http://wicket.apache.org/img/wicket-7-bg-1.jpg",
				"http://wicket.apache.org/img/wicket-7-bg-2.jpg"));
		picture.add(externalImage3);
		add(picture);
	}
}
