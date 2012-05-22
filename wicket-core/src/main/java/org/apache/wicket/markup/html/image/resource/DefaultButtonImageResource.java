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
package org.apache.wicket.markup.html.image.resource;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Automatically generates a basic button image. The model for the component determines the label
 * displayed on the button.
 * 
 * @author Jonathan Locke
 */
public class DefaultButtonImageResource extends RenderedDynamicImageResource
{
	private static final long serialVersionUID = 1L;

	/** The default height for button images */
	private static int defaultHeight = 26;

	/** The default width for button images */
	private static int defaultWidth = 74;

	/** default color: orange. */
	private static final int DEFAULT_COLOR = new Color(0xE9, 0x60, 0x1A).getRGB();

	/** default background color: white. */
	private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE.getRGB();

	/** default text color: white. */
	private static final int DEFAULT_TEXT_COLOR = Color.WHITE.getRGB();

	/** default font: Helvetica bold 16. */
	private static final Map<TextAttribute, Object> DEFAULT_FONT = new HashMap<TextAttribute, Object>(
		new Font("Helvetica", Font.BOLD, 16).getAttributes());

	/**
	 * @param defaultHeight
	 *            The defaultHeight to set.
	 */
	public static void setDefaultHeight(int defaultHeight)
	{
		DefaultButtonImageResource.defaultHeight = defaultHeight;
	}

	/**
	 * @param defaultWidth
	 *            The defaultWidth to set.
	 */
	public static void setDefaultWidth(int defaultWidth)
	{
		DefaultButtonImageResource.defaultWidth = defaultWidth;
	}

	/** The height of the arc in the corner */
	private int arcHeight = 10;

	/** The width of the arc in the corner */
	private int arcWidth = 10;

	/** The background color behind the button */
	private int backgroundColorRgb = DEFAULT_BACKGROUND_COLOR;

	/** The color of the button itself */
	private int colorRgb = DEFAULT_COLOR;

	/** The font to use */
	private Map<TextAttribute, Object> fontAttributes = DEFAULT_FONT;

	/** The color of the text */
	private int textColorRgb = DEFAULT_TEXT_COLOR;

	/** The button label */
	private final String label;

	/**
	 * @param label
	 *            The label for this button image
	 * @param width
	 *            Width of image in pixels
	 * @param height
	 *            Height of image in pixels
	 */
	public DefaultButtonImageResource(int width, int height, final String label)
	{
		super(width, height, "png");
		this.label = label;
		setWidth(width == -1 ? defaultWidth : width);
		setHeight(height == -1 ? defaultHeight : height);
	}

	/**
	 * @param label
	 *            The label for this button image
	 */
	public DefaultButtonImageResource(final String label)
	{
		this(defaultWidth, defaultHeight, label);
	}

	/**
	 * @return Returns the arcHeight.
	 */
	public synchronized int getArcHeight()
	{
		return arcHeight;
	}

	/**
	 * @return Returns the arcWidth.
	 */
	public synchronized int getArcWidth()
	{
		return arcWidth;
	}

	/**
	 * @return Returns the backgroundColor.
	 */
	public synchronized Color getBackgroundColor()
	{
		return new Color(backgroundColorRgb);
	}

	/**
	 * @return Returns the color.
	 */
	public synchronized Color getColor()
	{
		return new Color(colorRgb);
	}

	/**
	 * @return Returns the font.
	 */
	public synchronized Font getFont()
	{
		return new Font(fontAttributes);
	}

	/**
	 * @return Returns the textColor.
	 */
	public synchronized Color getTextColor()
	{
		return new Color(textColorRgb);
	}

	/**
	 * @param arcHeight
	 *            The arcHeight to set.
	 */
	public synchronized void setArcHeight(int arcHeight)
	{
		this.arcHeight = arcHeight;
		invalidate();
	}

	/**
	 * @param arcWidth
	 *            The arcWidth to set.
	 */
	public synchronized void setArcWidth(int arcWidth)
	{
		this.arcWidth = arcWidth;
		invalidate();
	}

	/**
	 * @param backgroundColor
	 *            The backgroundColor to set.
	 */
	public synchronized void setBackgroundColor(Color backgroundColor)
	{
		backgroundColorRgb = backgroundColor.getRGB();
		invalidate();
	}

	/**
	 * @param color
	 *            The color to set.
	 */
	public synchronized void setColor(Color color)
	{
		colorRgb = color.getRGB();
		invalidate();
	}

	/**
	 * @param font
	 *            The font to set.
	 */
	public synchronized void setFont(Font font)
	{
		fontAttributes = new HashMap<TextAttribute, Object>(font.getAttributes());
		invalidate();
	}

	/**
	 * @param textColor
	 *            The textColor to set.
	 */
	public synchronized void setTextColor(Color textColor)
	{
		textColorRgb = textColor.getRGB();
		invalidate();
	}

	/**
	 * Renders button image.
	 * 
	 * @see RenderedDynamicImageResource#render(java.awt.Graphics2D, Attributes)
	 */
	@Override
	protected boolean render(Graphics2D graphics, Attributes attributes)
	{
		// Get width and height
		final int width = getWidth();
		final int height = getHeight();

		// Get size of text
		graphics.setFont(getFont());
		final FontMetrics fontMetrics = graphics.getFontMetrics();
		final int dxText = fontMetrics.stringWidth(label);
		final int dxMargin = 10;

		// Does text fit with a nice margin?
		if (dxText > width - dxMargin)
		{
			// Re-render as a larger button
			setWidth(dxText + dxMargin);
			return false;
		}
		else
		{
			// Turn on anti-aliasing
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

			// Draw background
			Color bgColor = getBackgroundColor();
			graphics.setColor(bgColor);
			graphics.fillRect(0, 0, width, height);

			// Draw round rectangle
			graphics.setColor(getColor());
			graphics.setBackground(bgColor);
			graphics.fillRoundRect(0, 0, width, height, arcWidth, arcHeight);

			// Draw text
			graphics.setColor(getTextColor());
			final int x = (width - dxText) / 2;
			final int y = (getHeight() - fontMetrics.getHeight()) / 2;
			graphics.drawString(label, x, y + fontMetrics.getAscent());
			return true;
		}
	}
}
