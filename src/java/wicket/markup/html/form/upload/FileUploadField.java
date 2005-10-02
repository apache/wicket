/*
 * $Id$ $Revision:
 * 1.7 $ $Date$
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


import wicket.Component;
import wicket.Request;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.FormComponent;
import wicket.model.IModel;
import wicket.protocol.http.MultipartWebRequest;
import wicket.util.upload.FileItem;

/**
 * Form component that corresponds to a &lt;input type=&quot;file&quot;&gt;.
 * When a FileInput component is nested in a
 * {@link wicket.markup.html.form.Form}, that has multipart == true, its model
 * is updated with the {@link wicket.util.upload.FileItem}for this
 * component.
 * 
 * @author Eelco Hillenius
 */
public class FileUploadField extends FormComponent
{
	private static final long serialVersionUID = 1L;
	
	/** The model for the uploaded file */
	private FileUpload fileUpload;

	/** True if a model has been set explicitly */
	private boolean hasExplicitModel;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public FileUploadField(final String id)
	{
		super(id);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 */
	public FileUploadField(final String id, IModel model)
	{
		super(id, model);
		hasExplicitModel = true;
	}

	/**
	 * @return The uploaded file
	 */
	public FileUpload getFileUpload()
	{
		// Get request
		final Request request = getRequest();
		
		// If we successfully installed a multipart request
		if (request instanceof MultipartWebRequest)
		{
			// Get the item for the path
			final FileItem item = ((MultipartWebRequest)request).getFile(getInputName());

			// Only update the model when there is a file (larger than zero
			// bytes)
			if (item != null && item.getSize() > 0)
			{
				return new FileUpload(item);
			}
		}
		return null;
	}
	
	/**
	 * @see wicket.Component#setModel(wicket.model.IModel)
	 */
	public Component setModel(IModel model)
	{
		hasExplicitModel = true;
		return super.setModel(model);
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
		// Only update the model if one was passed in
		if (hasExplicitModel)
		{
			setModelObject(getFileUpload());
		}
	}
	
	/**
	 * @see wicket.markup.html.form.FormComponent#isMultiPart()
	 */
	public boolean isMultiPart()
	{
		return true;
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
}
