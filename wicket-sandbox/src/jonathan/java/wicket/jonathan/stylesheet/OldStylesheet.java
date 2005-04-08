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
package wicket.jonathan.stylesheet;

import wicket.markup.html.WebResource;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringBufferResourceStream;

/**
 * A stylesheet resource.
 * 
 * @author Jonathan Locke
 */
public class OldStylesheet extends WebResource
{
	/** Stylesheet information */
	private StringBufferResourceStream resource = new StringBufferResourceStream();
	
	/**
	 * @param s String to append to stylesheet
	 */
	public void append(final String s)
	{
		resource.append(s);
	}
	
	/**
	 * @see WebResource#getResourceStream()
	 */
	public IResourceStream getResourceStream()
	{
		return resource;
	}
}
