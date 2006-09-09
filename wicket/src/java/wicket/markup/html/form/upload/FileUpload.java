/*
 * $Id$
 * $Revision$
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wicket.Application;
import wicket.RequestCycle;
import wicket.util.file.Files;
import wicket.util.upload.FileItem;

/**
 * Model for file uploads.
 * 
 * @author Jonathan Locke
 */
public class FileUpload implements Serializable
{
	private static final long serialVersionUID = 1L;

	final FileItem item;
	
	private List<InputStream> inputStreams;

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
	 * Deletes temp file from disk
	 */
	public void delete()
	{
		item.delete();
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
	 * @since 1.2
	 * @return name of uploaded client side file
	 */
	public String getClientFileName()
	{
		return item.getName();
	}


	/**
	 * Get an input stream for the file uploaded. Use this input stream if you
	 * can't use {@link #writeTo(File)} for persisting the uploaded file. This
	 * can be if you need to react upon the content of the file or need to
	 * persist it elsewhere, i.e. a database or external filesystem.
	 * <p>
	 * <b>PLEASE NOTE!</b><br>
	 * The InputStream return will be closed be Wicket at the end of the request.
	 * If you need it across a request you need to hold on to this FileUpload
	 * instead.
	 * @return Input stream with file contents.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException
	{
		if (inputStreams == null) {
			inputStreams = new ArrayList<InputStream>();
		}
		
		InputStream is = item.getInputStream();
		inputStreams.add(is);
		
		return is;
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

	/**
	 * Convinience method that copies the input stream returned by
	 * {@link #getInputStream()} into a temporary file.
	 * <p>
	 * Only use this if you actually need a {@link File} to work with, in all
	 * other cases use {@link #getInputStream()} or {@link #getBytes()}
	 * 
	 * @since 1.2
	 * 
	 * @return temporary file containing the contents of the uploaded file
	 * @throws IOException
	 */
	public final File writeToTempFile() throws IOException
	{
		String sessionId = Application.get().getSessionStore().getSessionId(
				RequestCycle.get().getRequest(), true);
		File temp = File.createTempFile(sessionId, item.getFieldName());
		writeTo(temp);
		return temp;
	}

	/**
	 * Close the streams which has been opened when getting the InputStream
	 * using {@link #getInputStream()}. All the input streams are closed at the
	 * end of the request. This is done when the FileUploadField, which is
	 * associated with this FileUpload is detached.
	 * <p>
	 * If an exception is thrown when closing the input streams, we ignore it,
	 * because the stream might have been closed already.
	 */
	void closeStreams()
	{
		if (inputStreams != null)
		{
			for (InputStream inputStream : inputStreams)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
					// We don't care aobut the exceptions thrown here.
				}
			}
			
			// Reset the list
			inputStreams = null;
		}
	}
}
