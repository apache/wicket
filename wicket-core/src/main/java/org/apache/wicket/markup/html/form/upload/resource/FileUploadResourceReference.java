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

import java.util.List;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

/**
 * A resource reference that provides default implementation of AbstractFileUploadResource.
 * The implementation generates JSON response with data from the upload (this data is
 * re-routed to the page for things like getting the client file name and file size).
 */
public class FileUploadResourceReference extends ResourceReference
{

	private final IUploadsFileManager uploadFileManager;

	private static FileUploadResourceReference instance;

	/**
	 * This method assumes {@link #createNewInstance(IUploadsFileManager)} was called before
	 *
	 * @return FileUploadResourceReference
	 */
	public static FileUploadResourceReference getInstance()
	{
		if (instance == null)
		{
			throw new IllegalStateException("An instance should be created via the createNewInstance method");
		}
		return instance;
	}

	/**
	 * Use this method in order to create an instance to be mounted at application level.
	 *
	 * @param fileManager The {@link IUploadsFileManager}
	 * @return FileUploadResourceReference
	 */
	public static FileUploadResourceReference createNewInstance(IUploadsFileManager fileManager)
	{
		if (instance == null)
		{
			instance = new FileUploadResourceReference(fileManager);
		}
		return instance;
	}

	protected FileUploadResourceReference(IUploadsFileManager uploadFileManager)
	{
		super(FileUploadResourceReference.class, "file-uploads");

		Args.notNull(uploadFileManager, "uploadFileManager");

		this.uploadFileManager = uploadFileManager;
	}

	@Override
	public IResource getResource()
	{
		return new AbstractFileUploadResource(uploadFileManager)
		{

			@Override
			protected String generateJsonResponse(ResourceResponse resourceResponse, ServletWebRequest webRequest, List<FileUpload> files)
			{
				JSONArray json = new JSONArray();

				for (FileUpload fileItem : files)
				{
					JSONObject fileJson = new JSONObject();

					try
					{
						generateFileInfo(fileJson, fileItem);
						json.put(fileJson);
					}
					catch (JSONException e)
					{
						throw new RuntimeException(e);
					}
				}
				return json.toString();
			}

		};
	}

	public IUploadsFileManager getUploadFileManager() {
		return uploadFileManager;
	}

	protected void generateFileInfo(JSONObject fileJson, FileUpload fileItem)
	{
		fileJson.put("clientFileName", fileItem.getClientFileName());
		fileJson.put("size", fileItem.getSize());
		fileJson.put("contentType", fileItem.getContentType());
	}

}
