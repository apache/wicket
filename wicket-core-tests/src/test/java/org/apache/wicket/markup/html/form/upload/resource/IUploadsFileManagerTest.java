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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.util.file.File;
import org.junit.jupiter.api.Test;

class IUploadsFileManagerTest
{
	@Test
	void storeOfAManagerWrittenForSaveSavesTheFileUnderItsClientFileName()
	{
		List<String> saved = new ArrayList<>();
		IUploadsFileManager manager = new IUploadsFileManager()
		{
			@SuppressWarnings("removal")
			@Override
			public void save(FileUpload fileItem, String uploadFieldId)
			{
				saved.add(uploadFieldId + '/' + fileItem.getClientFileName());
			}

			@Override
			public File getFile(String uploadFieldId, String fileName)
			{
				return null;
			}
		};

		assertEquals("file.txt", manager.store(newFileUpload("file.txt"), "field"));
		assertEquals(List.of("field/file.txt"), saved);
	}

	@SuppressWarnings("removal")
	@Test
	void saveOfAManagerWrittenForStoreSaysToCallStore()
	{
		IUploadsFileManager manager = new IUploadsFileManager()
		{
			@Override
			public String store(FileUpload fileItem, String uploadFieldId)
			{
				return "stored-name";
			}

			@Override
			public File getFile(String uploadFieldId, String fileName)
			{
				return null;
			}
		};

		UnsupportedOperationException exception = assertThrows(
			UnsupportedOperationException.class,
			() -> manager.save(newFileUpload("file.txt"), "field"));
		assertTrue(exception.getMessage().contains("store(FileUpload, String)"));
	}

	@Test
	void storeOfAManagerImplementingNeitherMethodFailsSayingWhichToImplement()
	{
		IUploadsFileManager manager = (uploadFieldId, fileName) -> null;

		UnsupportedOperationException exception = assertThrows(
			UnsupportedOperationException.class,
			() -> manager.store(newFileUpload("file.txt"), "field"));
		assertTrue(exception.getMessage().contains("store(FileUpload, String)"));
	}

	private static FileUpload newFileUpload(String clientFileName)
	{
		FileUpload fileUpload = mock(FileUpload.class);
		when(fileUpload.getClientFileName()).thenReturn(clientFileName);
		return fileUpload;
	}
}
