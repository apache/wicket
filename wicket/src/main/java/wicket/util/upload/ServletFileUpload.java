/*
 * $Id: ServletFileUpload.java 4710 2006-03-02 08:46:15 +0000 (Thu, 02 Mar 2006)
 * eelco12 $ $Revision$ $Date: 2006-03-02 08:46:15 +0000 (Thu, 02 Mar
 * 2006) $
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
package wicket.util.upload;

import java.util.List;

import javax.servlet.http.HttpServletRequest;


/**
 * <p>
 * High level API for processing file uploads.
 * </p>
 * 
 * <p>
 * This class handles multiple files per single HTML widget, sent using
 * <code>multipart/mixed</code> encoding type, as specified by <a
 * href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>. Use {@link
 * #parseRequest(HttpServletRequest)} to acquire a list of {@link
 * wicket.util.upload.FileItem}s associated with a given HTML widget.
 * </p>
 * 
 * <p>
 * How the data for individual parts is stored is determined by the factory used
 * to create them; a given part may be in memory, on disk, or somewhere else.
 * </p>
 * 
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 */
public class ServletFileUpload extends FileUpload
{

	// ---------------------------------------------------------- Class methods


	/**
	 * Utility method that determines whether the request contains multipart
	 * content.
	 * 
	 * @param request
	 *            The servlet request to be evaluated. Must be non-null.
	 * 
	 * @return <code>true</code> if the request is multipart;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean isMultipartContent(HttpServletRequest request)
	{
		if (!"post".equals(request.getMethod().toLowerCase()))
		{
			return false;
		}
		return FileUploadBase.isMultipartContent(new ServletRequestContext(request));
	}


	// ----------------------------------------------------------- Constructors


	/**
	 * Constructs an uninitialised instance of this class. A factory must be
	 * configured, using <code>setFileItemFactory()</code>, before attempting
	 * to parse requests.
	 */
	public ServletFileUpload()
	{
		super();
	}


	/**
	 * Constructs an instance of this class which uses the supplied factory to
	 * create <code>FileItem</code> instances.
	 * 
	 * @param fileItemFactory
	 */
	public ServletFileUpload(FileItemFactory fileItemFactory)
	{
		super(fileItemFactory);
	}


	// --------------------------------------------------------- Public methods


	/**
	 * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
	 * compliant <code>multipart/form-data</code> stream.
	 * 
	 * @param request
	 *            The servlet request to be parsed.
	 * 
	 * @return A list of <code>FileItem</code> instances parsed from the
	 *         request, in the order that they were transmitted.
	 * 
	 * @exception FileUploadException
	 *                if there are problems reading/parsing the request or
	 *                storing files.
	 */
	public List /* FileItem */parseRequest(HttpServletRequest request) throws FileUploadException
	{
		return parseRequest(new ServletRequestContext(request));
	}
}
