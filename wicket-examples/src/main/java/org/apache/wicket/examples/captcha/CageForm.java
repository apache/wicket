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

import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;

import com.github.cage.Cage;
import com.github.cage.GCage;

/**
 * A demo form that shows how to use <a href="https://github.com/akiraly/cage">Cage</a>
 * library
 */
public class CageForm<T> extends AbstractCaptchaForm<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param id
	 *          The component id
	 */
	public CageForm(String id)
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
				randomText = Captcha.randomString(6, 8);
				Cage cage = new GCage();
				return cage.draw(randomText);
			}
		};
	}
}
