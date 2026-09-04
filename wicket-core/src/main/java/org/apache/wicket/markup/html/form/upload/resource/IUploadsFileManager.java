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

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.util.file.File;

/**
 * This interface defines the bridge between a file uploaded to a resource and the wicket component.
 * Wicket component uses some identifier (passed as a request parameter) to instruct the resource
 * how to store file. Same identifier is used to retrieve the file, once uploaded, from component in page.
 * Mind that uploader resource is a singleton => identifier needs to be unique among different sessions
 * (and pages in a session).
 */
public interface IUploadsFileManager
{

	/**
	 * Saves an uploaded files into some persistent storage (e,g, disk).
	 *
	 * @param fileItem
	 *            The {@link FileUpload}
	 * @param uploadFieldId
	 *            The unique ID of the upload field.
	 */
	void save(FileUpload fileItem, String uploadFieldId);

	/**
	 * Retrieves the file based on an uploadFieldId and clientFileName.
	 *
	 * @param uploadFieldId
	 *            The unique ID of the upload field.
	 * @param clientFileName
	 *            The client file name
	 * @return File the file
	 */
	File getFile(String uploadFieldId, String clientFileName);
}
