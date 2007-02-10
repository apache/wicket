/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.displaytag.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import wicket.util.string.AppendingStringBuffer;

/**
 * Just a utility class for testing out the table and column tags. This List
 * fills itself with objects and sorts them as though it where pulling data from
 * a report. This list is used to show the various report oriented examples
 * (such as grouping, callbacks, and data exports).
 * 
 * @author epesh
 * @version $Revision $ ($Author $)
 */
public class ReportList extends ArrayList implements Serializable
{
	/**
	 * Creats a TestList that is filled with 20 ReportableListObject suitable
	 * for testing.
	 */
	public ReportList()
	{
		super();

		for (int j = 0; j < 20; j++)
		{
			add(new ReportableListObject());
		}

		Collections.sort(this);
	}

	/**
	 * Creates a TestList that is filled with [size] ReportableListObject
	 * suitable for testing.
	 * 
	 * @param size
	 *            int
	 */
	public ReportList(int size)
	{
		super();

		for (int j = 0; j < size; j++)
		{
			add(new ReportableListObject());
		}

		Collections.sort(this);
	}

	/**
	 * @return String
	 */
	public String toString()
	{
		AppendingStringBuffer buf = new AppendingStringBuffer(200);
		for (int j = 0; j < this.size(); j++)
		{
			buf.append("" + j + ": " + get(j) + "\n");
		}
		return buf.toString();
	}

}
