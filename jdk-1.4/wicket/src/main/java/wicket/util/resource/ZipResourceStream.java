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
package wicket.util.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketRuntimeException;
import wicket.util.file.File;
import wicket.util.time.Time;

/**
 * An IResourceStream that ZIPs a directory's contents on the fly
 * 
 * <p>
 * <b>NOTE 1.</b> Nested directories are not supported yet, and a
 * {@link FileNotFoundException} will be thrown in that case.
 * </p>
 * 
 * <p>
 * <b>NOTE 2.</b> As a future improvement, cache a map of generated ZIP files
 * for every directory and use a Watcher to detect modifications in this
 * directory. Using ehcache would be good for that, but it's not in Wicket
 * dependencies yet. <b>No caching of the generated ZIP files is done yet.</b>
 * </p>
 * 
 * <p>
 * <b>NOTE 3.</b> As a future improvement, implement getLastModified() and
 * request ResourceStreamRequestTarget to generate Last-Modified and Expires
 * HTTP headers. <b>No HTTP cache headers are provided yet</b>. See WICKET-385
 * </p>
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class ZipResourceStream extends AbstractResourceStream
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(ZipResourceStream.class);

	ByteArrayOutputStream bytearray;

	/**
	 * Construct.
	 * 
	 * @param dir
	 *            The directory where to look for files. The directory itself
	 *            will not be included in the ZIP.
	 */
	public ZipResourceStream(File dir)
	{
		bytearray = new ByteArrayOutputStream();
		try
		{
			int BUFFER = 2048;
			BufferedInputStream origin = null;
			ZipOutputStream out = new ZipOutputStream(bytearray);
			byte data[] = new byte[BUFFER];
			// get a list of files from current directory
			String files[] = dir.list();

			if (files == null)
				throw new IllegalArgumentException("Not a directory: " + dir);

			for (int i = 0; i < files.length; i++)
			{
				log.debug("Adding: " + files[i]);
				FileInputStream fi = new FileInputStream(new File(dir, files[i]));
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(files[i]);
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1)
				{
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	public void close() throws IOException
	{
	}

	/**
	 * @see wicket.util.resource.IResourceStream#getContentType()
	 */
	public String getContentType()
	{
		return null;
	}

	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return new ByteArrayInputStream(bytearray.toByteArray());
	}

	public long length()
	{
		return bytearray.size();
	}

	public Time lastModifiedTime()
	{
		return null;
	}
}
