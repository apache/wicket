/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision$ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
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
package wicket.performance;

import java.io.File;
import java.util.Random;

import wicket.MarkupContainer;
import wicket.markup.IMarkupCacheKeyProvider;
import wicket.markup.IMarkupResourceStreamProvider;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.util.resource.FileResourceStream;
import wicket.util.resource.IResourceStream;

/**
 * 
 * @author Juergen Donnerstag
 */
public class DummyContainer extends WebMarkupContainer
		implements
			IMarkupResourceStreamProvider,
			IMarkupCacheKeyProvider
{
	private static final long serialVersionUID = 1L;

	private File markupFile;

	private final Random random = new Random();

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param markupFile
	 */
	public DummyContainer(final MarkupContainer parent)
	{
		super(parent, "dummy");
	}

	/**
	 * 
	 * @param file
	 */
	public void setMarkupFile(final File file)
	{
		this.markupFile = file;
	}

	/**
	 * 
	 * @see wicket.Component#loadMarkupStream()
	 */
	@Override
	protected MarkupStream loadMarkupStream()
	{
		// Actually this is like never loading the markup as this.markupFile
		// will allways be null when called by Component.<init>.
		if (this.markupFile != null)
		{
			return super.loadMarkupStream();
		}
		
		return null;
	}

	/**
	 * 
	 * @see wicket.markup.IMarkupResourceStreamProvider#getMarkupResourceStream(wicket.MarkupContainer,
	 *      java.lang.Class)
	 */
	public IResourceStream getMarkupResourceStream(final MarkupContainer container,
			final Class<? extends MarkupContainer> containerClass)
	{
		if (container.getClass() == containerClass)
		{
			return new FileResourceStream(new wicket.util.file.File(this.markupFile));
		}

		return null;
	}

	/**
	 * 
	 * @see wicket.markup.IMarkupCacheKeyProvider#getCacheKey(wicket.MarkupContainer,
	 *      java.lang.Class)
	 */
	public CharSequence getCacheKey(final MarkupContainer container,
			final Class<? extends MarkupContainer> containerClass)
	{
		// Disable caching
		return null;

		// Enable caching
		// return this.markupFile.getPath();

		// Some requests are cached, others are not
		// Note: The range must be significantly larger (2-10) than the number
		// of files which are scanned. The larger, the less likely it is that
		// a file is found in the cache
		// return String.valueOf(random.nextInt(1000));
	}
}
