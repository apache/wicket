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
package org.apache.wicket.extensions.markup.html.captcha;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.time.Time;


/**
 * Generates a captcha image.
 * 
 * @author Joshua Perlow
 */
public final class CaptchaImageResource extends DynamicImageResource
{
	/**
	 * This class is used to encapsulate all the filters that a character will get when rendered.
	 * The changes are kept so that the size of the shapes can be properly recorded and reproduced
	 * later, since it dynamically generates the size of the captcha image. The reason I did it this
	 * way is because none of the JFC graphics classes are serializable, so they cannot be instance
	 * variables here. If anyone knows a better way to do this, please let me know.
	 */
	private static final class CharAttributes implements IClusterable
	{
		private static final long serialVersionUID = 1L;
		private final char c;
		private final String name;
		private final int rise;
		private final double rotation;
		private final double shearX;
		private final double shearY;

		CharAttributes(final char c, final String name, final double rotation, final int rise,
			final double shearX, final double shearY)
		{
			this.c = c;
			this.name = name;
			this.rotation = rotation;
			this.rise = rise;
			this.shearX = shearX;
			this.shearY = shearY;
		}

		char getChar()
		{
			return c;
		}

		String getName()
		{
			return name;
		}

		int getRise()
		{
			return rise;
		}

		double getRotation()
		{
			return rotation;
		}

		double getShearX()
		{
			return shearX;
		}

		double getShearY()
		{
			return shearY;
		}
	}

	private static final long serialVersionUID = 1L;

	private static int randomInt(final int min, final int max)
	{
		return (int)(Math.random() * (max - min) + min);
	}

	private static String randomString(final int min, final int max)
	{
		int num = randomInt(min, max);
		byte b[] = new byte[num];
		for (int i = 0; i < num; i++)
		{
			b[i] = (byte)randomInt('a', 'z');
		}
		return new String(b);
	}

	private final IModel<String> challengeId;

	private final List<String> fontNames = Arrays.asList("Helvetica", "Arial", "Courier");
	private final int fontSize;
	private final int fontStyle;

	/** Transient image data so that image only needs to be generated once per VM */
	private transient SoftReference<byte[]> imageData;

	private final int margin;

	/**
	 * Construct.
	 */
	public CaptchaImageResource()
	{
		this(randomString(6, 8));
	}

	/**
	 * Construct.
	 * 
	 * @param challengeId
	 *            The id of the challenge
	 */
	public CaptchaImageResource(final String challengeId)
	{
		this(new Model<String>(challengeId));
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param challengeId
	 *            The id of the challenge
	 */
	public CaptchaImageResource(final IModel<String> challengeId)
	{
		this(challengeId, 48, 30);
	}

	/**
	 * Construct.
	 * 
	 * @param challengeId
	 *            The id of the challenge
	 * @param fontSize
	 *            The font size
	 * @param margin
	 *            The image's margin
	 */
	public CaptchaImageResource(final IModel<String> challengeId, final int fontSize,
		final int margin)
	{
		this.challengeId = challengeId;
		fontStyle = 1;
		this.fontSize = fontSize;
		this.margin = margin;
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param challengeId
	 *            The id of the challenge
	 * @param fontSize
	 *            The font size
	 * @param margin
	 *            The image's margin
	 */
	public CaptchaImageResource(final String challengeId, final int fontSize, final int margin)
	{
		this(new Model<String>(challengeId), fontSize, margin);
	}

	/**
	 * Gets the id for the challenge.
	 * 
	 * @return The id for the challenge
	 */
	public final String getChallengeId()
	{
		return challengeId.getObject();
	}

	/**
	 * Gets the id for the challenge
	 * 
	 * @return The id for the challenge
	 */
	public final IModel<String> getChallengeIdModel()
	{
		return challengeId;
	}

	/**
	 * Causes the image to be redrawn the next time its requested.
	 */
	public final void invalidate()
	{
		imageData = null;
	}

	@Override
	protected final byte[] getImageData(final Attributes attributes)
	{
		// get image data is always called in sync block
		byte[] data = null;
		if (imageData != null)
		{
			data = imageData.get();
		}
		if (data == null)
		{
			data = render();
			imageData = new SoftReference<byte[]>(data);
			setLastModifiedTime(Time.now());
		}
		return data;
	}

	private Font getFont(final String fontName)
	{
		return new Font(fontName, fontStyle, fontSize);
	}

	/**
	 * Renders this image
	 * 
	 * @return The image data
	 */
	private final byte[] render()
	{
		int width = margin * 2;
		int height = margin * 2;
		char[] chars = challengeId.getObject().toCharArray();
		List<CharAttributes> charAttsList = new ArrayList<CharAttributes>();
		TextLayout text;
		AffineTransform textAt;
		Shape shape;

		for (char ch : chars)
		{
			String fontName = fontNames.get(randomInt(0, fontNames.size()));
			double rotation = Math.toRadians(randomInt(-35, 35));
			int rise = randomInt(margin / 2, margin);
			Random ran = new Random();
			double shearX = ran.nextDouble() * 0.2;
			double shearY = ran.nextDouble() * 0.2;
			CharAttributes cf = new CharAttributes(ch, fontName, rotation, rise, shearX, shearY);
			charAttsList.add(cf);
			text = new TextLayout(ch + "", getFont(fontName), new FontRenderContext(null, false,
				false));
			textAt = new AffineTransform();
			textAt.rotate(rotation);
			textAt.shear(shearX, shearY);
			shape = text.getOutline(textAt);
			width += (int)shape.getBounds2D().getWidth();
			if (height < (int)shape.getBounds2D().getHeight() + rise)
			{
				height = (int)shape.getBounds2D().getHeight() + rise;
			}
		}

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gfx = (Graphics2D)image.getGraphics();
		gfx.setBackground(Color.WHITE);
		int curWidth = margin;
		for (CharAttributes cf : charAttsList)
		{
			text = new TextLayout(cf.getChar() + "", getFont(cf.getName()),
				gfx.getFontRenderContext());
			textAt = new AffineTransform();
			textAt.translate(curWidth, height - cf.getRise());
			textAt.rotate(cf.getRotation());
			textAt.shear(cf.getShearX(), cf.getShearY());
			shape = text.getOutline(textAt);
			curWidth += shape.getBounds().getWidth();
			gfx.setXORMode(Color.BLACK);
			gfx.fill(shape);
		}

		// XOR circle
		int dx = randomInt(width, 2 * width);
		int dy = randomInt(width, 2 * height);
		int x = randomInt(0, width / 2);
		int y = randomInt(0, height / 2);

		gfx.setXORMode(Color.BLACK);
		gfx.setStroke(new BasicStroke(randomInt(fontSize / 8, fontSize / 2)));
		gfx.drawOval(x, y, dx, dy);

		WritableRaster rstr = image.getRaster();
		int[] vColor = new int[3];
		int[] oldColor = new int[3];
		Random vRandom = new Random(System.currentTimeMillis());

		// noise
		for (x = 0; x < width; x++)
		{
			for (y = 0; y < height; y++)
			{
				rstr.getPixel(x, y, oldColor);

				// hard noise
				vColor[0] = (int)(Math.floor(vRandom.nextFloat() * 1.03) * 255);
				// soft noise
				vColor[0] = vColor[0] ^ (170 + (int)(vRandom.nextFloat() * 80));
				// xor to image
				vColor[0] = vColor[0] ^ oldColor[0];
				vColor[1] = vColor[0];
				vColor[2] = vColor[0];

				rstr.setPixel(x, y, vColor);
			}
		}
		return toImageData(image);
	}
}
