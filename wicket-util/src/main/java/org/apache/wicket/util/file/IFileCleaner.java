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
package org.apache.wicket.util.file;

import java.io.File;

import org.apache.commons.io.FileDeleteStrategy;

/**
 * Keeps track of files awaiting deletion, and deletes them when an associated marker object is
 * reclaimed by the garbage collector.
 * 
 * <p>
 * Needed to remove files created temporarily for the needs of FileUpload functionality.
 */
public interface IFileCleaner
{

	/**
	 * Track the specified file, using the provided marker, deleting the file when the marker
	 * instance is garbage collected.
	 * 
	 * @param file
	 *            the file to be tracked, not null
	 * @param marker
	 *            the marker object used to track the file, not null
	 * @throws NullPointerException
	 *             if the file is null
	 */
	void track(File file, Object marker);

	/**
	 * Track the specified file, using the provided marker, deleting the file when the marker
	 * instance is garbage collected.
	 * 
	 * @param file
	 *            the file to be tracked, not null
	 * @param marker
	 *            the marker object used to track the file, not null
	 * @param deleteStrategy
	 *            the strategy that actually deletes the file. E.g. to delete a non-empty folder the
	 *            strategy should delete all children first
	 * @throws NullPointerException
	 *             if the file is null
	 */
	void track(File file, Object marker, FileDeleteStrategy deleteStrategy);

	/**
	 * Call this method to stop the cleaner and to free all allocated resources by it
	 */
	void destroy();
}
