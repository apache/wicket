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
package wicket.util.tester;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is experimental only and may not yet work in your environment
 * 
 * @author Ingram Chen
 */
public class ExternalPageViewer
{
	private static final List<String> browserPaths = new ArrayList<String>();

	// TODO Post 1.2: General: We could use JNLP to launch browser (see
	// http://www.croftsoft.com/library/tutorials/browser/), but why not use
	// Swing HTMLArea??
	static
	{
		registerBrowserPath("C:/Program Files/Mozilla Firefox/firefox.exe");
		registerBrowserPath("C:/Program Files/Internet Explorer/iexplore.exe");
	}

	private final WicketTester tester;

	/**
	 * 
	 * @param tester
	 */
	public ExternalPageViewer(final WicketTester tester)
	{
		this.tester = tester;
	}

	/**
	 * register addtional browser path for viewInBrowser()
	 * 
	 * @param path
	 */
	public static final void registerBrowserPath(String path)
	{
		browserPaths.add(path);
	}

	/**
	 * open a web browser and see lastet rendered WebPage.
	 */
	public final void viewInBrowser()
	{
		int webRootPathIndex = getThisClassFileURL().getPath().indexOf("/WEB-INF");

		// obtain webRootPath, for example:
		// D:/eclipse/workspace/MyProject/MyWebRoot
		String webRootPath = getThisClassFileURL().getPath().substring(0, webRootPathIndex);

		File temp = new File(webRootPath + "/" + getTemperaryDumpHtmlFileName());
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(temp);
			out.write(tester.getServletResponse().getDocument().getBytes(getHtmlEncoding()));
			out.flush();
		}
		catch (UnsupportedEncodingException e)
		{
			throw convertoUnexpect(e);
		}
		catch (IOException e)
		{
			throw convertoUnexpect(e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					throw convertoUnexpect(e);
				}
			}
		}

		if (!new File(getBrowserPath()).exists())
		{
			throw new IllegalStateException("No browser found at " + getBrowserPath());
		}
		// try {
		throw new RuntimeException("Not yet supported");
		// new ProcessBuilder(getBrowserPath(),
		// temp.toURL().toString()).start();
		// } catch (MalformedURLException e) {
		// throw convertoUnexpect(e);
		// } catch (IOException e) {
		// throw convertoUnexpect(e);
		// }
	}

	/**
	 * 
	 * @return path
	 */
	private String getBrowserPath()
	{
		Iterator<String> iter = browserPaths.iterator();
		while (iter.hasNext())
		{
			String path = iter.next();
			if (new File(path).exists())
			{
				return path;
			}
		}
		throw new IllegalStateException("No browser found, please add definition");
	}

	/**
	 * define a temperary file name that stores source of last rendered page.
	 * This file is used by external browser
	 * 
	 * @return String
	 */
	protected String getTemperaryDumpHtmlFileName()
	{
		// this pattern will hide from eclipe and ignore by cvs
		return ".del-wicketTestDump.html";
	}

	/**
	 * set default encoding for writing temperary file.
	 * 
	 * @return String
	 */
	protected String getHtmlEncoding()
	{
		return "UTF-8";
	}

	/**
	 * 
	 * @return URL
	 */
	private URL getThisClassFileURL()
	{
		URL url = this.getClass().getClassLoader().getResource(
				this.getClass().getName().replace('.', '/') + ".class");
		return url;
	}

	/**
	 * 
	 * @param e
	 * @return RuntimeException
	 */
	private RuntimeException convertoUnexpect(Exception e)
	{
		return new RuntimeException("tester: unexpect", e);
	}
}
