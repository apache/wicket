/*
 * $Id: HelloWorldApplication.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) jdonnerstag $ $Revision: 5394 $ $Date: 2006-04-16 13:36:52 +0000 (Sun,
 * 16 Apr 2006) $
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
package wicket.examples.captcha;

import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.captcha.CaptchaImageResource;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.image.Image;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.PropertyModel;
import wicket.util.value.ValueMap;

/**
 * Captcha example page.
 * 
 * @author Joshua Perlow
 */
public class Captcha extends WicketExamplePage
{
	private final class CaptchaForm extends Form
	{
		private static final long serialVersionUID = 1L;

		private final CaptchaImageResource captchaImageResource;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 */
		public CaptchaForm(MarkupContainer parent, String id)
		{
			super(parent, id);

			captchaImageResource = new CaptchaImageResource(imagePass);
			new Image(this, "captchaImage", captchaImageResource);
			new RequiredTextField<String>(this, "password", new PropertyModel<String>(properties,
					"password"))
			{
				protected final void onComponentTag(final ComponentTag tag)
				{
					super.onComponentTag(tag);
					// clear the field after each render
					tag.put("value", "");
				}
			};
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit()
		{
			if (!imagePass.equals(getPassword()))
			{
				error("Captcha password '" + getPassword() + "' is wrong.\n"
						+ "Correct password was: " + imagePass);
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
	private String imagePass = randomString(6, 8);

	private final ValueMap properties = new ValueMap();

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public Captcha(final PageParameters parameters)
	{
		final FeedbackPanel feedback = new FeedbackPanel(this, "feedback");
		new CaptchaForm(this, "captchaForm");
	}

	private String getPassword()
	{
		return properties.getString("password");
	}
}