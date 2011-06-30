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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link Files}
 */
public class FilesTest extends Assert
{

	/**
	 * @throws IOException
	 */
	@Test
	public void remove() throws IOException
	{
		java.io.File file = java.io.File.createTempFile("wicket-test--", ".tmp");
		assertTrue("The just created file should exist!", file.exists());

		boolean removed = Files.remove(file);
		assertFalse("The just removed file should not exist!", file.exists());
		assertTrue("Files.remove(file) should remove the file", removed);
	}
}
