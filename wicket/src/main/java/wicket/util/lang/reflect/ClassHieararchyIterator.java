/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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
package wicket.util.lang.reflect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/**
 * Iterator used to iterate over class hieararchies
 * 
 * @author ivaynberg
 */
public class ClassHieararchyIterator implements Iterator<Class>
{
	/** list of classes that define the iteration in specified order */
	ArrayList<Class> hierarchy = new ArrayList<Class>();

	/** current position of the iterator */
	private int index = 0;

	/**
	 * Construct.
	 * 
	 * @param clazz
	 *            class whose hierarchy will be iterated
	 * @param scanOrder
	 *            direction of iteration
	 */
	public ClassHieararchyIterator(Class clazz, ClassOrder scanOrder)
	{
		// build the hierarchy iteration
		Class cursor = clazz;
		while (cursor != null)
		{
			hierarchy.add(cursor);
			cursor = cursor.getSuperclass();
		}
		if (scanOrder == ClassOrder.SUPER_TO_SUB)
		{
			Collections.reverse(hierarchy);
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return index < hierarchy.size();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public Class next()
	{
		return hierarchy.get(index++);
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("remove() is not supported by "
				+ getClass().getName());
	}
}