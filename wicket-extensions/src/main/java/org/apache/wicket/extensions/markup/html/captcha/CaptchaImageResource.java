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
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
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
public class CaptchaImageResource extends DynamicImageResource
{
	/**
	 * This class is used to encapsulate all the filters that a character will get when rendered.
	 * The changes are kept so that the size of the shapes can be properly recorded and reproduced
	 * later, since it dynamically generates the size of the captcha image. The reason I did it this
	 * way is because none of the JFC graphics classes are serializable, so they cannot be instance
	 * variables here.
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

	private static int randomInt(final Random rng, final int min, final int max)
	{
		return (int) (rng.nextDouble() * (max - min) + min);
	}

	private static String randomString(final Random rng, final int min, final int max)
	{
		int num = randomInt(rng, min, max);
		byte b[] = new byte[num];
		for (int i = 0; i < num; i++)
		{
			b[i] = (byte) randomInt(rng, 'a', 'z');
		}
		return new String(b);
	}

	private static final RandomNumberGeneratorFactory RNG_FACTORY = new RandomNumberGeneratorFactory();

	private final IModel<String> challengeId;

	private final List<String> fontNames = Arrays.asList("Helvetica", "Arial", "Courier");
	private final int fontSize;
	private final int fontStyle;

	/**
	 * Transient image data so that image only needs to be re-generated after de-serialization
	 */
	private transient SoftReference<byte[]> imageData;

	private final int margin;
	private final Random rng;

	/**
	 * Construct.
	 */
	public CaptchaImageResource()
	{
		this(randomString(RNG_FACTORY.newRandomNumberGenerator(), 10, 14));
	}

	/**
	 * Construct.
	 *
	 * @param challengeId
	 *          The id of the challenge
	 */
	public CaptchaImageResource(final String challengeId)
	{
		this(Model.of(challengeId));
	}

	/**
	 * Construct.
	 *
	 * @param challengeId
	 *          The id of the challenge
	 */
	public CaptchaImageResource(final IModel<String> challengeId)
	{
		this(challengeId, 48, 30);
	}

	/**
	 * Construct.
	 *
	 * @param challengeId
	 *          The id of the challenge
	 * @param fontSize
	 *          The font size
	 * @param margin
	 *          The image's margin
	 */
	public CaptchaImageResource(final IModel<String> challengeId, final int fontSize,
	                            final int margin)
	{
		this.challengeId = challengeId;
		this.fontStyle = 1;
		this.fontSize = fontSize;
		this.margin = margin;
		this.rng = newRandomNumberGenerator();
	}

	/**
	 * Construct.
	 *
	 * @param challengeId
	 *          The id of the challenge
	 * @param fontSize
	 *          The font size
	 * @param margin
	 *          The image's margin
	 */
	public CaptchaImageResource(final String challengeId, final int fontSize, final int margin)
	{
		this(Model.of(challengeId), fontSize, margin);
	}

