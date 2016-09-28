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
package org.apache.wicket.util.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

public class PathTest extends WicketTestCase
{

	@Test
	public void loadFromRootUsingSubpathInFilename() throws Exception
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

	public static void assertContents(String expectedContents, IResourceStream rs)
		throws ResourceStreamNotFoundException, IOException
	{
		InputStream in = rs.getInputStream();
		try
		{
			final byte[] expectedBytes = expectedContents.getBytes(Charset.defaultCharset());
			final int expectedLength = expectedBytes.length;
			byte[] buf = new byte[expectedLength * 2];
			int read = in.read(buf, 0, buf.length);
			assertEquals("contents do not match", expectedLength, read);
			byte[] buf2 = new byte[expectedLength];
			System.arraycopy(buf, 0, buf2, 0, expectedLength);
			assertTrue("contents do not match", Arrays.equals(expectedBytes, buf2));
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void loadFilenameFromPath() throws Exception
	{
		final String contents = PathTest.class.getName() + ": loaded from prefix";
		final File file = createTempFile(contents);
		final File parent = file.getParentFile();
		final Path path = new Path(parent.getCanonicalPath());
		IResourceStream rs = path.find(PathTest.class, file.getName());
		assertNotNull(rs);
		assertContents(contents, rs);
	}

	public static File createTempFile(String contents) throws IOException
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

	public static File findRoot(File file)
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
}
