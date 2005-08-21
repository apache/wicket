/*
 * $Id$
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
package wicket.protocol.http.portlet;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;


import wicket.WicketRuntimeException;
import wicket.protocol.http.MultipartWebRequest;
import wicket.util.lang.Bytes;
import wicket.util.upload.DiskFileItemFactory;
import wicket.util.upload.FileItem;
import wicket.util.upload.FileUploadException;
import wicket.util.upload.PortletFileUpload;
import wicket.util.value.ValueMap;

/**
 * Portlet specific WebRequest subclass for multipart content uploads.
 * 
 * @author Ate Douma
 */
public class MultipartPortletWebRequest extends PortletWebRequest implements MultipartWebRequest
{
	/** Map of file items. */
	private final ValueMap files = new ValueMap();

	/** Map of parameters. */
	private final ValueMap parameters = new ValueMap();
	
	/**
	 * Constructor
	 * 
	 * @param maxSize the maximum size this request may be
	 * @param request the portlet (Action)request
	 * @param servletPath the WicketServlet applicationPath parameter
	 * @throws FileUploadException Thrown if something goes wrong with upload
	 */
	public MultipartPortletWebRequest(PortletRequest request, String servletPath, Bytes maxSize) throws FileUploadException
	{
		super(request, servletPath);
		
		ActionRequest actionRequest = (ActionRequest)request;

		// Check that request is multipart
		final boolean isMultipart = PortletFileUpload.isMultipartContent(actionRequest);
		if (!isMultipart)
		{
			throw new IllegalStateException("PortletRequest does not contain multipart content");
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();		

        // Configure the factory here, if desired.
        PortletFileUpload upload = new PortletFileUpload(factory);
        
        // The encoding that will be used to decode the string parameters
        // It should NOT be null at this point, but it may be 
        // if the older Servlet API 2.2 is used
        String encoding = actionRequest.getCharacterEncoding();

		// set encoding specifically when we found it
		if (encoding != null)
		{
			upload.setHeaderEncoding(encoding);
		}

		upload.setSizeMax(maxSize.bytes());
		final List items = upload.parseRequest(actionRequest);

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
