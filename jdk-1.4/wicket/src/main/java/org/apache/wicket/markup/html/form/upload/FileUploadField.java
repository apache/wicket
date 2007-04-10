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
package org.apache.wicket.markup.html.form.upload;


import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.util.upload.FileItem;

/**
 * Form component that corresponds to a &lt;input type=&quot;file&quot;&gt;.
 * When a FileInput component is nested in a
 * {@link org.apache.wicket.markup.html.form.Form}, that has multipart == true, its model
 * is updated with the {@link org.apache.wicket.util.upload.FileItem}for this component.
 * 
 * @author Eelco Hillenius
 */
public class FileUploadField extends FormComponent
{
	private static final long serialVersionUID = 1L;

	/** True if a model has been set explicitly */
	private boolean hasExplicitModel;

	private transient FileUpload fileUpload;

	/**
	 * @see org.apache.wicket.Component#Component(String)
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
		if (request instanceof IMultipartWebRequest)
		{
			// Get the item for the path
			final FileItem item = ((IMultipartWebRequest)request).getFile(getInputName());

			// Only update the model when there is a file (larger than zero
			// bytes)
			if (item != null && item.getSize() > 0)
			{
				if (fileUpload == null)
				{
					fileUpload = new FileUpload(item);
				}

				return fileUpload;
			}
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.Component#setModel(org.apache.wicket.model.IModel)
	 */
	public Component setModel(IModel model)
	{
		hasExplicitModel = true;
		return super.setModel(model);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
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
	 * @see org.apache.wicket.markup.html.form.FormComponent#getInputAsArray()
	 */
	public String[] getInputAsArray()
	{
		FileUpload fu = getFileUpload();
		if (fu != null)
			return new String[] { fu.getClientFileName() };
		return null;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#isMultiPart()
	 */
	public boolean isMultiPart()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
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
	 * @see org.apache.wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	protected boolean supportsPersistence()
	{
		return false;
	}

	/**
	 * Clean up at the end of the request. This means closing all inputstreams
	 * which might have been opened from the fileUpload.
	 * 
	 * @see org.apache.wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		if (fileUpload != null)
		{
			fileUpload.closeStreams();
			fileUpload = null;
		}
		super.onDetach();
	}
}
