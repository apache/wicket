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
package org.apache.wicket.markup.html.form.upload.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.util.file.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FolderUploadsFileManagerTest
{
	@TempDir
	Path tempDir;

	@Test
	void getFileRejectsTraversalInUploadFieldId()
	{
		FolderUploadsFileManager manager = new FolderUploadsFileManager(new File(tempDir.toFile()));

		assertThrows(SecurityException.class, () -> manager.getFile("../escaped", "safe.txt"));
	}

	@Test
	void getFileRejectsTraversalInClientFileName()
	{
		FolderUploadsFileManager manager = new FolderUploadsFileManager(new File(tempDir.toFile()));

		assertThrows(SecurityException.class, () -> manager.getFile("uploadField", "../../etc/passwd"));
	}

	@Test
	void saveRejectsTraversalInUploadFieldId() throws IOException
	{
		FolderUploadsFileManager manager = new FolderUploadsFileManager(new File(tempDir.toFile()));
		FileUpload fileUpload = mock(FileUpload.class);
		when(fileUpload.getClientFileName()).thenReturn("safe.txt");
		when(fileUpload.getInputStream())
			.thenReturn(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

		assertThrows(SecurityException.class, () -> manager.save(fileUpload, "../escaped"));
	}

	@Test
	void saveRejectsTraversalInClientFileName() throws IOException
	{
		FolderUploadsFileManager manager = new FolderUploadsFileManager(new File(tempDir.toFile()));
		FileUpload fileUpload = mock(FileUpload.class);
		when(fileUpload.getClientFileName()).thenReturn("../../etc/passwd");
		when(fileUpload.getInputStream())
			.thenReturn(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

		assertThrows(SecurityException.class, () -> manager.save(fileUpload, "uploadField"));
	}

	@Test
	void saveSucceedsWithValidUploadFieldIdAndClientFileName() throws IOException
	{
		FolderUploadsFileManager manager = new FolderUploadsFileManager(new File(tempDir.toFile()));
		FileUpload fileUpload = mock(FileUpload.class);
		when(fileUpload.getClientFileName()).thenReturn("safe.txt");
		when(fileUpload.getInputStream())
			.thenReturn(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

		manager.save(fileUpload, "uploadField");

		Path savedFile = tempDir.resolve("uploadField").resolve("safe.txt");
		assertTrue(Files.exists(savedFile));
		assertEquals("content", Files.readString(savedFile, StandardCharsets.UTF_8));
	}
}
