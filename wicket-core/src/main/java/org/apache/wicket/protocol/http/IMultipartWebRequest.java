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
package org.apache.wicket.protocol.http;

import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;


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
	public Map<String, List<FileItem>> getFiles();

	/**
	 * Gets the files that were uploaded using the given field name.
	 * 
	 * @param fieldName
	 *            the field name that was used for the upload
	 * @return the uploads with the given field name
	 */
	public List<FileItem> getFile(final String fieldName);
}
