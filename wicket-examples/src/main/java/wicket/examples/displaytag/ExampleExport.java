/*
 * $Id: ExampleExport.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) $
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
package wicket.examples.displaytag;

import java.util.List;

import wicket.PageParameters;
import wicket.examples.displaytag.export.CsvView;
import wicket.examples.displaytag.export.ExcelView;
import wicket.examples.displaytag.export.ExportLink;
import wicket.examples.displaytag.export.XmlView;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;

/**
 * How to support exporting table data
 * 
 * @author Juergen Donnerstag
 */
public class ExampleExport extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleExport(final PageParameters parameters)
	{
		// Test data
		final List<ListObject> data = new TestList(6, false);

		// Add the table
		new SimpleListView<ListObject>(this, "rows", data);

		// Add the export links
		new ExportLink(this, "exportCsv", data, new CsvView(data, true, false, false));
		new ExportLink(this, "exportExcel", data, new ExcelView(data, true, false, false));
		new ExportLink(this, "exportXml", data, new XmlView(data, true, false, false));
	}
}