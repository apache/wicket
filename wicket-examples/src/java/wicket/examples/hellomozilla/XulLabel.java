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
package wicket.examples.hellomozilla;

import java.io.Serializable;

import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.HtmlContainer;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Simple XUL label component.
 *
 * @author Eelco Hillenius
 */
public class XulLabel extends HtmlContainer
{
	/**
	 * Construct.
	 * @param name name of the component
	 * @param object subject of ognlExpression
	 * @param ognlExpression ognl expression that works on object
	 */
	public XulLabel(String name, Serializable object, String ognlExpression)
	{
		super(name);
		// create a replacement body that will get the result of the ognlExpression
		// applied to the object
		IModel replacementModel = new PropertyModel(new Model(object), ognlExpression);
		// replace the value attribute with the result of the replacement model
		add(new ComponentTagAttributeModifier("value", replacementModel));
	}
}
