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
package org.apache.wicket.extensions.captcha.kittens;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A unique and fun-to-use captcha technique I developed at Thoof.
 * 
 * @author Jonathan Locke
 */
public class KittenCaptchaPanel extends Panel
{
	private static final long serialVersionUID = 2711167040323855070L;

	private static final Logger LOG = LoggerFactory.getLogger(KittenCaptchaPanel.class);

	// The background grass area
	private static BufferedImage grass = load("images/grass.png");

	// The kittens and other animals
	private static final List<Animal> kittens = new ArrayList<Animal>();
	private static final List<Animal> nonKittens = new ArrayList<Animal>();

	// Random number generator
	private static Random random = new Random(-1);

	// Load animals
	static
	{
		kittens.add(new Animal("kitten_01", true));
		kittens.add(new Animal("kitten_02", true));
		kittens.add(new Animal("kitten_03", true));
		kittens.add(new Animal("kitten_04", true));
		nonKittens.add(new Animal("chick", false));
		nonKittens.add(new Animal("guinea_pig", false));
		nonKittens.add(new Animal("hamster", false));
		nonKittens.add(new Animal("puppy", false));
		nonKittens.add(new Animal("rabbit", false));
	}

	/**
	 * @param filename
	 *            The name of the file to load
	 * @return The image read form the file
	 */
	private static BufferedImage load(final String filename)
	{
		try
		{
			return ImageIO.read(new MemoryCacheImageInputStream(
				KittenCaptchaPanel.class.getResourceAsStream(filename)));
		}
		catch (IOException e)
		{
			LOG.error("Error loading image", e);
			return null;
		}
	}

	/**
	 * The various animals as placed animals
	 */
	private final PlacedAnimalList animals;

	/**
	 * Label that shows request status
	 */
	private final Label animalSelectionLabel;

	/**
	 * The image component
	 */
	private final Image image;

	/**
	 * The image resource referenced by the Image component
	 */
	private final CaptchaImageResource imageResource;

	/**
	 * Size of this kitten panel's image
	 */
	private final Dimension imageSize;

