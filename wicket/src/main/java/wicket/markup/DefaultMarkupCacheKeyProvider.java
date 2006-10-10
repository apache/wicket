/*
 * $Id: MarkupCache.java 4639 2006-02-26 01:44:07 -0800 (Sun, 26 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-26 01:44:07 -0800 (Sun, 26 Feb
 * 2006) $
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
package wicket.markup;

import java.util.Locale;

import wicket.MarkupContainer;
import wicket.util.string.AppendingStringBuffer;

/**
 * Wicket default implementation for the cache key used to reference the cached
 * markup resource stream.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class DefaultMarkupCacheKeyProvider implements IMarkupCacheKeyProvider
{
	/**
	 * Constructor.
	 */
	public DefaultMarkupCacheKeyProvider()
	{
	}

	/**
	 * Construct a proper key value for the cache
	 * 
	 * @param container
	 *            The container requesting the markup
	 * @param clazz
	 *            The clazz to get the key for
	 * @return Key that uniquely identifies any markup that might be associated
	 *         with this markup container.
	 */
	public CharSequence getCacheKey(final MarkupContainer container,
			final Class<? extends MarkupContainer> clazz)
	{
		final String classname = clazz.getName();
		final Locale locale = container.getLocale();
		final String style = container.getStyle();
		final String markupType = container.getMarkupType();

		final AppendingStringBuffer buffer = new AppendingStringBuffer(classname.length() + 32);
		buffer.append(classname);

		if (locale != null)
		{
			// TODO What is wrong with locale.toString()?!?
			final boolean l = locale.getLanguage().length() != 0;
			final boolean c = locale.getCountry().length() != 0;
			final boolean v = locale.getVariant().length() != 0;
			buffer.append(locale.getLanguage());
			if (c || (l && v))
			{
				buffer.append('_').append(locale.getCountry()); // This may just
				// append '_'
			}
			if (v && (l || c))
			{
				buffer.append('_').append(locale.getVariant());
			}
		}
		if (style != null)
		{
			buffer.append(style);
		}

		buffer.append(markupType);
		return buffer;
	}
}
