/*
 * $Id$ $Revision:
 * 1.7 $ $Date$
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
package wicket.markup.html.list;

import wicket.markup.html.WebMarkupContainer;

/**
 * Items of the Loop.
 * 
 * @author Jonathan Locke
 */
public class LoopItem extends WebMarkupContainer
{
	/** The index of the LoopItem in the parent Loop */
	private final int index;

	/**
	 * A constructor which uses the index and the list provided to create a
	 * LoopItem. This constructor is the default one.
	 * 
	 * @param index The index of the item
	 */
	protected LoopItem(final int index)
	{
		super(Integer.toString(index), null);
		this.index = index;
	}

	/**
	 * Gets the index of the loopItem in the parent Loop.
	 * 
	 * @return The index of this loopItem in the parent Loop
	 */
	public final int getIndex()
	{
		return index;
	}
}
