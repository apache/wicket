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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.string.StringList;

/**
 * Processes a buffered image with alpha transparency by scan-lines, creating a simple rectangle
 * list enclosing all opaque pixels. The list is used by {@link #contains(Point)} to do hit testing.
 * An effective z-ordering of hit test regions is enabled by {@link #subtract(OpaqueRegion, Point)}.
 * 
 * @author Jonathan Locke
 */
class OpaqueRegion
{
	/**
	 * The list of rectangles in this region
	 */
	private final List<Rectangle> rectangles;

	/**
	 * @param image
	 *            The image to process
	 * @throws IllegalArgumentException
	 *             Thrown if image is not TYPE_INT_ARGB
	 */
	OpaqueRegion(final BufferedImage image)
	{
		// Check image type
		if (image.getType() != BufferedImage.TYPE_INT_ARGB)
		{
			throw new IllegalArgumentException("image must be TYPE_INT_ARGB");
		}

		// Initialize rectangle list
		rectangles = new ArrayList<>();

		// Get color model for image
		final ColorModel colorModel = image.getColorModel();

		// Process scan-lines
		final int dx = image.getWidth();
		final int dy = image.getHeight();
		for (int y = 0; y < dy; y++)
		{
			// No start line yet
			int startx = -1;

			// Process pixels in scan line
			for (int x = 0; x < dx; x++)
			{
				// Get pixel
				final int pixel = image.getRGB(x, y);

				// If the pixel is opaque
				if (colorModel.getAlpha(pixel) > 0)
				{
					// If no opaque rectangle has been started
					if (startx == -1)
					{
						// start one at the current pixel
						startx = x;
					}
				}
				else
				{
					// Pixel is transparent. If we started a rectangle
					if (startx != -1)
					{
						// close the rectangle and add it to the list
						rectangles.add(new Rectangle(startx, y, x - startx - 1, 1));

						// The next rectangle is not started yet
						startx = -1;
					}
				}
			}

			// If there is a rectangle open still
			if (startx != -1)
			{
				// close the rectangle and add it to the list
				rectangles.add(new Rectangle(startx, y, dx - startx - 1, 1));
			}
		}
	}

	/**
	 * @param rectangles
	 *            List of rectangles in opaque area
	 */
	private OpaqueRegion(final List<Rectangle> rectangles)
	{
		this.rectangles = rectangles;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return StringList.valueOf(rectangles).join();
	}

	/**
	 * @return The total area in pixels of this non-rectangular opaque region
	 */
	int areaInPixels()
	{
		int area = 0;
		for (final Rectangle rectangle : rectangles)
		{
			area += rectangle.width * rectangle.height;
		}
		return area;
	}

	/**
	 * @param point
	 *            The point to hit test
	 * @return True if this opaque region contains the point
	 */
	boolean contains(final Point point)
	{
		for (final Rectangle rectangle : rectangles)
		{
			if (rectangle.contains(point.x, point.y))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return The height of this opaque region in pixels
	 */
	int height()
	{
		int height = 0;
		for (final Rectangle rectangle : rectangles)
		{
			int y2 = rectangle.y + rectangle.height;
			if (y2 > height)
			{
				height = y2;
			}
		}
		return height;
	}

	/**
	 * @param removeRegion
	 *            The region to subtract from this one
	 * @param offset
	 *            An offset to apply to the remove region
	 * @return This region with the given region removed
	 */
	OpaqueRegion subtract(final OpaqueRegion removeRegion, final Point offset)
	{
		// Create new rectangle list
		final List<Rectangle> newRectangles = new ArrayList<>();

		// For each rectangle
		for (final Rectangle rectangle : rectangles)
		{
			// Get y
			final int y = rectangle.y;

			// Work list starts with the current rectangle in this opaque region
			final List<Rectangle> workList = new ArrayList<>();
			workList.add(new Rectangle(rectangle));

			// For each rectangle in the region to remove
			for (final Rectangle remove : removeRegion.rectangles)
			{
				// Offset the rectangle
				final Rectangle offsetRemove = new Rectangle(remove);
				offsetRemove.translate(offset.x, offset.y);

				// If we've gone past a possible match
				if (offsetRemove.y > y)
				{
					// quit
					break;
				}

				// If we're processing a rectangle on the right scan-line
				if (offsetRemove.y == y)
				{
					// Get rectangle x1 and x2
					int rx1 = offsetRemove.x;
					int rx2 = offsetRemove.x + offsetRemove.width;

					// Go through work list to remove the given rectangle
					for (int i = 0; i < workList.size(); i++)
					{
						// Get rectangle from work list
						final Rectangle work = workList.get(i);

						// Get work x1, x2
						int x1 = work.x;
						int x2 = work.x + work.width;

						// Compare left and right sides
						if ((rx1 <= x1) && (rx2 >= x2))
						{
							// Whole rectangle is obscured
							workList.remove(i);
							i -= 1;
						}
						else
						{
							// Check which sides are in
							boolean leftIn = (rx1 >= x1) && (rx1 < x2);
							boolean rightIn = (rx2 > x1) && (rx2 <= x2);

							if (leftIn)
							{
								if (rightIn)
								{
									// Split in two
									if (rx1 - x1 > 0)
									{
										workList.set(i, new Rectangle(x1, y, rx1 - x1, 1));
									}

									if (x2 - rx2 > 0)
									{
										workList.add(i + 1, new Rectangle(rx2, y, x2 - rx2, 1));
									}

								}
								else
								{
									// Chop off right side
									if (rx1 - x1 > 0)
									{
										workList.set(i, new Rectangle(x1, y, rx1 - x1, 1));
									}
								}
							}
							else if (rightIn)
							{
								// Chop off left side
								if (x2 - rx2 > 0)
								{
									workList.set(i, new Rectangle(rx2, y, x2 - rx2, 1));
								}
							}
						}
					}
				}
			}

			// Add the rectangle(s) to the new list
			newRectangles.addAll(workList);
		}

		// Return new opaque region
		return new OpaqueRegion(newRectangles);
	}

	/**
	 * @return A visual representation of this region for debugging purposes
	 */
	BufferedImage toDebugImage()
	{
		// Create a new image the same size as this region
		final int dx = width();
		final int dy = height();
		final BufferedImage image = new BufferedImage(dx, dy, BufferedImage.TYPE_INT_ARGB);

		// Black out all pixels
		for (int y = 0; y < dy; y++)
		{
			for (int x = 0; x < dx; x++)
			{
				image.setRGB(x, y, 0);
			}
		}

		// Make pixels purple
		for (final Rectangle rectangle : rectangles)
		{
			for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++)
			{
				image.setRGB(x, rectangle.y, 0xff00ff00);
			}
		}

		// Return debug image
		return image;
	}

	/**
	 * @return Width of this opaque region in pixels
	 */
	int width()
	{
		int width = 0;
		for (final Rectangle rectangle : rectangles)
		{
			int x2 = rectangle.x + rectangle.width;
			if (x2 > width)
			{
				width = x2;
			}
		}
		return width;
	}
}
