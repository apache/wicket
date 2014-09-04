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


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.upload.FileItem;

/**
 * Form component that corresponds to a &lt;input type=&quot;file&quot;&gt;. When a FileInput
 * component is nested in a {@link org.apache.wicket.markup.html.form.Form}, that has multipart ==
 * true, its model is updated with the {@link org.apache.wicket.markup.html.form.upload.FileUpload}
 * for this component.
 * <p>
 * <strong>NOTE</strong>The model of this component is reset with {@code null} at the end of the
 * request because {@link FileUpload} instances do not survive across requests since the input
 * streams they point to will be closed. Because of this, the {@link FileUpload} instance should be
 * processed within the same request as the form containing it was submitted.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class FileUploadField extends FormComponent<List<FileUpload>>
{
	private static final long serialVersionUID = 1L;

	private transient List<FileUpload> fileUploads;

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
	 *            the model holding the uploaded {@link FileUpload}s
	 */
	public FileUploadField(final String id, IModel<List<FileUpload>> model)
	{
		super(id, model);
	}

	/**
	 * @return the first uploaded file if HTML5 &lt;input type="file" <strong>multiple</strong>
	 *         /&gt; is used and the browser supports <em>multiple</em>, otherwise returns the
	 *         single uploaded file.
	 * @see #getFileUploads()
	 */
	public FileUpload getFileUpload()
	{
		List<FileUpload> fileUploads = getFileUploads();

		return fileUploads.isEmpty() ? null : fileUploads.get(0) ;
	}

	/**
	 * @return a list of all uploaded files. The list is empty if no files were selected. It will return more than one files if:
	 *         <ul>
	 *         <li>HTML5 &lt;input type="file" <strong>multiple</strong> /&gt; is used</li>
	 *         <li>the browser supports <em>multiple</em> attribute</li>
	 *         <li>the user has selected more than one files from the <em>Select file</em> dialog</li>
	 *         </ul>
	 */
	public List<FileUpload> getFileUploads()
	{
		if (fileUploads != null)
		{
			return fileUploads;
		}

		fileUploads = new ArrayList<>();

		// Get request
		final Request request = getRequest();

		// If we successfully installed a multipart request
		if (request instanceof IMultipartWebRequest)
		{
			// Get the item for the path
			final List<FileItem> fileItems = ((IMultipartWebRequest)request).getFile(getInputName());

			if (fileItems != null)
			{
				for (FileItem item : fileItems)
				{
					// Only update the model when there is a file (larger than zero
					// bytes)
					if (item != null && item.getSize() > 0)
					{
						fileUploads.add(new FileUpload(item));
					}
				}
			}
		}
		
		return fileUploads;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
	 */
	@Override
	public void updateModel()
	{
		if (getModel() != null)
		{
			super.updateModel();
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#getInputAsArray()
	 */
	@Override
	public String[] getInputAsArray()
	{
		List<FileUpload> fileUploads = getFileUploads();
		if (fileUploads.isEmpty() == false)
		{
			List<String> clientFileNames = new ArrayList<>();
			for (FileUpload fu : fileUploads)
			{
				clientFileNames.add(fu.getClientFileName());
			}
			return clientFileNames.toArray(new String[clientFileNames.size()]);
		}
		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(java.lang.String[])
	 */
	@Override
	protected List<FileUpload> convertValue(String[] value) throws ConversionException
	{
		final String[] filenames = getInputAsArray();
		if (filenames == null)
		{
			return null;
		}
		return getFileUploads();
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#isMultiPart()
	 */
	@Override
	public boolean isMultiPart()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
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
	 * Clean up at the end of the request. This means closing all inputstreams which might have been
	 * opened from the fileUpload.
	 * 
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		if ((fileUploads != null) && forceCloseStreamsOnDetach())
		{
			for (FileUpload fu : fileUploads)
			{
				fu.closeStreams();
			}
			fileUploads = null;

			if (getModel() != null)
			{
				getModel().setObject(null);
			}
		}
		super.onDetach();
	}

	/**
	 * The FileUploadField will close any input streams you have opened in its FileUpload by
	 * default. If you wish to manage the stream yourself (e.g. you want to use it in another
	 * thread) then you can override this method to prevent this behavior.
	 * 
	 * @return <code>true</code> if stream should be closed at the end of request
	 */
	protected boolean forceCloseStreamsOnDetach()
	{
		return true;
	}
}
