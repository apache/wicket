/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;

import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequest;
import wicket.util.lang.Bytes;
import wicket.util.value.ValueMap;

/**
 * WebRequest subclass for multipart content uploads.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Cameron Braid
 */
public class MultipartWebRequest extends WebRequest
{
	/** Map of file items. */
	private final ValueMap files = new ValueMap();

	/** Map of parameters. */
	private final ValueMap parameters = new ValueMap();
	
	/**
	 * Constructor
	 * 
	 * @param maxSize the maximum size this request may be
	 * @param httpServletRequest
	 *            The servlet request
	 * @throws FileUploadException Thrown if something goes wrong with upload
	 */
	public MultipartWebRequest(Bytes maxSize, final HttpServletRequest httpServletRequest) throws FileUploadException
	{
		super(httpServletRequest);

		// Check that request is multipart
		final boolean isMultipart = FileUpload.isMultipartContent(httpServletRequest);
		if (!isMultipart)
		{
			throw new IllegalStateException("Request does not contain multipart content");
		}

        // The encoding that will be used to decode the string parameters
        // It should NOT be null at this point, but it may be 
        // if the older Servlet API 2.2 is used
        String encoding = httpServletRequest.getCharacterEncoding();

		// Parse multipart request into items
		final DiskFileUpload diskFileUpload = new DiskFileUpload();

		// set encoding specifically when we found it
		if (encoding != null)
		{
			diskFileUpload.setHeaderEncoding(encoding);
		}

		diskFileUpload.setSizeMax(maxSize.bytes());
		final List items = diskFileUpload.parseRequest(httpServletRequest);

		// Loop through items
		for (Iterator i = items.iterator(); i.hasNext();)
		{
			// Get next item
			final FileItem item = (FileItem)i.next();

			// If item is a form field
			if (item.isFormField())
			{
				// Set parameter value
				final String value;
				if (encoding != null)
				{
					try
					{
						value = item.getString(encoding);
					}
					catch (UnsupportedEncodingException e)
					{
						throw new WicketRuntimeException(e);
					}
				}
				else
				{
					value = item.getString();
				}
				parameters.put(item.getFieldName(), value);
			}
			else
			{
				// Add to file list
				files.put(item.getFieldName(), item);
			}
		}
	}

	/**
	 * @return Returns the files.
	 */
	public Map getFiles()
	{
		return files;
	}

	/**
	 * Gets the file that was uploaded using the given field name.
	 * 
	 * @param fieldName
	 *            the field name that was used for the upload
	 * @return the upload with the given field name
	 */
	public FileItem getFile(final String fieldName)
	{
		return (FileItem)files.get(fieldName);
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getParameter(java.lang.String)
	 */
	public String getParameter(final String key)
	{
		return parameters.getString(key);
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getParameterMap()
	 */
	public Map getParameterMap()
	{
		return parameters;
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getParameters(java.lang.String)
	 */
	public String[] getParameters(final String key)
	{
		String val = getParameter(key);
		return (val != null) ? val.split(",") : null;
	}
}
