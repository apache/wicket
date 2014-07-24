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
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link Files}
 */
public class FilesTest extends Assert
{
	/**
	 * Tests for {@link Files#remove(java.io.File)}
	 * 
	 * @throws IOException
	 */
	@Test
	public void remove() throws IOException
	{
		assertFalse("'null' files are not deleted.", Files.remove(null));

		assertFalse("Non existing files are not deleted.", Files.remove(new File(
				"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111")));

		java.io.File file = getFile();
		file.createNewFile();
		assertTrue("The just created file should exist!", file.isFile());

		boolean removed = Files.remove(file);
		assertFalse("The just removed file should not exist!", file.exists());
		assertTrue("Files.remove(file) should remove the file", removed);

		// try to remove non-existing file
		removed = Files.remove(file);
		assertFalse("The just removed file should not exist!", file.exists());
		assertFalse("Files.remove(file) should not remove the file", removed);

		// try to remove a folder
		java.io.File folder = getFolder();
		Files.mkdirs(folder);
		assertTrue(folder.isDirectory());
		assertFalse("Should not be able to delete a folder, even empty one.", Files.remove(folder));
		assertTrue("Should not be able to delete a folder.", Files.removeFolder(folder));
	}

	/**
	 * Tests for {@link Files#removeFolder(java.io.File)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void removeFolder() throws Exception
	{
		assertFalse("'null' folders are not deleted.", Files.removeFolder(null));

		assertFalse("Non existing folders are not deleted.", Files.removeFolder(new File(
			"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111")));

		java.io.File folder = new File(System.getProperty("java.io.tmpdir"), "wicket-test-folder");
		Files.mkdirs(folder);
		assertTrue(folder.isDirectory());
		File file = new File(folder, "child");
		file.createNewFile();
		assertTrue(file.exists());

		assertTrue("Should be able to delete a folder.", Files.removeFolder(folder));
	}

	/**
	 * Tests for {@link Files#removeAsync(java.io.File, IFileCleaner)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void removeAsync() throws Exception
	{

		IFileCleaner fileCleaner = new FileCleaner();

		assertFalse("'null' files are not deleted.", Files.removeAsync(null, fileCleaner));

		File nonExistingFile = new File(
			"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111");
		assertTrue("Even non existing file are scheduled for deletion.",
			Files.removeFolderAsync(nonExistingFile, fileCleaner));
		assertFalse(nonExistingFile.exists());

		java.io.File file = getFile();
		file.createNewFile();
		assertTrue("The just created file should exist!", file.exists());
		assertTrue(file.isFile());

		assertTrue("The file is scheduled for deletion.", Files.removeAsync(file, fileCleaner));

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
		assertFalse("The file no longer exists", exists);
	}

	/**
	 * Tests for {@link Files#removeFolderAsync(java.io.File, IFileCleaner)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void removeFolderAsync() throws Exception
	{
		assertFalse("'null' folders are not deleted.", Files.removeFolderAsync(null, null));

		IFileCleaner fileCleaner = new FileCleaner();

		File nonExistingFolder = new File(
			"/somethingThatDoesntExistsOnMostMachines-111111111111111111111111111111");
		assertTrue("Even non existing folders are scheduled for deletion.",
			Files.removeFolderAsync(nonExistingFolder, fileCleaner));
		assertFalse(nonExistingFolder.exists());

		java.io.File folder = getFolder();
		Files.mkdirs(folder);
		assertTrue(folder.isDirectory());
		File file = new File(folder, "child");
		file.createNewFile();
		assertTrue(file.exists());

		assertTrue("The folder is scheduled for deletion.",
			Files.removeFolderAsync(folder, fileCleaner));

		// remove the reference to the folder to be deleted
		// this way the FileCleaningTracker's ReferenceQueue will mark it as eligible for GC
		folder = null;

		// give chance to the file cleaner to run and delete the folder
		System.gc();
		Thread.sleep(5);
		java.io.File newFolderReference = getFolder();
		assertFalse("The folder still exists", newFolderReference.exists());
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

		assertEquals( java.io.File.separator + "file with whitespace", Files.getLocalFileFromUrl(url).getPath());
	}

	/**
	 * @return a reference to a folder, without creating it !
	 */
	private java.io.File getFolder()
	{
		File folder = new File(System.getProperty("java.io.tmpdir"), "wicket-6.x-test-folder");
		return folder;
	}

	/**
	 * @return a reference to a file, without creating it !
	 * @throws IOException
	 */
	private java.io.File getFile() throws IOException
	{
		return new java.io.File(System.getProperty("java.io.tmpdir"), "wicket-6.x-test-file");
	}

}
