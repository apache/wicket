/*
 * $Id$
 * $Revision$
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

import org.apache.commons.fileupload.FileItem;

import wicket.Request;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.AbstractValidator;
import wicket.util.collections.MicroMap;

/**
 * Textfield that can be used with upload forms.
 *
 * @author Eelco Hillenius
 */
public class UploadTextField extends TextField
{
	/** the name of the file upload form field. */
	private final String fileFieldName;

	/** whether the upload is required. */
	private final boolean uploadRequired;

	/**
	 * Construct.
	 * @param name component name
	 * @param fileFieldName the name of the file upload form field
	 * @param model the upload model to be used for putting the upload and its name
	 * @param uploadRequired whether the upload is required
	 */
	public UploadTextField(String name, String fileFieldName,
			UploadModel model, boolean uploadRequired)
	{
		super(name, model);
		if (fileFieldName == null)
		{
			throw new NullPointerException("fileFieldName must be provided");
		}
		this.fileFieldName = fileFieldName;
		if(model == null)
		{
			throw new NullPointerException("model must be provided");
		}
		this.uploadRequired = uploadRequired;
		add(new UploadFieldValidator());
	}

	/**
	 * @see wicket.markup.html.form.TextField#updateModel()
	 */
	protected void updateModel()
	{
		UploadModel model = (UploadModel)getModel();
		model.setFile(getFile());
		model.setName(getRequestString());
	}

	/**
	 * Gets the upload that was sent for this component (with fileFieldName).
	 * @return the uploaded file or null if not found
	 */
	public final FileItem getFile()
	{
		Request request = getRequest();
		if (!(request instanceof MultipartWebRequest))
		{
			throw new IllegalStateException("this component may only " +
					"be used with upload (multipart) forms");
		}
		MultipartWebRequest multipartRequest = (MultipartWebRequest)request;
		return multipartRequest.getFile(fileFieldName);
	}

	/**
	 * Validates that a file was uploaded and, if so, that a name for that file
	 * using this component was provided.
	 */
	public final class UploadFieldValidator extends AbstractValidator
	{
		/**
		 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
		 */
		public void validate(FormComponent component)
		{
			final FileItem item = getFile(); // get upload
			final String name = component.getRequestString(); // get field value

			if (item == null || item.getSize() == 0) // any upload at all?
			{
				if(uploadRequired) // is providing an upload mandatory?
				{
					error(component, resourceKey(component) + ".file.required",
							new MicroMap("fileFieldName", fileFieldName));
				}
				else // no upload for this field took place; ignore
				{
					return;
				}
			}
			else // we have an upload
			{
				if (name == null || name.trim().equals("")) // is the name given
				{
					// though an upload was provided, we deny this request is no
					// name was given for the uploaded file using this component
					error(component, resourceKey(component) + ".name.required", name);
				}
			}
		}
	}
}