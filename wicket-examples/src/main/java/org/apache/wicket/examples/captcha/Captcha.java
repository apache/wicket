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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;


/**
 * Captcha example page.
 * 
 * @author Joshua Perlow
 */
public class Captcha extends WicketExamplePage
{
	private final class CaptchaForm<T> extends Form<T>
	{
		private static final long serialVersionUID = 1L;

		private final CaptchaImageResource captchaImageResource;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public CaptchaForm(String id)
		{
			super(id);

			captchaImageResource = new CaptchaImageResource(imagePass);
			final Image captchaImage = new Image("captchaImage", captchaImageResource);
			captchaImage.setOutputMarkupId(true);
			add(captchaImage);

			AjaxLink<Void> changeCaptchaLink = new AjaxLink<Void>("changeCaptcha")
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					captchaImageResource.invalidate();
					target.add(captchaImage);
				}
			};
			add(changeCaptchaLink);

			add(new RequiredTextField<String>("captchaText", new PropertyModel<String>(properties,
				"captchaText"), String.class)
			{
				@Override
				protected final void onComponentTag(final ComponentTag tag)
				{
					super.onComponentTag(tag);
					// clear the field after each render
					tag.put("value", "");
				}
			});
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public void onSubmit()
		{
			if (!imagePass.equals(getCaptchaText()))
			{
				error("Captcha text '" + getCaptchaText() + "' is wrong.\n" +
					"Correct text was: " + imagePass);
			}
			else
			{
				info("Success!");
			}

			// force redrawing
			captchaImageResource.invalidate();
		}
	}

	private static final long serialVersionUID = 1L;

	private static int randomInt(int min, int max)
	{
		return (int)(Math.random() * (max - min) + min);
	}

	private static String randomString(int min, int max)
	{
		int num = randomInt(min, max);
		byte b[] = new byte[num];
		for (int i = 0; i < num; i++)
			b[i] = (byte)randomInt('a', 'z');
		return new String(b);
	}

	/** Random captcha password to match against. */
	private final String imagePass = randomString(6, 8);

	private final ValueMap properties = new ValueMap();

	/**
	 * Constructor.
	 */
	public Captcha()
	{
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new CaptchaForm<>("captchaForm"));
	}

	private String getCaptchaText()
	{
		return properties.getString("captchaText");
	}
}
