/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;

import wicket.IFeedback;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.protocol.http.WebRequest;
import wicket.util.lang.Bytes;

/**
 * Form for handling (file) uploads with multipart requests. Use this with
 * {@link wicket.markup.html.form.upload.FileUploadField}components. You can
 * attach mutliple FileInput fields for muliple file uploads.
 * <p>
 * This class depends on package
 * <a href="http://jakarta.apache.org/commons/fileupload/">Commons FileUpload</a>, version 1.0.
 * </p>
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class UploadForm extends Form
{
	/** Maximum size of an upload in bytes */
	Bytes maxSize = Bytes.MAX;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public UploadForm(String id)
	{
		super(id);
	}

	/**
	 * @see Form#Form(String, IFeedback)
	 */
	public UploadForm(final String id, IFeedback validationFeedback)
	{
		super(id, validationFeedback);
	}

	/**
	 * @see Form#Form(String, IModel, IFeedback)
	 */
	public UploadForm(final String id, IModel model, IFeedback feedback)
	{
		super(id, model, feedback);
	}

	/**
	 * Wraps the servlet request in a multipart request and sets it as the
	 * current request.
	 * 
	 * @see wicket.markup.html.form.Form#onFormSubmitted()
	 */
	public void onFormSubmitted()
	{
		// Change the request to a multipart web request so parameters are
		// parsed out correctly
		final HttpServletRequest request = ((WebRequest)getRequest()).getHttpServletRequest();
		try
		{
			final MultipartWebRequest multipartWebRequest = new MultipartWebRequest(this, request);
			getRequestCycle().setRequest(multipartWebRequest);

			// Now do normal form submit validation processing
			super.onFormSubmitted();
		}
		catch (FileUploadException e)
		{
			// Create model with exception and maximum size values
			final HashMap model = new HashMap();
			model.put("exception", e);
			model.put("maxSize", maxSize);

			if (e instanceof SizeLimitExceededException)
			{
				// Resource key should be <form-id>.uploadTooLarge to override default message
				final String defaultValue = "Upload must be less than " + maxSize;
				error(getString(getId() + ".uploadTooLarge", Model.valueOf(model), defaultValue));
			}
			else
			{
				// Resource key should be <form-id>.uploadFailed to override default message
				final String defaultValue = "Upload failed: " + e.getLocalizedMessage();
				error(getString(getId() + ".uploadFailed", Model.valueOf(model), defaultValue));
			}
		}
	}

	/**
	 * @param maxSize
	 *            The maxSize to set.
	 */
	public void setMaxSize(final Bytes maxSize)
	{
		this.maxSize = maxSize;
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("enctype", "multipart/form-data");
	}

	/**
	 * @see Form#onSubmit()
	 */
	protected abstract void onSubmit();
}
