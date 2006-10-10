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
package wicket.markup;

import wicket.MarkupContainer;

/**
 * In order to create Wicket Component you need to provide a parent (container).
 * The only exception are pages. In same rare cases e.g. bordered pages, it is
 * useful if the parent (the page in that case) were able get the real container
 * (the border) and the new component to this container instead. The idea is to
 * be able to create a component in Page exactly the same way no matter whether
 * the page, or a base page, adds a border to create a common page layout or
 * not. Example:
 * <p>
 * How you usually add a component
 * 
 * <pre>
 * new Label(this, &quot;myLabel&quot;, &quot;test&quot;);
 * </pre>
 * 
 * With the interface you would need to add component to a bordered page like
 * 
 * <pre>
 * new Label(getMyBorder(), &quot;myLabel&quot;, &quot;test&quot;);
 * </pre>
 * 
 * provided getMyBorder() return the border component which has been added to
 * the page previously.
 * 
 * @author Juergen
 */
public interface IAlternateParentProvider
{
	/**
	 * Get the altnernate component parent
	 * 
	 * @param childClass
	 *            the Class of the new child to be added
	 * @param childId
	 *            the id of the component to be added
	 * @return The alternate parent container
	 */
	MarkupContainer getAlternateParent(final Class childClass, final String childId);
}
