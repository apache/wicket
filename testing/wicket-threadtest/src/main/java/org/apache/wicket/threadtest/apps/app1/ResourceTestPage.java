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
package org.apache.wicket.threadtest.apps.app1;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.resource.DynamicImageResource;

/**
 * Web page with 50 dynamically-created image resources.
 * 
 * @author almaw
 */
public class ResourceTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	/**
	 * Defines the number of images per page
	 */
	public static final int IMAGES_PER_PAGE = 20;

	/**
	 * Construct.
	 */
	public ResourceTestPage()
	{
		List<Object> list = Arrays.asList(new Object[IMAGES_PER_PAGE]);
		add(new ListView<Object>("listView", list)
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Object> item)
			{
				final Random random = new Random();
				BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
				Graphics gfx = image.getGraphics();
				gfx.setColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()));
				gfx.fillRect(0, 0, 32, 32);
				gfx.dispose();

				// Write it into a byte array as a JPEG.
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try
				{
					ImageIO.write(image, "jpeg", baos);
				}
				catch (Exception e)
				{
					throw new WicketRuntimeException(e);
				}

				final byte[] imageData = baos.toByteArray();

				item.add(new Image("image", new DynamicImageResource("jpeg")
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected byte[] getImageData(Attributes attributes)
					{
						return imageData;
					}
				}));
			}

		});
	}
}
