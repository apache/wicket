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
package org.apache.wicket.extensions.ajax.markup.html;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;

/**
 * Uploads files from a drop event.
 *
 * @author Andrew Kondratev
 * @author svenmeier
 */
public class AjaxFileDropBehavior extends AjaxEventBehavior
{

	private static final ResourceReference JS = new PackageResourceReference(AjaxFileDropBehavior.class, "datatransfer.js");

	/**
	 *  Maximum size of all uploaded files in bytes in a request.
	 */
	private Bytes maxSize;

	/**
	 *  Maximum size of file of upload in bytes (if there are more than one) in a request.
	 */
	private Bytes fileMaxSize;

	private String parameterName = "f";
	
	public AjaxFileDropBehavior()
	{
		super("drop");
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(JS));
		
		// default must be prevented for dragover event, otherwise browser will consume the dataTransfer
		response.render(OnDomReadyHeaderItem.forScript(String.format("jQuery('#%s').on('dragover', function(e) { e.preventDefault(); });", component.getMarkupId())));
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		attributes.setMultipart(true);
		attributes.setMethod(Method.POST);
		// default must be prevented, otherwise browser will consume the dataTransfer
		attributes.setPreventDefault(true);

		attributes.getDynamicExtraParameters().add(
			String.format("return Wicket.DataTransfer.getFilesAsParamArray(attrs.event.originalEvent, '%s');", parameterName)
		);
	}

	@Override
	protected void onEvent(AjaxRequestTarget target)
	{
		try
		{
			ServletWebRequest request = (ServletWebRequest)getComponent().getRequest();
			final MultipartServletWebRequest multipartWebRequest = request.newMultipartWebRequest(
				getMaxSize(), getComponent().getPage().getId());
			multipartWebRequest.setFileMaxSize(getFileMaxSize());
			multipartWebRequest.parseFileParts();

			// TODO: Can't this be detected from header?
			getComponent().getRequestCycle().setRequest(multipartWebRequest);
			
			ArrayList<FileUpload> fileUploads = new ArrayList<>();

			// Get the item for the path
			final List<FileItem> fileItems = multipartWebRequest.getFile(parameterName);

			if (fileItems != null)
			{
				for (FileItem item : fileItems)
				{
					fileUploads.add(new FileUpload(item));
				}
			}
			
			onFileUpload(target, fileUploads);
		}
		catch (final FileUploadException fux)
		{
			onError(fux);
		}
	}

	public Bytes getMaxSize()
	{
		if (maxSize == null) {
			maxSize = getComponent().getApplication().getApplicationSettings().getDefaultMaximumUploadSize();
		}
		return maxSize;
	}

	public void setMaxSize(Bytes maxSize)
	{
		Args.notNull(maxSize, "maxSize");
		this.maxSize = maxSize;
	}

	public Bytes getFileMaxSize()
	{
		return fileMaxSize;
	}

	public void setFileMaxSize(Bytes fileMaxSize)
	{
		this.fileMaxSize = fileMaxSize;
	}
	
	protected void onFileUpload(AjaxRequestTarget target, List<FileUpload> files)
	{
	}

	protected void onError(FileUploadException fux)
	{
	}
}