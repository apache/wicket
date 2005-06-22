/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.fileupload.FileItem;

import wicket.util.file.Files;

/**
 * Model for file uploads.
 * 
 * @author Jonathan Locke
 */
public class FileUpload implements Serializable
{
	final FileItem item;

	/**
	 * Constructor
	 * 
	 * @param item
	 *            The uploaded file item
	 */
	FileUpload(final FileItem item)
	{
		this.item = item;
	}

	/**
	 * @return Uploaded file as an array of bytes
	 */
	public byte[] getBytes()
	{
		return item.get();
	}
	
	/**
	 * @return Content type for upload
	 */
	public String getContentType()
	{
		return item.getContentType();
	}

	/**
	 * @return File object for client-side file that was uploaded.
	 */
	public File getFile()
	{
		return new File(item.getName());
	}

	/**
	 * @return Input stream with file contents.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException
	{
		return item.getInputStream();
	}

	/**
	 * @return The upload's size
	 */
	public long getSize()
	{
		return item.getSize();
	}

	/**
	 * Saves this file upload to a given file on the server side.
	 * 
	 * @param file
	 *            The file
	 * @throws IOException
	 */
	public void writeTo(final File file) throws IOException
	{
		InputStream is = getInputStream();
		try
		{
			Files.writeTo(file, is);
		}
		finally
		{
			is.close();
		}
	}
}
