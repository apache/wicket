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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.protocol.http.WebRequest;

/**
 * Base class for upload forms.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractUploadForm extends Form
{
	/** Log. */
	private static Log log = LogFactory.getLog(AbstractUploadForm.class);

	/**
	 * Construct.
	 * 
	 * @param name
	 *            component name
	 * @param validationErrorHandler
	 *            validation error handler
	 */
	public AbstractUploadForm(String name, IValidationFeedback validationErrorHandler)
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
	protected void onSubmit()
	{
		// The submit was valid and some form subclass implementation of
		// onSubmit() called super.onSubmit()
		final List files = ((MultipartWebRequest)getRequest()).getFiles();
		for (final Iterator iterator = files.iterator(); iterator.hasNext();)
		{
			onUpload((FileItem)iterator.next());
		}
	}

	/**
	 * Override this method to handle uploading a file
	 * 
	 * @param fileItem
	 *            The file item to deal with
	 */
	protected abstract void onUpload(FileItem fileItem);
}
