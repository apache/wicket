/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.util.file;

/**
 * File utility methods.
 * 
 * @author Jonathan Locke
 */
public class Files
{
	/** serialVersionUID */
	private static final long serialVersionUID = -1464216059997960924L;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Files()
	{
	}

	/**
	 * Deletes a file, dealing with a particularly nasty bug on Windows.
	 * 
	 * @param file
	 *            File to delete
	 * @return True if file was deleted
	 */
	public static boolean delete(final java.io.File file)
	{
		// Delete current file
		if (!file.delete())
		{
			// NOTE: fix for java/win bug. see:
			// http://forum.java.sun.com/thread.jsp?forum=4&thread=158689&tstart=0&trange=15
			System.gc();
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
			}

			// Try one more time to delete the file
			return file.delete();
		}
		return true;
	}
}
