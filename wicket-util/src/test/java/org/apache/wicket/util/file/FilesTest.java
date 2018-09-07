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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Files}
 */
public class FilesTest
{
	/**
	 * Tests for {@link Files#remove(java.io.File)}
	 * 
	 * @throws IOException
	 */
	@Test
	public void remove() throws IOException
	{
		assertFalse(Files.remove(null), "'null' files are not deleted.");

		assertFalse(
			Files.remove(new File(
				"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111")),
			"Non existing files are not deleted.");

		java.io.File file = getFile();
		file.createNewFile();
		assertTrue(file.isFile(), "The just created file should exist!");

		boolean removed = Files.remove(file);
		assertFalse(file.exists(), "The just removed file should not exist!");
		assertTrue(removed, "Files.remove(file) should remove the file");

		// try to remove non-existing file
		removed = Files.remove(file);
		assertFalse(file.exists(), "The just removed file should not exist!");
		assertFalse(removed, "Files.remove(file) should not remove the file");

		// try to remove a folder
		java.io.File folder = getFolder();
		Files.mkdirs(folder);
		assertTrue(folder.isDirectory());
		assertFalse(Files.remove(folder), "Should not be able to delete a folder, even empty one.");
		assertTrue(Files.removeFolder(folder), "Should not be able to delete a folder.");
	}

	/**
	 * @return a reference to a file, without creating it !
	 * @throws IOException
	 */
	private java.io.File getFile() throws IOException
	{
		return new java.io.File(System.getProperty("java.io.tmpdir"), "wicket-7.x-test-file");
	}

	/**
	 * @return a reference to a folder, without creating it !
	 */
	private java.io.File getFolder()
	{
		File folder = new File(System.getProperty("java.io.tmpdir"), "wicket-7.x-test-folder");
		return folder;
	}

	/**
	 * WICKET-6236 - honoring the javadoc by putting a wait only after the 10th failed attempt to
	 * delete a file
	 */
	@Test
	public void dontWaitTooMuchIfCantDelete()
	{
		java.io.File f = mock(java.io.File.class);
		when(f.isFile()).thenReturn(true);
		when(f.delete()).thenReturn(false);
		long start = currentTimeMillis();
		Files.remove(f);
		long end = currentTimeMillis();
		assertTrue((end - start) < 5000l);
	}

	/**
	 * Tests for {@link Files#removeFolder(java.io.File)}
	 *
	 * @throws Exception
	 */
	@Test
	public void removeFolder() throws Exception
	{
		assertFalse(Files.removeFolder(null), "'null' folders are not deleted.");

		assertFalse(
			Files.removeFolder(new File(
				"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111")),
			"Non existing folders are not deleted.");

		java.io.File folder = new File(System.getProperty("java.io.tmpdir"), "wicket-test-folder");
		Files.mkdirs(folder);
		assertTrue(folder.isDirectory());
		File file = new File(folder, "child");
		file.createNewFile();
		assertTrue(file.exists());

		assertTrue(Files.removeFolder(folder), "Should be able to delete a folder.");
	}

	/**
	 * Tests for {@link Files#removeAsync(java.io.File, IFileCleaner)}
	 *
	 * @throws Exception
	 */
	@Test
	@Disabled // the test is unreliable on the CI server
	public void removeAsync() throws Exception
	{

		IFileCleaner fileCleaner = new FileCleaner();

		assertFalse(Files.removeAsync(null, fileCleaner), "'null' files are not deleted.");

		File nonExistingFile = new File(
			"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111");
		assertTrue(Files.removeFolderAsync(nonExistingFile, fileCleaner),
			"Even non existing file are scheduled for deletion.");
		assertFalse(nonExistingFile.exists());

		java.io.File file = getFile();
		file.createNewFile();
		assertTrue(file.exists(), "The just created file should exist!");
		assertTrue(file.isFile());

		assertTrue(Files.removeAsync(file, fileCleaner), "The file is scheduled for deletion.");

		// remove the reference to the file to be deleted
		// this way the FileCleaningTracker's ReferenceQueue will mark it as eligible for GC
		file = null;

		// give chance to the file cleaner to run and delete the folder
		System.gc();
		boolean exists = true;
		for (int i = 0; i < 10; i++)
		{
			Thread.sleep(5);
			java.io.File newFileReference = getFile();
			if (!newFileReference.exists())
			{
				exists = false;
				break;
			}
		}
		assertFalse(exists, "The file no longer exists");
	}

	/**
	 * Tests for {@link Files#removeFolderAsync(java.io.File, IFileCleaner)}
	 *
	 * @throws Exception
	 */
	@Test
	@Disabled // the test is unreliable on the CI server
	public void removeFolderAsync() throws Exception
	{
		assertFalse(Files.removeFolderAsync(null, null), "'null' folders are not deleted.");

		IFileCleaner fileCleaner = new FileCleaner();

		File nonExistingFolder = new File(
			"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111");
		assertTrue(Files.removeFolderAsync(nonExistingFolder, fileCleaner),
			"Even non existing folders are scheduled for deletion.");
		assertFalse(nonExistingFolder.exists());

		java.io.File folder = getFolder();
		Files.mkdirs(folder);
		assertTrue(folder.isDirectory());
		File file = new File(folder, "child");
		file.createNewFile();
		assertTrue(file.exists());

		assertTrue(Files.removeFolderAsync(folder, fileCleaner),
			"The folder is scheduled for deletion.");

		// remove the reference to the folder to be deleted
		// this way the FileCleaningTracker's ReferenceQueue will mark it as eligible for GC
		folder = null;

		// give chance to the file cleaner to run and delete the folder
		System.gc();
		Thread.sleep(5);
		java.io.File newFolderReference = getFolder();
		assertFalse(newFolderReference.exists(), "The folder still exists");
	}

	/**
	 * WICKET-4509
	 *
	 * @throws Exception
	 */
	@Test
	public void fileWithWhitespace() throws Exception
	{
		URL url = new URL("file:/file%20with%20whitespace");

		assertEquals(java.io.File.separator + "file with whitespace",
			Files.getLocalFileFromUrl(url).getPath());
	}

}
