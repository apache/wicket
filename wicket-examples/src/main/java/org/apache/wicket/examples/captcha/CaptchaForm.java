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

/**
 * A demo form that uses Wicket's default captcha image resource
 */
public class CaptchaForm<T> extends AbstractCaptchaForm<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 *
	 * @param id
	 *          The component id
	 */
	public CaptchaForm(String id)
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
				getChallengeIdModel().setObject(randomText);
				return super.render();
			}
		};
	}
}
