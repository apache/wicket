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
package org.apache.wicket.core.util.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class PathTest extends WicketTestCase
{

	private static void assertContents(String expectedContents, IResourceStream rs)
		throws ResourceStreamNotFoundException, IOException
	{
		InputStream in = rs.getInputStream();
		try
		{
			final byte[] expectedBytes = expectedContents.getBytes(Charset.defaultCharset());
			final int expectedLength = expectedBytes.length;
			byte[] buf = new byte[expectedLength * 2];
			int read = in.read(buf, 0, buf.length);
			assertEquals(expectedLength, read, "contents do not match");
			byte[] buf2 = new byte[expectedLength];
			System.arraycopy(buf, 0, buf2, 0, expectedLength);
			assertTrue(Arrays.equals(expectedBytes, buf2), "contents do not match");
		}
		finally
		{
			in.close();
		}
	}

	private static File createTempFile(String contents) throws IOException
	{
		FileOutputStream out = null;
		try
		{
			File tmp = File.createTempFile("temp", "temp");
			tmp.deleteOnExit();
			out = new FileOutputStream(tmp);
			out.write(contents.getBytes(Charset.defaultCharset()));
			return tmp;
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	private static File findRoot(File file)
	{
		final File parent = file.getParentFile();
		if (parent == null)
		{
			return file;
		}
		else
		{
			return findRoot(parent);
		}
	}

	@Test
	void loadFromRootUsingSubpathInFilename() throws Exception
	{
		final String contents = PathTest.class.getName() + ": loaded from root";
		final File file = createTempFile(contents);
		final File root = findRoot(file);
		final Path path = new Path(root.getCanonicalPath());
		String relative = root.toURI().relativize(file.toURI()).getPath();
		IResourceStream rs = path.find(PathTest.class, relative);
		assertNotNull(rs);
		assertContents(contents, rs);
	}

	@Test
	void loadFilenameFromPath() throws Exception
	{
		final String contents = PathTest.class.getName() + ": loaded from prefix";
		final File file = createTempFile(contents);
		final File parent = file.getParentFile();
		final Path path = new Path(parent.getCanonicalPath());
		IResourceStream rs = path.find(PathTest.class, file.getName());
		assertNotNull(rs);
		assertContents(contents, rs);
	}
}
