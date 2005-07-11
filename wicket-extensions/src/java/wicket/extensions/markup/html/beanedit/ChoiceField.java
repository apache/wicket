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
package wicket.extensions.markup.html.beanedit;

import java.util.Collection;
import java.util.List;

/**
 * Field for rendering a drop down choice.
 *
 * @author Eelco Hillenius
 */
public class ChoiceField extends AbstractBeanField
{
	/**
	 * The choices.
	 */
	private final Collection choices;

	/**
	 * Construct.
	 * @param name name of the field
	 * @param displayName display name
	 * @param choices the choices
	 */
	public ChoiceField(String name, String displayName, Collection choices)
	{
		super(name, displayName);
		this.choices = choices;
	}

	/**
	 * Construct.
	 * @param name name of the field
	 * @param displayName display name
	 * @param choices the choices
	 */
	public ChoiceField(String name, String displayName, List choices)
	{
		super(name, displayName);
		this.choices = choices;
	}

	/**
	 * Gets the choices.
	 * @return choices
	 */
	public final Collection getChoices()
	{
		return choices;
	}
}
