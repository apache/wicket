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
package org.apache.wicket.markup.html.link;

import java.awt.geom.Rectangle2D;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.ImageTest;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * @since 1.5
 */
public class ClientSideImageMapPage_1 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public ClientSideImageMapPage_1()
	{
		final Image image = new Image("image", new PackageResourceReference(ImageTest.class,
			"Beer.gif"));
		add(image);
		final ClientSideImageMap map = new ClientSideImageMap("map", image);
		map.addCircleArea(new ExternalLink("wicketHomePage1", "http://wicket.apache.org"), 0, 0, 10);
		map.addRectangleArea(new ExternalLink("wicketHomePage2", "http://wicket.apache.org"), 0, 0,
			10, 10);
		map.addPolygonArea(new ExternalLink("wicketHomePage3", "http://wicket.apache.org"), 0, 0,
			10, 0, 10, 10, 0, 10);
		map.addShapeArea(new ExternalLink("wicketHomePage4", "http://wicket.apache.org"),
			new Rectangle2D.Float(0, 0, 10, 10));
		add(map);
	}
}
