/*
 * $Id: IStringResourceStream.java 3585 2006-01-02 07:37:31 +0000 (Mon, 02 Jan
 * 2006) jonathanlocke $ $Revision$ $Date: 2006-01-02 07:37:31 +0000
 * (Mon, 02 Jan 2006) $
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
package wicket.util.resource;

import java.nio.charset.Charset;

/**
 * A resource that can be converted to a String representation, possibly using
 * an explicit Charset.
 * 
 * @author Jonathan Locke
 */
public interface IStringResourceStream extends IResourceStream
{
	/**
	 * Sets the character set used for converting this resource to a String.
	 * 
	 * @param charset
	 *            Charset for component
	 */
	void setCharset(final Charset charset);

	/**
	 * @return This resource as a String.
	 */
	String asString();
}
