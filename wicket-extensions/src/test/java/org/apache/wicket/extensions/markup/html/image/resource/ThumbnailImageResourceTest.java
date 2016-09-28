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
package org.apache.wicket.extensions.markup.html.image.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResource;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * @author Pedro Santos
 */
public class ThumbnailImageResourceTest extends WicketTestCase
{
	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3649">WICKET-3649</a>
	 * 
	 * @throws IOException
	 */
	@Test
	public void testThumbnailImageResource() throws IOException
	{
		DefaultButtonImageResource unscaled = new DefaultButtonImageResource(60, 60, "");
		unscaled.setFormat("jpg");
		ThumbnailImageResource scaledImageResource = new ThumbnailImageResource(unscaled, 30);

		tester.startResource(scaledImageResource);

		byte[] response = tester.getLastResponse().getBinaryContent();
		InputStream in = new ByteArrayInputStream(response);
		BufferedImage scaledImage = ImageIO.read(in);

		assertEquals(30, scaledImage.getWidth());
		assertEquals(30, scaledImage.getHeight());
	}
}
