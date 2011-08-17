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
package org.apache.wicket.util.license;

import java.io.File;
import java.util.List;

interface ILicenseHeaderHandler
{
	/**
	 * @return The suffixes that matches the files that it handles.
	 */
	List<String> getSuffixes();

	/**
	 * @return The files to ignore.
	 */
	List<String> getIgnoreFiles();

	/**
	 * @param file
	 *            Add a license header to the file.
	 * @return True if the license were added. False if not.
	 */
	boolean addLicenseHeader(File file);

	/**
	 * @param file
	 *            The file to check for a correct license header.
	 * @return True if the file has a correct license header. False if not.
	 */
	boolean checkLicenseHeader(File file);

	/**
	 * Get the license type of the file. F.ex. ASL2, ASL1.1, GPL, LGPL etc.
	 * 
	 * @param file
	 *            THe file to get the license type from.
	 * @return The license type of the file. Null if no license header in the file.
	 */
	String getLicenseType(File file);
}