	/**
	 * @param id
	 *            Component id
	 * @param imageSize
	 *            Size of kitten captcha image
	 */
	public KittenCaptchaPanel(final String id, final Dimension imageSize)
	{
		super(id);

		// Save image size
		this.imageSize = imageSize;

		// Create animal list
		animals = new PlacedAnimalList();

		// Need to ajax refresh
		setOutputMarkupId(true);

		// Show how many animals have been selected
		animalSelectionLabel = new Label("animalSelectionLabel",
			new AbstractReadOnlyModel<String>()
			{
				private static final long serialVersionUID = 6792322972316712326L;

				@Override
				public String getObject()
				{
					return imageResource.selectString();
				}
			});
		animalSelectionLabel.setOutputMarkupId(true);
		add(animalSelectionLabel);

		// Image referencing captcha image resource
		image = new NonCachingImage("image", imageResource = new CaptchaImageResource(animals));
		image.add(new AjaxEventBehavior("click")
		{
			private static final long serialVersionUID = 7480352029955897654L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener ajaxCallListener = new AjaxCallListener() {
					@Override
					public CharSequence getBeforeSendHandler(Component component)
					{
						return "showLoadingIndicator();";
					}
				};
				attributes.getAjaxCallListeners().add(ajaxCallListener);
				List<CharSequence> dynamicExtraParameters = attributes.getDynamicExtraParameters();
				dynamicExtraParameters.add("return { x: getEventX(this, event), y: getEventY(this, event)}");
			}

			@Override
			protected void onEvent(final AjaxRequestTarget target)
			{
				// Get clicked cursor position
				final Request request = RequestCycle.get().getRequest();
				final int x = request.getRequestParameters().getParameterValue("x").toInt(0);
				final int y = request.getRequestParameters().getParameterValue("y").toInt(0);

				// Force refresh
				imageResource.clearData();

				// Find any animal at the clicked location
				final PlacedAnimal animal = animals.atLocation(new Point(x, y));

				// If the user clicked on an animal
				if (animal != null)
				{
					// Toggle the animal's highlighting
					animal.isHighlighted = !animal.isHighlighted;

					// Instead of reload entire image just change the src
					// attribute, this reduces the flicker
					final StringBuilder javascript = new StringBuilder();
					javascript.append("Wicket.$('")
						.append(image.getMarkupId())
						.append("').src = '");
					CharSequence url = image.urlFor(IResourceListener.INTERFACE,
						new PageParameters());
					javascript.append(url);
					javascript.append(url.toString().indexOf('?') > -1 ? "&amp;" : "?")
						.append("rand=")
						.append(Math.random());
					javascript.append("'");
					target.appendJavaScript(javascript.toString());
				}
				else
				{
					// The user didn't click on an animal, so hide the loading
					// indicator
					target.appendJavaScript(" hideLoadingIndicator();");
				}

				// Update the selection label
				target.add(animalSelectionLabel);
			}
		});
		add(image);
	}

	/**
	 * @return True if all (three) kittens have been selected
	 */
	public boolean allKittensSelected()
	{
		return imageResource.allKittensSelected();
	}

	/**
	 * Resets for another go-around
	 */
	public void reset()
	{
		imageResource.reset();
	}

	/**
	 * @param animals
	 *            List of animals
	 * @param newAnimal
	 *            New animal to place
	 * @return The placed animal
	 */
	private PlacedAnimal placeAnimal(final List<PlacedAnimal> animals, final Animal newAnimal)
	{
		// Try 100 times
		for (int iter = 0; iter < 100; iter++)
		{
			// Get the new animal's width and height
			final int width = newAnimal.image.getWidth();
			final int height = newAnimal.image.getHeight();

			// Pick a random position
			final int x = random(imageSize.width - width);
			final int y = random(imageSize.height - height);
			final Point point = new Point(x, y);

			// Determine if there is too much overlap with other animals
			final double tooClose = new Point(width, height).distance(new Point(0, 0)) / 2.0;
			boolean tooMuchOverlap = false;
			for (final PlacedAnimal animal : animals)
			{
				if (point.distance(animal.location) < tooClose)
				{
					tooMuchOverlap = true;
					break;
				}
			}

			// If there was not too much overlap
			if (!tooMuchOverlap)
			{
				// The animal is now placed at x, y
				return new PlacedAnimal(newAnimal, new Point(x, y));
			}
		}

		// Could not place animal
		return null;
	}

	/**
	 * @param max
	 *            Maximum size of random value
	 * @return A random number between 0 and max - 1
	 */
	private int random(final int max)
	{
		return Math.abs(random.nextInt(max));
	}

	/**
	 * @return A random kitten
	 */
	private Animal randomKitten()
	{
		return kittens.get(random(kittens.size()));
	}

	/**
	 * @return A random other animal
	 */
	private Animal randomNonKitten()
	{
		return nonKittens.get(random(nonKittens.size()));
	}

	/**
	 * Animal, whether kitten or non-kitten
	 */
	private static class Animal
	{
		/**
		 * The highlighted image
		 */
		private final BufferedImage highlightedImage;

		/**
		 * The normal image
		 */
		private final BufferedImage image;

		/**
		 * True if the animal is a kitten
		 */
		private final boolean isKitten;

		/**
		 * The visible region of the animal
		 */
		private final OpaqueRegion visibleRegion;

		/**
		 * @param filename
		 *            The filename
		 * @param isKitten
		 *            True if the animal is a kitten
		 */
		private Animal(final String filename, final boolean isKitten)
		{
			this.isKitten = isKitten;
			image = load("images/" + filename);
			highlightedImage = load("images/" + filename + "_highlight");
			visibleRegion = new OpaqueRegion(image);
		}

		/**
		 * @param filename
		 *            The file to load
		 * @return The image in the file
		 */
		private BufferedImage load(final String filename)
		{
			try
			{
				final BufferedImage loadedImage = ImageIO.read(new MemoryCacheImageInputStream(
					KittenCaptchaPanel.class.getResourceAsStream(filename + ".png")));
				final BufferedImage image = new BufferedImage(loadedImage.getWidth(),
					loadedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
				final Graphics2D graphics = image.createGraphics();
				graphics.drawImage(loadedImage, 0, 0, null);
				graphics.dispose();
				return image;
			}
			catch (IOException e)
			{
				LOG.error("Error loading image", e);
				return null;
			}
		}
	}

	/**
	 * Resource which renders the actual captcha image
	 */
	private static class CaptchaImageResource extends DynamicImageResource
	{
		private static final long serialVersionUID = -1560784998742404278L;

		/**
		 * The placed animals
		 */
		private final PlacedAnimalList animals;

		/**
		 * Image data array
		 */
		private transient SoftReference<byte[]> data = null;

		@Override
		protected void configureResponse(final ResourceResponse response,
			final Attributes attributes)
		{
			super.configureResponse(response, attributes);
			response.disableCaching();
		}

		/**
		 * @param animals
		 *            The positioned animals
		 */
		private CaptchaImageResource(final PlacedAnimalList animals)
		{
			this.animals = animals;
			setFormat("jpg");
		}

		/**
		 * @return Rendered image data
		 */
		@Override
		protected byte[] getImageData(final Attributes attributes)
		{
			// Handle caching
			setLastModifiedTime(Time.now());
			final WebResponse response = (WebResponse)RequestCycle.get().getResponse();
			response.setHeader("Cache-Control", "no-cache, must-revalidate, max-age=0, no-store");

			// If we don't have data
			if ((data == null) || (data.get() == null))
			{
				// Create the image and turn it into data
				final BufferedImage composedImage = animals.createImage();
				data = new SoftReference<byte[]>(toImageData(composedImage));
			}

			// Return image data
			return data.get();
		}

		/**
		 * Invalidates the image data
		 */
		protected void invalidate()
		{
			data = null;
		}

		/**
		 * @return True if all kittens have been selected
		 */
		private boolean allKittensSelected()
		{
			return animals.allKittensSelected();
		}

		/**
		 * Clears out image data
		 */
		private void clearData()
		{
			invalidate();
			setLastModifiedTime(Time.now());
		}

		/**
		 * Resets animals to default states
		 */
		private void reset()
		{
			animals.reset();
		}

		/**
		 * @return Selection state string for animals
		 */
		private String selectString()
		{
			return animals.selectString();
		}
	}

	/**
	 * An animal that has a location
	 */
	private static class PlacedAnimal implements Serializable
	{
		private static final long serialVersionUID = -6703909440564862486L;

		/**
		 * The animal
		 */
		private transient Animal animal;

		/**
		 * Index in kitten or nonKitten list
		 */
		private final int index;

		/**
		 * True if the animal is highlighted
		 */
		private boolean isHighlighted;

		/**
		 * True if this animal is a kitten
		 */
		private final boolean isKitten;

		/**
		 * The location of the animal
		 */
		private final Point location;

		/**
		 * Scaling values
		 */
		private final float[] scales = { 1f, 1f, 1f, 1f };

		/**
		 * @param animal
		 *            The animal
		 * @param location
		 *            Where to put it
		 */
		public PlacedAnimal(final Animal animal, final Point location)
		{
			this.animal = animal;
			this.location = location;
			isKitten = animal.isKitten;
			if (isKitten)
			{
				index = kittens.indexOf(animal);
			}
			else
			{
				index = nonKittens.indexOf(animal);
			}
			for (int i = 0; i < 3; i++)
			{
				scales[i] = random(0.9f, 1.0f);
			}
			scales[3] = random(0.7f, 1.0f);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return (isKitten ? "kitten at " : "other at ") + location.x + ", " + location.y;
		}

		/**
		 * @param point
		 *            The point
		 * @return True if this placed animal contains the given point
		 */
		private boolean contains(final Point point)
		{
			final Point relativePoint = new Point(point.x - location.x, point.y - location.y);
			return getAnimal().visibleRegion.contains(relativePoint);
		}

		/**
		 * @param graphics
		 *            The graphics to draw on
		 */
		private void draw(final Graphics2D graphics)
		{
			final float[] offsets = new float[4];
			final RescaleOp rop = new RescaleOp(scales, offsets, null);
			if (isHighlighted)
			{
				graphics.drawImage(getAnimal().highlightedImage, rop, location.x, location.y);
			}
			else
			{
				graphics.drawImage(getAnimal().image, rop, location.x, location.y);
			}
		}

		/**
		 * @return The animal that is placed
		 */
		private Animal getAnimal()
		{
			if (animal == null)
			{
				if (isKitten)
				{
					animal = kittens.get(index);
				}
				else
				{
					animal = nonKittens.get(index);
				}
			}
			return animal;
		}

		/**
		 * @param min
		 *            Minimum random value
		 * @param max
		 *            Maximum random value
		 * @return A random value in the given range
		 */
		private float random(final float min, final float max)
		{
			return min + Math.abs(random.nextFloat() * (max - min));
		}
	}

	/**
	 * Holds a list of placed animals
	 */
	private class PlacedAnimalList implements Serializable
	{
		private static final long serialVersionUID = 6335852594326213439L;

		/**
		 * List of placed animals
		 */
		private final List<PlacedAnimal> animals = new ArrayList<PlacedAnimal>();

		/**
		 * Arrange random animals and kittens
		 */
		private PlacedAnimalList()
		{
			// Place the three kittens
			animals.add(placeAnimal(animals, randomKitten()));
			animals.add(placeAnimal(animals, randomKitten()));
			animals.add(placeAnimal(animals, randomKitten()));

			// Try a few times
			for (int iter = 0; iter < 500; iter++)
			{
				// Place a non kitten
				final PlacedAnimal animal = placeAnimal(animals, randomNonKitten());

				// If we were able to place the animal
				if (animal != null)
				{
					// add it to the list
					animals.add(animal);
				}

				// 15 non-kittens is enough
				if (animals.size() > 15)
				{
					break;
				}
			}

			// Shuffle the animal order
			Collections.shuffle(animals);

			// Ensure kittens are visible enough
			List<PlacedAnimal> strayKittens = new ArrayList<PlacedAnimal>();
			for (final PlacedAnimal animal : animals)
			{
				// If it's a kitten
				if (animal.isKitten)
				{
					// Compute the area of the visible region in pixels
					final int kittenArea = animal.getAnimal().visibleRegion.areaInPixels();

					// If at least 4/5ths of the given kitten is not visible
					// (because it is obscured by other animal(s))
					if (visibleRegion(animal).areaInPixels() < kittenArea * 4 / 5)
					{
						// The user probably can't identify it, so add to the
						// stray kittens list
						strayKittens.add(animal);
					}
				}
			}

			// Remove any the stray kittens and then re-add them so they move to
			// the top of the z-order
			animals.removeAll(strayKittens);
			animals.addAll(strayKittens);
		}

		/**
		 * @return True if all kittens are selected
		 */
		private boolean allKittensSelected()
		{
			for (final PlacedAnimal animal : animals)
			{
				if (animal.isKitten != animal.isHighlighted)
				{
					return false;
				}
			}
			return true;
		}

		/**
		 * @param location
		 *            The cursor location that was clicked
		 * @return Any animal that might be at the given location or null if none found (the user
		 *         clicked on grass)
		 */
		private PlacedAnimal atLocation(final Point location)
		{
			// Reverse list for z-ordered hit-testing
			final List<PlacedAnimal> reversedAnimals = new ArrayList<PlacedAnimal>(animals);
			Collections.reverse(reversedAnimals);

			// Return any animal at the given location
			for (final PlacedAnimal animal : reversedAnimals)
			{
				if (animal.contains(location))
				{
					return animal;
				}
			}

			// No animal found
			return null;
		}

		/**
		 * @return The kitten captcha image
		 */
		private BufferedImage createImage()
		{
			// Create image of the right size
			final BufferedImage newImage = new BufferedImage(imageSize.width, imageSize.height,
				BufferedImage.TYPE_INT_RGB);

			// Draw the grass
			final Graphics2D graphics = newImage.createGraphics();
			graphics.drawImage(grass, 0, 0, null);

			// Draw each animal in order
			for (final PlacedAnimal animal : animals)
			{
				animal.draw(graphics);
			}

			// Clean up graphics resource
			graphics.dispose();

			// Return the rendered animals
			return newImage;
		}

		/**
		 * Undo highlight states of animals
		 */
		private void reset()
		{
			for (final PlacedAnimal animal : animals)
			{
				animal.isHighlighted = false;
			}
		}

		/**
		 * @return Selection string to show
		 */
		private String selectString()
		{
			int selected = 0;
			for (final PlacedAnimal animal : animals)
			{
				if (animal.isHighlighted)
				{
					selected++;
				}
			}
			if (selected == 0)
			{
				return getString("instructions");
			}
			else
			{
				return selected + " " + getString("animalsSelected");
			}
		}

		/**
		 * @param animal
		 *            The animal
		 * @return The visible region of the animal
		 */
		private OpaqueRegion visibleRegion(final PlacedAnimal animal)
		{
			// The index of the animal in the animal list
			int index = animals.indexOf(animal);

			// Check sanity
			if (index == -1)
			{
				// Invalid animal somehow
				throw new IllegalArgumentException("animal not in list");
			}
			else
			{
				// Get the animal's visible region
				OpaqueRegion visible = animal.getAnimal().visibleRegion;

				// Go through the animals above the given animal
				for (index++; index < animals.size(); index++)
				{

					// Remove the higher animal's visible region
					final PlacedAnimal remove = animals.get(index);
					visible = visible.subtract(remove.getAnimal().visibleRegion, new Point(
						remove.location.x - animal.location.x, remove.location.y -
							animal.location.y));
				}
				return visible;
			}
		}
	}
}
