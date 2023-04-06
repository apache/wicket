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
package org.apache.wicket.markup.html.form.upload.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload2.FileItem;
import org.apache.commons.fileupload2.pub.FileUploadSizeException;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.openjson.JSONObject;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The resource that handles the file uploads.
 * Reads the file items from the request parameters and uses {@link IUploadsFileManager}
 * to store them.
 * Additionally, cares about the response's content type and body.
 * <p>
 * This code was adapted from
 * <p>
 * <a href="https://github.com/martin-g/blogs/blob/master/file-upload/src/main/java/com/mycompany/fileupload/AbstractFileUploadResource.java">AbstractFileUploadResource.java</a>
 * <p>
 * The main difference is that there some JQuery plugin is used at client side (and it supports multiple uploads +
 * some UI allowing to delete/preview files and so on).
 * Here we are just using plain jQuery code at client side to upload a single file.
 */
public abstract class AbstractFileUploadResource extends AbstractResource
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractFileUploadResource.class);

	public static final String PARAM_NAME = "WICKET-FILE-UPLOAD";

	/**
	 * This resource is usually an application singleton. Thus, client side pass
	 * to the resource a unique ID identifying the upload field performing the upload.
	 * The upload file makes sure this is a unique identifier at application level, See
	 * {@link FileUploadToResourceField#generateAUniqueApplicationWiseId()}. So that, there are no clashes between
	 * different users/pages/sessions performing an upload.
	 */
	public static final String UPLOAD_ID = "uploadId";
	/**
	 * i18n key for case no files were selected.
	 */
	public static final String NO_FILE_SELECTED = "wicket.no.files.selected";
	/**
	 * i18n key for the case selcted files exceed size limit.
	 */
	public static final String REQUEST_SIZE_LIMIT_EXCEEDED = "wicket.multipart.size.exceeded";

	private final IUploadsFileManager fileManager;

	public AbstractFileUploadResource(IUploadsFileManager fileManager)
	{
		this.fileManager = fileManager;
	}

	/**
	 * Reads and stores the uploaded files
	 *
	 * @param attributes
	 *            Attributes
	 * @return ResourceResponse
	 */
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		final ResourceResponse resourceResponse = new ResourceResponse();

		final ServletWebRequest webRequest = (ServletWebRequest) attributes.getRequest();

		// get the ID of the upload field (it should be unique per application)
		String uploadId = webRequest.getRequestParameters().getParameterValue(UPLOAD_ID).toString("resource");

		try
		{
			MultipartServletWebRequest multiPartRequest = webRequest.newMultipartWebRequest(getMaxSize(), uploadId);
			multiPartRequest.parseFileParts();

			RequestCycle.get().setRequest(multiPartRequest);

			// retrieve the files.
			Map<String, List<FileItem>> files = multiPartRequest.getFiles();
			List<FileItem> fileItems = files.get(PARAM_NAME);

			if (fileItems != null)
			{
				List<FileUpload> fileUploads = new ArrayList<>();
				for (FileItem fileItem : fileItems)
				{
					fileUploads.add(new FileUpload(fileItem));
				}
				saveFiles(fileUploads, uploadId);
				prepareResponse(resourceResponse, webRequest, fileUploads);
			}
			else
			{
				resourceResponse.setContentType("application/json");
				resourceResponse.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes) throws IOException
					{
						JSONObject json = new JSONObject();
						json.put("error", true);
						json.put("errorMessage", NO_FILE_SELECTED);
						String error = json.toString();
						attributes.getResponse().write(error);
					}
				});
			}

		}
		catch (Exception fux)
		{
			LOG.error("An error occurred while uploading a file", fux);
			if (fux instanceof FileUploadSizeException)
			{
				resourceResponse.setStatusCode(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
				return resourceResponse;
			}
			resourceResponse.setContentType("application/json");
			JSONObject json = new JSONObject();
			json.put("error", true);
			String errorMessage = LOG.isDebugEnabled() ? fux.getMessage() : "An error occurred while uploading files";
			json.put("errorMessage", errorMessage);
			String error = json.toString();
			resourceResponse.setWriteCallback(new WriteCallback()
			{
				@Override
				public void writeData(Attributes attributes) throws IOException
				{
					attributes.getResponse().write(error);
				}
			});
		}

		return resourceResponse;
	}

	/**
	 * Sets the response's content type and body
	 *
	 * @param resourceResponse
	 *            ResourceResponse
	 * @param webRequest
	 *            ServletWebRequest
	 * @param fileItems
	 *            List<FileUpload>
	 */
	protected void prepareResponse(ResourceResponse resourceResponse, ServletWebRequest webRequest, List<FileUpload> fileItems)
	{
		resourceResponse.setContentType("application/json");
		final String responseContent = generateJsonResponse(resourceResponse, webRequest, fileItems);

		resourceResponse.setWriteCallback(new WriteCallback()
		{
			@Override
			public void writeData(Attributes attributes) throws IOException
			{
				attributes.getResponse().write(responseContent);
			}
		});
	}

	/**
	 * Delegates to FileManager to store the uploaded files
	 *
	 * @param fileItems
	 *            List<FileUpload>
	 */
	protected void saveFiles(List<FileUpload> fileItems, String uploadId)
	{
		for (FileUpload fileItem : fileItems)
		{
			fileManager.save(fileItem, uploadId);
		}
	}

	/**
	 * Defines what is the maximum size of the uploaded files.
	 *
	 * @return Bytes
	 */
	protected abstract Bytes getMaxSize();

	/**
	 * Should generate the response's body in JSON format
	 *
	 * @param resourceResponse
	 *            ResourceResponse
	 * @param webRequest
	 *            ServletWebRequest
	 * @param files
	 *            List<FileUpload>
	 * @return The generated JSON
	 */
	protected abstract String generateJsonResponse(ResourceResponse resourceResponse,
			ServletWebRequest webRequest, List<FileUpload> files);

}