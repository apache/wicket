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
package org.apache.wicket.examples.captcha;

import java.awt.image.BufferedImage;
import java.util.Properties;

import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

/**
 * A demo form that shows how to use <a href="https://github.com/axet/kaptcha">Kaptcha</a>
 * library
 */
public class KaptchaForm<T> extends AbstractCaptchaForm<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param id
	 *          The component id
	 */
	public KaptchaForm(String id)
	{
		super(id);
	}

	@Override
	protected CaptchaImageResource createCaptchImageResource()
	{
		return new CaptchaImageResource()
		{
			@Override
			protected byte[] render()
			{
				DefaultKaptcha kaptcha = new DefaultKaptcha();
				Properties properties = new Properties();
				kaptcha.setConfig(new Config(properties));
				randomText = Captcha.randomString(6, 8);
				BufferedImage image = kaptcha.createImage(randomText);
				return toImageData(image);
			}
		};
	}
}
