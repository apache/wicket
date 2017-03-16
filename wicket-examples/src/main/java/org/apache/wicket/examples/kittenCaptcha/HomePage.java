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
package org.apache.wicket.examples.kittenCaptcha;

import java.awt.Dimension;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.captcha.kittens.KittenCaptchaPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Kitten captcha example
 */
public class HomePage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private final KittenCaptchaPanel captcha;
	private int errors;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage(final PageParameters parameters)
	{
		add(captcha = new KittenCaptchaPanel("captcha", new Dimension(400, 200)));

		// In a real application, you'd check the kittens in a form
		add(new AjaxLink<Void>("checkKittens")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if (!isSpamBot() && captcha.allKittensSelected())
				{
					target.appendJavaScript("alert('you win! happy kittens!');");
				}
				else
				{
					errors++;
					if (isSpamBot())
					{
						target.appendJavaScript("alert('spammer alert');");
					}
					else
					{
						target.appendJavaScript("alert('please try again');");
					}
					target.add(captcha);
				}
				captcha.reset();
			}
		});
	}

	/**
	 * @return {@code true} is there are at least 3 errors
	 */
	boolean isSpamBot()
	{
		return errors > 3;
	}
}
