/*
 * $Id: IMultipartWebRequest.java 4092 2006-02-02 21:33:16 +0000 (Thu, 02 Feb
 * 2006) eelco12 $
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
package wicket.protocol.http;

import java.util.Map;

import wicket.util.upload.FileItem;

/**
 * An interface providing access to multipart content uploads of a WebRequest
 * 
 * @author Ate Douma
 */
public interface IMultipartWebRequest
{
	/**
	 * @return Returns the files.
	 */
	public Map getFiles();

	/**
	 * Gets the file that was uploaded using the given field name.
	 * 
	 * @param fieldName
	 *            the field name that was used for the upload
	 * @return the upload with the given field name
	 */
	public FileItem getFile(final String fieldName);
}
