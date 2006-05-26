/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.link;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import wicket.IRequestTarget;
import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.util.io.Streams;
import wicket.util.string.Strings;

/**
 * A link that streams a file to the client. When clicked this link will prompt
 * the save as dialog in the browser.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class DownloadLink extends Link
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * File to stream
	 */
	private final File file;

	/**
	 * File name to stream
	 */
	private final String fileName;


	/**
	 * Constructor. File name used will be the result of
	 * <code>file.getName()</code>
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param file
	 *            file to stream to client
	 */
	public DownloadLink(MarkupContainer parent, String id, File file)
	{
		super(parent, id);
		if (file == null)
		{
			throw new IllegalArgumentException("file cannot be null");
		}
		this.file = file;
		this.fileName = file.getName();
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param file
	 *            file to stream to client
	 * @param fileName
	 *            name of the file
	 */
	public DownloadLink(MarkupContainer parent, String id, File file, String fileName)
	{
		super(parent, id);
		if (file == null)
		{
			throw new IllegalArgumentException("file cannot be null");
		}
		if (Strings.isEmpty(fileName))
		{
			throw new IllegalArgumentException("fileName cannot be an empty string");
		}
		this.file = file;
		this.fileName = fileName;
	}


	/**
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		getRequestCycle().setRequestTarget(new IRequestTarget()
		{

			public void detach(RequestCycle requestCycle)
			{
			}

			public Object getLock(RequestCycle requestCycle)
			{
				return null;
			}

			public void respond(RequestCycle requestCycle)
			{
				WebResponse r = (WebResponse)requestCycle.getResponse();
				r.setAttachmentHeader(fileName);

				try
				{
					InputStream is = new FileInputStream(file);
					try
					{
						Streams.copy(is, r.getOutputStream());
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
					finally
					{
						try
						{
							is.close();
						}
						catch (IOException e)
						{
							throw new RuntimeException(e);
						}
					}
				}
				catch (FileNotFoundException e)
				{
					throw new RuntimeException(e);
				}
			}
		});
	}

}
