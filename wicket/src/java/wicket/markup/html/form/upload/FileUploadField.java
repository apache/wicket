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

import org.apache.commons.fileupload.FileItem;

import wicket.Request;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.FormComponent;

/**
 * Form component that corresponds to a &lt;input type=&quot;file&quot;&gt;.
 * When a FileInput component is nested in a
 * {@link wicket.markup.html.form.upload.UploadForm}, its model is updated with
 * the {@link org.apache.commons.fileupload.FileItem}for this component.
 * 
 * @author Eelco Hillenius
 */
public class FileUploadField extends FormComponent
{
	/** The model for the uploaded file */
	private FileUpload fileUpload;
	
	/**
	 * @see wicket.Component#Component(String)
	 */
	public FileUploadField(final String id)
	{
		super(id);
	}

	/**
	 * @return The uploaded file
	 */
	public FileUpload getFileUpload()
	{
		return fileUpload;
	}
	
	/**
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		// Must be attached to an input tag
		checkComponentTag(tag, "input");

		// Check for file type
		checkComponentTagAttribute(tag, "type", "file");

		// Default handling for component tag
		super.onComponentTag(tag);
	}

	/**
	 * FileInputs cannot be persisted; returns false.
	 * 
	 * @see wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	protected boolean supportsPersistence()
	{
		return false;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
		// Get request 
		final Request request = getRequest();
		
		// If we successfully installed a multipart request
		if (request instanceof MultipartWebRequest)
		{
			// Get the item for the path
			final FileItem item = ((MultipartWebRequest)request).getFile(getPath());
	
			// Only update the model when there is a file (larger than zero bytes)
			if (item != null && item.getSize() > 0)
			{
				this.fileUpload = new FileUpload(item);
			}
		}
	}
}
