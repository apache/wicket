/*
 * $Id$
 * $Revision$
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
package wicket.spring.common.web;

import java.util.Arrays;

import wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import wicket.extensions.markup.html.repeater.data.table.IColumn;
import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import wicket.model.Model;

/**
 * Base class for the contact display page.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class ContactsDisplayPage extends BasePage {
	public ContactsDisplayPage() {
		IColumn[] cols = new IColumn[4];
		cols[0] = new PropertyColumn(new Model<String>("first name"), "firstName",
				"firstName");
		cols[1] = new PropertyColumn(new Model<String>("last name"), "lastName",
				"lastName");
		cols[2] = new PropertyColumn(new Model<String>("home phone"), "homePhone");
		cols[3] = new PropertyColumn(new Model<String>("cell phone"), "cellPhone");

		new DefaultDataTable(this, "contacts", Arrays.asList(cols), getDataProvider(), 5);
	}

	protected abstract SortableDataProvider getDataProvider();
}
