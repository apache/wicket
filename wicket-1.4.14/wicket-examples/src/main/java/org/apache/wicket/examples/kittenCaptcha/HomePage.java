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

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.captcha.kittens.KittenCaptchaPanel;

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
		add(new AjaxLink("checkKittens")
		{
			private static final long serialVersionUID = 642245961797905032L;

			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				if (!isSpamBot() && captcha.allKittensSelected())
				{
					target.appendJavascript("alert('you win! happy kittens!');");
				}
				else
				{
					errors++;
					if (isSpamBot())
					{
						target.appendJavascript("alert('spammer alert');");
					}
					else
					{
						target.appendJavascript("alert('please try again');");
					}
					target.addComponent(captcha);
				}
				captcha.reset();
			}
		});
	}

	/**
	 * 
	 * @return
	 */
	boolean isSpamBot()
	{
		return errors > 3;
	}
}
