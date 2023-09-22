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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.PackageResourceReference;

public class ImageSrcSetTestPage extends WebPage
{

	private static final long serialVersionUID = 1L;

	public ImageSrcSetTestPage()
	{
		Image image2 = new Image("image2", new PackageResourceReference(this.getClass(),
			"small.jpg"), new PackageResourceReference(this.getClass(), "small.jpg"),
			new PackageResourceReference(this.getClass(), "medium.jpg"),
			new PackageResourceReference(this.getClass(), "large.jpg"));
		image2.setXValues("320w", "2x", "900w");
		image2.setSizes("(min-width: 50em) 33vw", "(min-width: 28em) 50vw", "100vw");
		this.add(image2);
	}
}
