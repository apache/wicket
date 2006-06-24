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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.Component;
import wicket.model.Model;

/**
 * TODO docme
 * 
 * @author Igor Vaynberg
 */
public abstract class HeaderlessColumn extends AbstractColumn implements IColumn
{
	/**
	 * Construct.
	 */
	public HeaderlessColumn()
	{
		super(new Model("&nbsp;"));
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.table.AbstractColumn#getHeader(java.lang.String)
	 */
	public Component getHeader(String componentId)
	{
		Component header = super.getHeader(componentId);
		return header.setEscapeModelStrings(false);
	}
}
