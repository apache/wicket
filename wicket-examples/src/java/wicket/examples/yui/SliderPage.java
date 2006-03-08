/*
 * $Id$ $Revision$
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
package wicket.examples.yui;

import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.yui.slider.Slider;
import wicket.model.PropertyModel;

/**
 * Page that displays the calendar component of the Yahoo UI library.
 * 
 * @author Eelco Hillenius
 */
public class SliderPage extends WicketExamplePage
{
	private Integer selection = new Integer(0);

	/**
	 * Construct.
	 */
	public SliderPage()
	{
		add(new Slider("slider", new PropertyModel(this, "selection")));
	}

	/**
	 * Gets selection.
	 * 
	 * @return selection
	 */
	public Integer getSelection()
	{
		return selection;
	}

	/**
	 * Sets selection.
	 * 
	 * @param selection
	 *            selection
	 */
	public void setSelection(Integer selection)
	{
		this.selection = selection;
	}
}
