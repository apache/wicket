/*
 * $Id$
 * $Revision$
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

import wicket.AttributeModifier;

/**
 * An interface to be implemented by components which are able to add
 * information to the body tag associated with the markup.
 * <p>
 * &lt;wicket:head&gt; and panels are able to contribute &lt;body
 * onLoad="xxx"&gt; attributes to the page. In order to support that feature, a
 * BodyOnLoadContainer is automatically created and associated with the body tag
 * of the page (in case of an exception, on the exception page you can see a
 * _body component being a child of the Page). That allways happens because
 * wicket does not know upfront if there will be a panel contributing to the
 * body or not (That will only change in wicket 2). This BodyOnLoadContainer
 * however is transparent, it delegates all events to its parent (the page) and
 * is not directly accessible by the user. However in order to allow the user to
 * attach an AttributeModifier to a body tag, the page might implement the
 * IBodyTagContributor interface.
 * 
 * @author Scott Sauyet
 */
public interface IBodyTagContributor
{
	/**
	 * Fetch a list of {@link wicket.AttributeModifier AttributeModifiers} for
	 * the body tag.
	 * 
	 * @return all AttributeModifiers which contribute to the &lt;BODY&gt; tag.
	 */
	AttributeModifier[] getBodyAttributeModifiers();
}
