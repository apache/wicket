/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;

import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Automatically generates a basic button image. The model for the component
 * determines the label displayed on the button.
 * 
 * @author Jonathan Locke
 */
public class ButtonImage extends DynamicImage
{
	/** The default height for button images */
	private static int defaultHeight = 32;

	/** The default width for button images */
	private static int defaultWidth = 80;
	
	/** serialVersionUID */
	private static final long serialVersionUID = 5934721258765771884L;
	
	/** The height of the arc in the corner */
	private int arcHeight = 10;
	
	/** The width of the arc in the corner */
	private int arcWidth = 10;

	/** The background color behind the button */
	private Color backgroundColor = Color.WHITE;

	/** The color of the button itself */
	private Color color = new Color(0xE9, 0x60, 0x1A);
	
	/** The font to use */
	private Font font = new Font("Helvetica", Font.BOLD, 16);

	/** The color of the text */
	private Color textColor = Color.WHITE;

	/**
	 * @param defaultHeight
	 *            The defaultHeight to set.
	 */
	public static void setDefaultHeight(int defaultHeight)
	{
		ButtonImage.defaultHeight = defaultHeight;
	}

	/**
	 * @param defaultWidth
	 *            The defaultWidth to set.
	 */
	public static void setDefaultWidth(int defaultWidth)
	{
		ButtonImage.defaultWidth = defaultWidth;
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public ButtonImage(final String name, final Serializable object)
	{
		super(name);
		setModel(new Model(object));
		setWidth(defaultWidth);
		setHeight(defaultHeight);
		setExtension("png");
	}

	/**
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public ButtonImage(final String name, final Serializable object, final String expression)
	{
		super(name);
		setModel(new PropertyModel(new Model(object), expression));
		setWidth(defaultWidth);
		setHeight(defaultHeight);
		setExtension("png");
	}
	
	/**
	 * @return Returns the arcHeight.
	 */
	public int getArcHeight()
	{
		return arcHeight;
	}

	/**
	 * @return Returns the arcWidth.
	 */
	public int getArcWidth()
	{
		return arcWidth;
	}

	/**
	 * @return Returns the backgroundColor.
	 */
	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	/**
	 * @return Returns the color.
	 */
	public Color getColor()
	{
		return color;
	}
	/**
	 * @return Returns the font.
	 */
	public Font getFont()
	{
		return font;
	}
	/**
	 * @return Returns the textColor.
	 */
	public Color getTextColor()
	{
		return textColor;
	}
	/**
	 * @param arcHeight The arcHeight to set.
	 */
	public void setArcHeight(int arcHeight)
	{
		this.arcHeight = arcHeight;
	}
	/**
	 * @param arcWidth The arcWidth to set.
	 */
	public void setArcWidth(int arcWidth)
	{
		this.arcWidth = arcWidth;
	}
	/**
	 * @param backgroundColor The backgroundColor to set.
	 */
	public void setBackgroundColor(Color backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}
	/**
	 * @param color The color to set.
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}
	/**
	 * @param font The font to set.
	 */
	public void setFont(Font font)
	{
		this.font = font;
	}
	/**
	 * @param textColor The textColor to set.
	 */
	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	/**
	 * Renders button image.
	 * 
	 * @see DynamicImage#render(Graphics2D)
	 */
	protected void render(final Graphics2D graphics)
	{
		// Get width and height
		final int width = getWidth();
		final int height = getHeight();
		
		// Turn on anti-aliasing
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw background
		graphics.setColor(backgroundColor);
		graphics.fillRect(0, 0, width, height);
		
		// Draw round rectangle
		graphics.setColor(color);
		graphics.setBackground(backgroundColor);
		graphics.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
		
		// Draw text
		graphics.setColor(textColor);
		graphics.setFont(font);
		final FontMetrics fontMetrics = graphics.getFontMetrics();
		final String text = getModelObjectAsString();
		final int x = (getWidth() - fontMetrics.stringWidth(text)) / 2;
		final int y = (getHeight() - fontMetrics.getHeight()) / 2;
		graphics.drawString(text, x, y + fontMetrics.getAscent());
	}
}
