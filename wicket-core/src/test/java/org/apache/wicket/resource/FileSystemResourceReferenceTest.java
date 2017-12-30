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
package org.apache.wicket.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for FileSystemResourceReference
 * 
 * @author Tobias Soloschenko
 *
 */
public class FileSystemResourceReferenceTest extends WicketTestCase
{

	/**
	 * Test ZIP files
	 * 
	 * @throws IOException
	 *             if the ZIP file or the file content can't be read
	 * @throws URISyntaxException
	 *             if the URI is not readable
	 */
	@Test
	public void testFileSystemResourceReferenceWithZip() throws IOException, URISyntaxException
	{
		InputStream inputStream = null;
		try
		{
			URL resource = FileSystemResourceReferenceTest.class.getResource("FileSystemResourceReferenceTest.zip");
			Path path = FileSystemResourceReference.getPath(URI.create("jar:" + resource.toURI() +
				"!/folderInZip/FileSystemResourceReference.txt"));
			final FileSystemResource fileSystemResource = new FileSystemResource(path);
			FileSystemResourceReference fileSystemResourceReference = new FileSystemResourceReference(
				"test", path)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected FileSystemResource getFileSystemResource()
				{
					return fileSystemResource;
				}
			};
			// Size
			Assert.assertEquals(fileSystemResource.getSize(), 39);

			// Content
			inputStream = fileSystemResource.getInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream);
			Assert.assertEquals("FileSystemResourceReference.zip content", outputStream.toString());
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Test normal files
	 * 
	 * @throws IOException
	 *             if the file can't be read
	 * @throws URISyntaxException
	 *             if the URI is not readable
	 */
	@Test
	public void testFileSystemResourceReferenceWithNormalFile() throws IOException,
		URISyntaxException
	{
		InputStream inputStream = null;
		try
		{
			URL resource = FileSystemResourceReferenceTest.class.getResource("FileSystemResourceReference.txt");
			Path path = FileSystemResourceReference.getPath(resource.toURI());
			final FileSystemResource fileSystemResource = new FileSystemResource(path);
			FileSystemResourceReference fileSystemResourceReference = new FileSystemResourceReference(
				"test", path)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected FileSystemResource getFileSystemResource()
				{
					return fileSystemResource;
				}
			};
			// Size
			Assert.assertEquals(fileSystemResource.getSize(), 54);

			// Content
			inputStream = fileSystemResource.getInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream);
			Assert.assertEquals("FileSystemResourceReference.zip content in normal file",
				outputStream.toString());
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Test mime types
	 * 
	 * @throws IOException
	 *             if the file or the mime type can't be read
	 * @throws URISyntaxException
	 *             if the URI is not readable
	 */
	@Test
	public void testMimeTypeEqual() throws IOException, URISyntaxException
	{
		URL resource = FileSystemResourceReferenceTest.class.getResource("FileSystemResourceReference.txt");
		Path path = FileSystemResourceReference.getPath(resource.toURI());
		final FileSystemResource fileSystemResource = new FileSystemResource(path)
		{

			private static final long serialVersionUID = 1L;

			protected String getMimeType() throws IOException
			{
				return "test/mime1";
			}
		};
		FileSystemResourceReference fileSystemResourceReference = new FileSystemResourceReference(
			"test", path)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected FileSystemResource getFileSystemResource()
			{
				return fileSystemResource;
			}
		};
		Assert.assertEquals("test/mime1", fileSystemResource.getMimeType());
	}

	/**
	 * Test mime type detection with java.nio.file.spi.FileTypeDetector
	 * 
	 * @throws IOException
	 *             if the file can't be read
	 * @throws URISyntaxException
	 *             if the
	 */
	@Test
	public void testMimeTypeDetection() throws IOException, URISyntaxException
	{
		// uke > unknown extension :-)
		URL resource = FileSystemResourceReferenceTest.class.getResource("FileSystemResourceReference.uke");
		Path path = FileSystemResourceReference.getPath(resource.toURI());

		final FileSystemResource fileSystemResource = new FileSystemResource(path);
		FileSystemResourceReference fileSystemResourceReference = new FileSystemResourceReference(
			"test", path)
		{
			private static final long serialVersionUID = 1L;

			protected FileSystemResource getFileSystemResource()
			{
				return fileSystemResource;

			}
		};
		Assert.assertEquals("text/plain_provided_by_detector", fileSystemResource.getMimeType());

		final FileSystemResource fileSystemResourceMime = new FileSystemResource(path)
		{
			private static final long serialVersionUID = 1L;

			protected String getMimeType() throws IOException
			{
				return "text/plain";
			}
		};
		FileSystemResourceReference fileSystemResourceReferenceOverriddenMime = new FileSystemResourceReference(
			"test", path)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected FileSystemResource getFileSystemResource()
			{
				return fileSystemResourceMime;
			}
		};
		Assert.assertEquals("text/plain", fileSystemResourceMime.getMimeType());
	}

	/**
	 * Test serialization of {@link FileSystemResource}
	 */
	@Test
	public void testSerialization() throws IOException, URISyntaxException
	{
		InputStream inputStream = null;
		try
		{
			URL resource = FileSystemResourceReferenceTest.class.getResource("FileSystemResourceReference.txt");
			Path path = FileSystemResourceReference.getPath(resource.toURI());
			final FileSystemResource fileSystemResource = new FileSystemResource(path);
			final FileSystemResource cloned = WicketObjects.cloneObject(fileSystemResource);

			Assert.assertEquals(cloned.getSize(), 54);

			// Content
			inputStream = cloned.getInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream);
			Assert.assertEquals("FileSystemResourceReference.zip content in normal file",
					outputStream.toString());
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}

}
