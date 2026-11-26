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
 * <p>
 * An implementation implements {@link #store(FileUpload, String)} and
 * {@link #getFile(String, String)}.
 */
public interface IUploadsFileManager
{

	/**
	 * Saves an uploaded file into some persistent storage (e.g. disk), under a name of its own:
	 * files of the same client file name, sent in one upload or in several, must not replace each
	 * other.
	 * <p>
	 * The name goes to the browser, which sends it back for {@link #getFile(String, String)} to
	 * find the file. It must not reveal anything the user is not supposed to know.
	 * <p>
	 * The default implementation saves the file with {@link #save(FileUpload, String)} and returns
	 * its client file name, for implementations written before this method existed. An
	 * implementation has to implement one of the two methods.
	 *
	 * @param fileItem
	 *            The {@link FileUpload}
	 * @param uploadFieldId
	 *            The unique ID of the upload field.
	 * @return the name the file was saved under, unique among the files of the upload field
	 * @since 11.0.0
	 */
	default String store(FileUpload fileItem, String uploadFieldId)
	{
		save(fileItem, uploadFieldId);
		return fileItem.getClientFileName();
	}

	/**
	 * Saves an uploaded file into some persistent storage (e.g. disk), under its client file name.
	 * <p>
	 * Wicket no longer calls this method, only the default {@link #store(FileUpload, String)} does.
	 * The default implementation throws an {@link UnsupportedOperationException}: an
	 * implementation that implements neither method fails at its first upload with an exception
	 * saying so.
	 *
	 * @param fileItem
	 *            The {@link FileUpload}
	 * @param uploadFieldId
	 *            The unique ID of the upload field.
	 * @deprecated files saved under their client file name replace each other when they share it.
	 *             Implement and call {@link #store(FileUpload, String)} instead.
	 */
	@Deprecated(since = "11.0.0", forRemoval = true)
	default void save(FileUpload fileItem, String uploadFieldId)
	{
		throw new UnsupportedOperationException(getClass().getName() +
			" does not implement the deprecated save(FileUpload, String): it has to implement " +
			"store(FileUpload, String), which is to be called instead");
	}

	/**
	 * Retrieves the file saved under a name by {@link #store(FileUpload, String)}.
	 * <p>
	 * The name comes back from the browser, which can change it: an implementation must never
	 * return a file that does not belong to the upload field.
	 *
	 * @param uploadFieldId
	 *            The unique ID of the upload field.
	 * @param fileName
	 *            The name {@link #store(FileUpload, String)} returned for the file
	 * @return File the file
	 */
	File getFile(String uploadFieldId, String fileName);
}
