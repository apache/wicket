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
package wicket.spring.common;


/**
 * Encapsulates the Query Paramaters to be passed to daos
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class QueryParam {
	private int first;

	private int count;

	private String sort;

	private boolean sortAsc;

	/**
	 * Set to return <tt>count</tt> elements, starting at the <tt>first</tt>
	 * element.
	 * 
	 * @param first
	 *            First element to return.
	 * @param count
	 *            Number of elements to return.
	 */
	public QueryParam(int first, int count) {
		this(first, count, null, true);
	}

	/**
	 * Set to return <tt>count</tt> sorted elements, starting at the
	 * <tt>first</tt> element.
	 * 
	 * @param first
	 *            First element to return.
	 * @param count
	 *            Number of elements to return.
	 * @param sort
	 *            Column to sort on.
	 * @param sortAsc
	 *            Sort ascending or descending.
	 */
	public QueryParam(int first, int count, String sort, boolean sortAsc) {
		this.first = first;
		this.count = count;
		this.sort = sort;
		this.sortAsc = sortAsc;
	}

	public int getCount() {
		return count;
	}

	public int getFirst() {
		return first;
	}

	public String getSort() {
		return sort;
	}

	public boolean isSortAsc() {
		return sortAsc;
	}

	public boolean hasSort() {
		return sort != null;
	}

}
