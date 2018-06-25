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
package org.apache.wicket.pageStore.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link NestedFolders}.
 */
public class NestedFoldersTest
{

	@Test
	public void escapedName() throws IOException
	{
		NestedFolders folders = new NestedFolders(Files.createTempDirectory(null).toFile());
		
		File sessionFolder = folders.get("x*x/x:x", false);
		
		assertEquals("x_x_x_x", sessionFolder.getName());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4478
	 *
	 * Tests that the folder where a session data is put is partitioned, i.e. it is put in folders
	 * which names are automatically calculated on the fly.
	 * 
	 * @throws IOException
	 */
	@Test
	public void parentFoldersAreRemovedIfEmpty() throws IOException
	{
		NestedFolders folders = new NestedFolders(Files.createTempDirectory(null).toFile());

		String sessionId = "abcdefg";
		File sessionFolder = folders.get(sessionId, true);

		assertEquals("abcdefg", sessionFolder.getName());
		assertEquals("1279", sessionFolder.getParentFile().getName());
		assertEquals("7141", sessionFolder.getParentFile().getParentFile().getName());
		assertEquals(folders.getBase(), sessionFolder.getParentFile().getParentFile().getParentFile());

		folders.remove(sessionId);

		// assert that the 'sessionId' folder and the parents two levels up are removed
		assertFalse(sessionFolder.getParentFile().getParentFile().exists());
	}

	@Test
	public void parentFoldersAreKeptWhenNotEmpty() throws IOException
	{
		NestedFolders folders = new NestedFolders(Files.createTempDirectory(null).toFile());

		String sessionId = "hijklmn";
		File sessionFolder = folders.get(sessionId, true);

		assertEquals("hijklmn", sessionFolder.getName());
		assertEquals("2326", sessionFolder.getParentFile().getName());
		assertEquals("7548", sessionFolder.getParentFile().getParentFile().getName());
		assertEquals(folders.getBase(), sessionFolder.getParentFile().getParentFile().getParentFile());

		// create additional folder inside the nested folders
		new File(sessionFolder.getParentFile().getParentFile(), "2345").mkdirs();
		
		folders.remove(sessionId);

		// assert that the 'sessionId' folder and the parent one level up are removed
		assertFalse(sessionFolder.getParentFile().exists());
		// ... but the parent two levels up still exists
		assertTrue(sessionFolder.getParentFile().getParentFile().exists());

	}
}
