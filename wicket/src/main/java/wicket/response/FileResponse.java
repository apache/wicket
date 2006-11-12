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
package wicket.response;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.util.file.File;

/**
 * A Response implementation that writes to a file.
 * 
 * @author Jonathan Locke
 */
public final class FileResponse extends Response
{
	/** The file to write to */
	private final File file;

	/** Output stream to write to */
	private OutputStream out;

	/** Output PrintWriter to write to */
	private PrintWriter printWriter;

	/**
	 * Constructor
	 * 
	 * @param file
	 *            The file to write to
	 */
	public FileResponse(final File file)
	{
		this.file = file;
	}

	/**
	 * @see wicket.Response#close()
	 */
	@Override
	public void close()
	{
		if (printWriter != null)
		{
			printWriter.close();
			printWriter = null;
		}
		if (out != null)
		{
			try
			{
				out.close();
				out = null;
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException("Unable to close to file " + file, e);
			}
		}
	}

	/**
	 * @see wicket.Response#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream()
	{
		try
		{
			if (out == null)
			{
				out = new FileOutputStream(file);
			}
			return out;
		}
		catch (FileNotFoundException e)
		{
			throw new WicketRuntimeException("Unable to get output stream to file " + file, e);
		}
	}

	/**
	 * @see wicket.Response#write(CharSequence)
	 */
	@Override
	public void write(final CharSequence string)
	{
		try
		{
			getPrintWriter().print(string);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to write to file " + file, e);
		}
	}

	private PrintWriter getPrintWriter() throws IOException
	{
		if (printWriter == null)
		{
			this.printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		}
		return printWriter;
	}
}
