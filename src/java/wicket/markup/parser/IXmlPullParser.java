/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.parser;

import java.io.IOException;

import wicket.util.resource.IResource;
import wicket.util.resource.ResourceNotFoundException;

/**
 * The interface of a streaming XML parser as required by Wicket.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public interface IXmlPullParser extends IMarkupFilter
{
	/**
	 * Return the encoding applied while reading the markup resource. The
	 * encoding is determined by analysing the &lt;?xml version=".."
	 * encoding=".." ?&gt; tag.
	 * 
	 * @return if null, JVM defaults have been used.
	 */
	public abstract String getEncoding();

	/**
	 * Wicket dissects the markup into Wicket relevant tags and raw markup,
	 * which is not further analysed by Wicket. getInputFromPositionMarker() is
	 * used to access the raw markup.
	 * 
	 * @param toPos
	 *            To position
	 * @return The raw markup in between the position marker and toPos
	 */
	public abstract CharSequence getInputFromPositionMarker(int toPos);

	/**
	 * Wicket dissects the markup into Wicket relevant tags and raw markup,
	 * which is not further analysed by Wicket. getInputSubsequence() is used to
	 * access the raw markup.
	 * 
	 * @param fromPos
	 *            From position
	 * @param toPos
	 *            To position
	 * @return The raw markup in between fromPos and toPos
	 */
	public abstract CharSequence getInputSubsequence(final int fromPos, final int toPos);

	/**
	 * Parse the markup provided. Use nextTag() to access the tags contained one
	 * after another.
	 * <p>
	 * Note: xml character encoding is NOT applied. It is assumed the input
	 * provided does have the correct encoding already.
	 * 
	 * @param string
	 *            The markup to be parsed
	 * @throws IOException
	 *             Error while reading the resource
	 * @throws ResourceNotFoundException
	 *             Resource not found
	 */
	public abstract void parse(final CharSequence string) throws IOException,
		ResourceNotFoundException;

	/**
	 * Reads and parses markup from a resource like file. Use nextTag() to
	 * access the tags contained, one after another.
	 * 
	 * @param resource
	 *            A resource like e.g. a file
	 * @throws IOException
	 *             Error while reading the resource
	 * @throws ResourceNotFoundException
	 *             Resource not found
	 */
	public abstract void parse(final IResource resource) throws IOException,
			ResourceNotFoundException;

	/**
	 * Set the position marker of the markup at the current position.
	 */
	public abstract void setPositionMarker();

	/**
	 * Set whether to strip components.
	 * 
	 * @param stripComments
	 *            if true, comments will be stripped
	 */
	public abstract void setStripComments(boolean stripComments);
}