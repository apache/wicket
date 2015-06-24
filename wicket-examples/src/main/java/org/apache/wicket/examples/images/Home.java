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
package org.apache.wicket.examples.images;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.InlineImage;
import org.apache.wicket.markup.html.image.Picture;
import org.apache.wicket.markup.html.image.Source;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResource;
import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;


/**
 * Demonstrates different flavors of org.apache.wicket.examples.images.
 * 
 * @author Jonathan Locke
 */
public final class Home extends WicketExamplePage
{
	/**
	 * A dynamic image resource using {@link Home#drawCircle(Graphics2D)} to draw a random circle on
	 * the canvas.
	 * 
	 */
	private final class CircleDynamicImageResource extends RenderedDynamicImageResource
	{
		private CircleDynamicImageResource(int width, int height)
		{
			super(width, height);
		}

		@Override
		protected boolean render(Graphics2D graphics, Attributes attributes)
		{
			drawCircle(graphics);
			return true;
		}
	}

	private static final ResourceReference RESOURCE_REF = new PackageResourceReference(Home.class,
		"Image2.gif");

	/**
	 * Constructor
	 */
	public Home()
	{
		// Image as package resource
		add(new Image("image2", new PackageResourceReference(Home.class, "Image2.gif")));

		// Dynamically created image. Will re-render whenever resource is asked
		// for.
		add(new Image("image3", new CircleDynamicImageResource(100, 100)));

		// Simple model
		add(new Image("image4", new Model<>("Image2.gif")));

		// Dynamically created buffered image
		add(new Image("image5", getImage5Resource()));

		// Add okay button image
		add(new Image("okButton", getOkButtonImage()));

		// Add cancel button image
		add(new Image("cancelButton", new SharedResourceReference("cancelButton")));

		// image loaded as resource ref via model.
		add(new Image("imageModelResourceReference", new Model<>(RESOURCE_REF)));

		// image loaded as resource via model.
		add(new Image("imageModelResource", new Model<CircleDynamicImageResource>(
			new CircleDynamicImageResource(100, 100))));

		// responsive images (only for img tag)
		// the first package resource reference is used for the src attribute all following for the
		// srcset in the order they are given to the constructor
		Image respImage = new Image("image6", new PackageResourceReference(this.getClass(),
			"Image2_small.gif"), new PackageResourceReference(this.getClass(), "Image2_small.gif"),
			new PackageResourceReference(this.getClass(), "Image2_medium.gif"),
			new PackageResourceReference(this.getClass(), "Image2_large.gif"));
		// the x values are applied after each given package resource reference in the order they
		// are applied to the setter in the srcset attribute
		respImage.setXValues("320w", "2x", "900w");
		// The sizes are applied comma separated to the sizes attribute
		respImage.setSizes("(min-width: 50em) 33vw", "(min-width: 28em) 50vw", "100vw");
		this.add(respImage);

		// responsive images (to demonstrate the same picture is used for sources and img)
		Picture picture = new Picture("picture");
		Source large = new Source("sourcelarge", new PackageResourceReference(this.getClass(),
			"Image2_large.gif"));
		large.setMedia("(min-width: 650px)");
		large.setSizes("(min-width: 50em) 33vw");
		picture.add(large);
		large.setOutputMarkupId(true);
		Source medium = new Source("sourcemedium", new PackageResourceReference(this.getClass(),
			"Image2_medium.gif"));
		medium.setMedia("(min-width: 465px)");
		picture.add(medium);
		Image image3 = new Image("image7", new PackageResourceReference(this.getClass(),
			"Image2_small.gif"));
		picture.add(image3);
		this.add(picture);

		add(new InlineImage("inline", new PackageResourceReference(getClass(),"Image2.gif")));
	}

	/**
	 * @return Gets shared image component
	 */
	public ResourceReference getImage5Resource()
	{
		return new ResourceReference(Home.class, "image5")
		{
			@Override
			public IResource getResource()
			{
				final BufferedDynamicImageResource resource = new BufferedDynamicImageResource();
				final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
				drawCircle((Graphics2D)image.getGraphics());
				resource.setImage(image);
				return resource;
			}
		};
	}

	/**
	 * Draws a random circle on a graphics
	 * 
	 * @param graphics
	 *            The graphics to draw on
	 */
	void drawCircle(Graphics2D graphics)
	{
		// Compute random size for circle
		final Random random = new Random();
		int dx = Math.abs(10 + random.nextInt(80));
		int dy = Math.abs(10 + random.nextInt(80));
		int x = Math.abs(random.nextInt(100 - dx));
		int y = Math.abs(random.nextInt(100 - dy));

		// Draw circle with thick stroke width
		graphics.setStroke(new BasicStroke(5));
		graphics.drawOval(x, y, dx, dy);
	}

	final ResourceReference getOkButtonImage()
	{
		return new ResourceReference("okButton")
		{
			@Override
			public IResource getResource()
			{
				return new DefaultButtonImageResource("Ok");
			}
		};
	}
}
