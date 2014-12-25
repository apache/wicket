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

public class ImagePictureTestPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public ImagePictureTestPage() {
	Picture picture = new Picture("picture");

	Source large = new Source("sourcelarge", new PackageResourceReference(this.getClass(), "large.jpg"));
	large.setMedia("(min-width: 650px)");
	large.setSizes("(min-width: 50em) 33vw");
	picture.addSource(large);
	large.setOutputMarkupId(true);

	Source medium = new Source("sourcemedium",new PackageResourceReference(this.getClass(), "medium.jpg"));
	medium.setMedia("(min-width: 465px)");
	picture.addSource(medium);

	Image image3 = new Image("image3", new PackageResourceReference(this.getClass(), "small.jpg"));
	picture.addImage(image3);

	this.add(picture);
    }
}
