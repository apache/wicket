/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.upload;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.protocol.http.WebRequest;

/**
 * Form for handling (file) uploads with multipart requests.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class UploadForm extends Form
{
	/**
	 * Construct.
	 * 
	 * @param name
	 *            component name
	 * @param validationErrorHandler
	 *            validation error handler
	 */
	public UploadForm(String name, IValidationFeedback validationErrorHandler)
	{
		super(name, validationErrorHandler);
	}

	/**
	 * @see wicket.markup.html.form.Form#onFormSubmitted()
	 */
	public void onFormSubmitted()
	{
		// Change the request to a multipart web request so parameters are
		// parsed out correctly
		RequestCycle.get().setRequest(
				new MultipartWebRequest(((WebRequest)getRequest()).getHttpServletRequest()));

		// Now do normal form submit validation processing
		super.onFormSubmitted();
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            the component tag
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("enctype", "multipart/form-data");
	}

	/**
	 * @see wicket.markup.html.form.Form#onSubmit()
	 */
	protected abstract void onSubmit();
}