	protected Random newRandomNumberGenerator()
	{
		return RNG_FACTORY.newRandomNumberGenerator();
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
			imageData = new SoftReference<>(data);
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
	protected byte[] render()
	{
		int width = margin * 2;
		int height = margin * 2;
		char[] chars = challengeId.getObject().toCharArray();
		List<CharAttributes> charAttsList = new ArrayList<>();
		TextLayout text;
		AffineTransform textAt;
		Shape shape;

		for (char ch : chars)
		{
			String fontName = fontNames.get(randomInt(rng, 0, fontNames.size()));
			double rotation = Math.toRadians(randomInt(rng, -35, 35));
			int rise = randomInt(rng, margin / 2, margin);

			double shearX = rng.nextDouble() * 0.2;
			double shearY = rng.nextDouble() * 0.2;
			CharAttributes cf = new CharAttributes(ch, fontName, rotation, rise, shearX, shearY);
			charAttsList.add(cf);
			text = new TextLayout(ch + "", getFont(fontName), new FontRenderContext(null, false,
				false));
			textAt = new AffineTransform();
			textAt.rotate(rotation);
			textAt.shear(shearX, shearY);
			shape = text.getOutline(textAt);
			width += (int) shape.getBounds2D().getWidth();
			if (height < (int) shape.getBounds2D().getHeight() + rise)
			{
				height = (int) shape.getBounds2D().getHeight() + rise;
			}
		}

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gfx = (Graphics2D) image.getGraphics();
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
		int dx = randomInt(rng, width, 2 * width);
		int dy = randomInt(rng, width, 2 * height);
		int x = randomInt(rng, 0, width / 2);
		int y = randomInt(rng, 0, height / 2);

		gfx.setXORMode(Color.BLACK);
		gfx.setStroke(new BasicStroke(randomInt(rng, fontSize / 8, fontSize / 2)));
		gfx.drawOval(x, y, dx, dy);

		WritableRaster rstr = image.getRaster();
		int[] vColor = new int[3];
		int[] oldColor = new int[3];

		// noise
		for (x = 0; x < width; x++)
		{
			for (y = 0; y < height; y++)
			{
				rstr.getPixel(x, y, oldColor);

				// hard noise
				vColor[0] = (int) (Math.floor(rng.nextFloat() * 1.03) * 255);
				// soft noise
				vColor[0] = vColor[0] ^ (170 + (int) (rng.nextFloat() * 80));
				// xor to image
				vColor[0] = vColor[0] ^ oldColor[0];
				vColor[1] = vColor[0];
				vColor[2] = vColor[0];

				rstr.setPixel(x, y, vColor);
			}
		}
		return toImageData(image);
	}

	/**
	 * The {@code RandomNumberGeneratorFactory} uses {@link java.security.SecureRandom} as RNG and {@code NativePRNG}
	 * on unix and {@code Windows-PRNG} on windows if it exists. Else it will fallback to {@code SHA1PRNG}.
	 * <p/>
	 * Please keep in mind that {@link java.security.SecureRandom} uses {@code /dev/random} as default on unix systems
	 * which is a blocking call. It is possible to change this by adding {@code -Djava.security.egd=file:/dev/urandom}
	 * to your application server startup script.
	 */
	private static final class RandomNumberGeneratorFactory
	{
		private final Provider.Service service;

		RandomNumberGeneratorFactory()
		{
			this.service = detectBestFittingService();
		}

		/**
		 * Checks all existing security providers and returns the best fitting service.
		 *
		 * This method is different to {@link java.security.SecureRandom#getPrngAlgorithm()} which uses the first PRNG
		 * algorithm of the first provider that has registered a SecureRandom implementation.
		 * {@code detectBestFittingService()} instead uses a native PRNG if available, then
		 * {@code SHA1PRNG} else {@code null} which triggers {@link java.security.SecureRandom#getPrngAlgorithm()}
		 * when calling {@code new SecureRandom()}.
		 *
		 * @return a native pseudo random number generator or sha1 as fallback.
		 */
		private Provider.Service detectBestFittingService()
		{
			Provider.Service _sha1Service = null;

			for (Provider provider : Security.getProviders())
			{
				for (Provider.Service service : provider.getServices())
				{
					if ("SecureRandom".equals(service.getType()))
					{
						String algorithm = service.getAlgorithm();
						if ("NativePRNG".equals(algorithm))
						{
							return service;
						}
						else if ("Windows-PRNG".equals(algorithm))
						{
							return service;
						}
						else if (_sha1Service == null && "SHA1PRNG".equals(algorithm))
						{
							_sha1Service = service;
						}
					}
				}
			}

			return _sha1Service;
		}

		/**
		 * @return new secure random number generator instance using best fitting service
		 */
		Random newRandomNumberGenerator()
		{
			if (service != null)
			{
				try
				{
					return SecureRandom.getInstance(service.getAlgorithm(), service.getProvider());
				}
				catch (NoSuchAlgorithmException nsax)
				{
					// this shouldn't happen, because 'detectBestFittingService' has checked for existing provider and
					// algorithms.
				}
			}

			return new SecureRandom();
		}
	}
}
