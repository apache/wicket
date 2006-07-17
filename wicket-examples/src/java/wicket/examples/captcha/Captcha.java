/*
 * $Id: HelloWorld.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision: 5394 $ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.rmi.server.UID;

import wicket.MarkupContainer;
import wicket.Session;
import wicket.examples.WicketExamplePage;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.AbstractValidator;
import wicket.markup.html.image.Image;
import wicket.markup.html.image.resource.DynamicImageResource;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.IModel;
import wicket.model.PropertyModel;

import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Example page for Captcha (completely automated public Turing test to tell
 * computers and humans apart) authentication.
 * 
 * @author Eelco Hillenius
 */
public class Captcha extends WicketExamplePage
{
	/**
	 * Form that has the captcha image and knows how to check the captcha
	 * response.
	 */
	private static final class CaptchaForm extends Form
	{
		/**
		 * Id of the challenge; generated fresh every time we issue a new
		 * challenge.
		 */
		private String challengeId = null;

		/** Variable for receiving the client's response. */
		private String challengeResponse;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent
		 * 
		 * @param id
		 *            component id
		 */
		public CaptchaForm(MarkupContainer parent, String id)
		{
			super(parent, id);

			// The dynamic image resource that generates the captcha image for
			// each request using a fresh (u)uid every time. The id is used by
			// the validator to check whether the input matches the expected
			// answer
			DynamicImageResource captchaImageResource = new DynamicImageResource()
			{
				@Override
				protected byte[] getImageData()
				{
					try
					{
						ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
						challengeId = new UID().toString();
						BufferedImage challenge = captchaService.getImageChallengeForID(
								challengeId, Session.get().getLocale());
						JPEGImageEncoder jpegEncoder = JPEGCodec
								.createJPEGEncoder(jpegOutputStream);
						jpegEncoder.encode(challenge);
						return jpegOutputStream.toByteArray();
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}
			};
			new Image(this, "captchaImage", captchaImageResource);

			// add the text field for receiving the client's answer
			IModel<String> responseModel = new PropertyModel<String>(this, "challengeResponse");
			TextField responseField = new TextField<String>(this, "response", responseModel)
			{
				@Override
				protected final void onComponentTag(final ComponentTag tag)
				{
					super.onComponentTag(tag);
					// clear the field after each render
					tag.put("value", "");
				}
			};

			// and add the validator that checks the response against the
			// challenge
			responseField.add(new AbstractValidator()
			{
				public void validate(FormComponent component)
				{
					if (!captchaService.validateResponseForID(challengeId, component.getInput())
							.booleanValue())
					{
						error(component);
					}
				}

				@Override
				protected String resourceKey(final FormComponent formComponent)
				{
					return "captcha.validation.failed";
				}
			});

			// add a feedback panel to trap any messages
			new FeedbackPanel(this, "feedback");
		}

		/**
		 * Gets the clients' response.
		 * 
		 * @return response
		 */
		public String getChallengeResponse()
		{
			return challengeResponse;
		}

		/**
		 * Sets client's response.
		 * 
		 * @param challengeResponse
		 *            response
		 */
		public void setChallengeResponse(String challengeResponse)
		{
			this.challengeResponse = challengeResponse;
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		protected void onSubmit()
		{
			// if we get here, it means the response got validated
			// set a message to display the answer was correct
			info(getLocalizer().getString("captcha.validation.succeeded", this));
		}
	}

	/**
	 * JCaptcha service. Has to be a singleton according to their documentation.
	 * For this example, just a static suffices.
	 */
	private static final ImageCaptchaService captchaService = new DefaultManageableImageCaptchaService();

	/**
	 * Construct.
	 */
	public Captcha()
	{
		new CaptchaForm(this, "form");
	}
}