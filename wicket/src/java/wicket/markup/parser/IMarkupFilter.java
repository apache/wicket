/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.parser;

import java.text.ParseException;

import wicket.markup.MarkupElement;


/**
 * Wicket uses a streaming XML parser to read the markup. A chain of
 * IMarkupFilters is used e.g. to remove comments, allow for html typical markup
 * (some tags don't need to be closed explicitly), etc..
 * <p>
 * The streaming XML parser must implement IMarkupFilter itself and thus is
 * ususally the first element of the chain.
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupFilter
{
	/**
	 * Gets the next tag from the input string.
	 * 
	 * @return The extracted tag.
	 * @throws ParseException
	 */
	public abstract MarkupElement nextTag() throws ParseException;

	/**
	 * IMarkupFilters are usually chained with the last filter being a XML
	 * parser. getParent() returns the next filter in the chain.
	 * 
	 * @return the next filter in the chain, or null if the last one.
	 */
	public abstract IMarkupFilter getParent();
}