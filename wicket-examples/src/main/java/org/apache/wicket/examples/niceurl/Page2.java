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
package org.apache.wicket.examples.niceurl;

import java.util.Random;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Simple bookmarkable page that displays page parameters.
 * 
 * @author Igor Vaynberg
 */
public class Page2 extends WicketExamplePage
{
	private final Random random = new Random();

	/**
	 * Constructor
	 * 
	 * @param parameters
	 */
	public Page2(PageParameters parameters)
	{
		String p1 = parameters.get("param1").toString("CANNOT RESOLVE FROM URL");
		String p2 = parameters.get("param2").toString("CANNOT RESOLVE FROM URL");
		add(new Label("p1", p1));
		add(new Label("p2", p2));

		String newP1 = String.valueOf(random.nextInt());
		String newP2 = String.valueOf(random.nextInt());

		PageParameters params = new PageParameters();
		params.set("param1", newP1 + " " + newP2);
		params.set("param2", newP2 + " " + newP1);

		BookmarkablePageLink<?> link = new BookmarkablePageLink<>("refreshLink", getClass(), params);
		add(link);

		add(new BookmarkablePageLink<>("homeLink", Home.class));
	}
}
