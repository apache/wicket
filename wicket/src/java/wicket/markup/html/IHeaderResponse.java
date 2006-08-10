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
package wicket.markup.html;

import java.io.Serializable;

import wicket.Response;

/**
 * Interface that is used to render header elements (usually javascript and CSS
 * references).
 * 
 * Implementation of this interface is responsible for filtering duplicate
 * contributions (so that for example the same javascript is not loaded twice)
 * during the same request.
 * 
 * @author Matej Knopp
 */
public interface IHeaderResponse extends Serializable
{
	/**
	 * Writes a javascript reference, if the specified reference hasn't been
	 * rendered yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 */
	public void renderJavascriptReference(PackageResourceReference reference);

	/**
	 * Renders javascript code to the response, if the javascript has not
	 * already been rendered.
	 * 
	 * @param javascript
	 *            javacript content to be rendered.
	 * 
	 * @param id
	 *            unique id for the javascript element. This can be null,
	 *            however in that case the ajax header contribution can't detect
	 *            duplicate script fragments.
	 */
	public void renderJavascript(CharSequence javascript, String id);

	/**
	 * Writes a CSS reference, if the specified reference hasn't been rendered
	 * yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the CSS resource
	 */
	public void renderCSSReference(PackageResourceReference reference);

	/**
	 * Renders an arbitrary string to the header. The string is only rendered if
	 * the same string hasn't been rendered before.
	 * <p>
	 * Note: This method is kind of dangerous as users are able to write to the
	 * output whatever they like.
	 * 
	 * @param string
	 *            string to be rendered to head
	 */
	public void renderString(CharSequence string);

	/**
	 * Marks the given object as rendered. The object can be anything (string,
	 * resource reference, etc...). The purpose of this function is to allow
	 * user to manually keep track of rendered items. This can be useful for
	 * items that are expensive to generate (like interpolated text).
	 * 
	 * @param object
	 *            object to be marked as rendered.
	 */
	public void markRendered(Object object);

	/**
	 * Returns whether the given object has been marked as rendered.
	 * <ul>
	 * 	<li>Methods <code>renderJavascriptReference</code> and
	 * 	    <code>renderCSSReference</code> mark the specified
	 * 	    {@link PackageResourceReference} as rendered.
	 *  <li>Method <code>renderJavascript</code> marks List of two elements
	 *      (first is javascript body CharSequence and second is id) as rendered. 
	 * 	<li>Method <code>renderString</code> marks the whole string as rendered.
	 *  <li>Method <code>markRendered</code> can be used to mark an arbitrary object as
	 *      rendered
	 * </ul>
	 * 
	 * @param object
	 *            Object that is queried to be rendered
	 * @return Whether the object has been marked as rendered during the request
	 */
	public boolean wasRendered(Object object);

	/**
	 * Returns the response that can be used to write arbitrary text to the head
	 * section.
	 * <p>
	 * Note: This method is kind of dangerous as users are able to write to the
	 * output whatever they like.
	 * 
	 * @return Reponse
	 */
	public Response getResponse();
}
