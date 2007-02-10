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
package wicket.extensions.markup.html.repeater.data.grid;

import java.util.List;

import wicket.extensions.markup.html.repeater.data.IDataProvider;

/**
 * Simple concrete implementation of {@link AbstractDataGridView}
 * 
 * <p>
 * Example:
 * 
 * <pre>
 *           &lt;table&gt;
 *             &lt;tr wicket:id=&quot;rows&quot;&gt;
 *               &lt;td wicket:id=&quot;cells&quot;&gt;
 *                 &lt;span wicket:id=&quot;cell&quot;&gt; &lt;/span&gt;
 *               &lt;/td&gt;
 *             &lt;/tr&gt;
 *           &lt;/table&gt;
 * </pre>
 * 
 * <p>
 * Though this example is about a HTML table, DataGridView is not at all limited
 * to HTML tables. Any kind of grid can be rendered using DataGridView.
 * <p>
 * And the related Java code:
 * 
 * <pre>
 * 
 * ICellPopulator[] columns = new ICellPopulator[2];
 * 
 * columns[0] = new PropertyPopulator(&quot;firstName&quot;);
 * columns[1] = new PropertyPopulator(&quot;lastName&quot;);
 * 
 * add(new DataGridView(&quot;rows&quot;, columns, new UserProvider()));
 * 
 * </pre>
 * 
 * @see AbstractDataGridView
 * @see IDataProvider
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class DataGridView extends AbstractDataGridView
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * Notice cells are created in the same order as cell populators in the list
	 * 
	 * @param id
	 *            component id
	 * @param populators
	 *            list of ICellPopulators used to populate cells
	 * @param dataProvider
	 *            data provider
	 */
	public DataGridView(String id, List/* <ICellPopulator> */populators, IDataProvider dataProvider)
	{
		super(id, (ICellPopulator[])populators.toArray(new ICellPopulator[populators.size()]),
				dataProvider);
	}

	/**
	 * Constructor
	 * 
	 * Notice cells are created in the same order as cell populators in the
	 * array
	 * 
	 * @param id
	 *            component id
	 * @param populators
	 *            array of ICellPopulators used to populate cells
	 * @param dataProvider
	 *            data provider
	 */
	public DataGridView(String id, ICellPopulator[] populators, IDataProvider dataProvider)
	{
		super(id, populators, dataProvider);
	}

	/**
	 * Returns the array of cell populators
	 * 
	 * @return the array of cell populators
	 */
	public ICellPopulator[] getPopulators()
	{
		return internalGetPopulators();
	}

	/**
	 * Sets the number of items to be displayed per page
	 * 
	 * @param items
	 *            number of items to display per page
	 * 
	 */
	public void setRowsPerPage(int items)
	{
		internalSetRowsPerPage(items);
	}

	/**
	 * @return number of items displayed per page
	 */
	public int getRowsPerPage()
	{
		return internalGetRowsPerPage();
	}

	/**
	 * Returns the data provider
	 * 
	 * @return data provider
	 */
	public IDataProvider getDataProvider()
	{
		return internalGetDataProvider();
	}


}
