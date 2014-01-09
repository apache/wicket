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
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.PropertyModel;

public abstract class AbstractCaptchaForm<T> extends GenericPanel<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The generated random text;
	 */
	protected String randomText;

	/**
	 * The text provided by the user
	 */
	private String captchaText;

	private final CaptchaImageResource captchaImageResource;

	/**
	 * Constructor.
	 *
	 * @param id
	 *          The component id
	 */
	public AbstractCaptchaForm(String id)
	{
		super(id);

		Form<T> form = new Form<T>("form")
		{
			@Override
			public void onSubmit()
			{
				if (!randomText.equals(captchaText))
				{
					error("Captcha text '" + captchaText + "' is wrong.\n" +
							"Correct text was: " + randomText);
				}
				else
				{
					info("Success!");
				}

				// force redrawing
				captchaImageResource.invalidate();
			}
		};
		add(form);

		final FeedbackPanel feedback = new FeedbackPanel("feedback",
				new ContainerFeedbackMessageFilter(AbstractCaptchaForm.this));
		form.add(feedback);

		captchaImageResource = createCaptchImageResource();
		final Image captchaImage = new Image("image", captchaImageResource);
		captchaImage.setOutputMarkupId(true);
		form.add(captchaImage);

		AjaxLink<Void> changeCaptchaLink = new AjaxLink<Void>("changeLink")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				captchaImageResource.invalidate();
				target.add(captchaImage);
			}
		};
		form.add(changeCaptchaLink);

		form.add(new RequiredTextField<String>("text",
				new PropertyModel<String>(AbstractCaptchaForm.this, "captchaText"), String.class)
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

	protected abstract CaptchaImageResource createCaptchImageResource();
}
