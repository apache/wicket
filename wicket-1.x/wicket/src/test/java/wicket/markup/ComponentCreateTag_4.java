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
package wicket.markup;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;


/**
 * Mock page for testing.
 *
 * @author Chris Turner
 */
public class ComponentCreateTag_4 extends WebPage 
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construct.
	 */
	public ComponentCreateTag_4() 
	{
	    // <wicket:component> are treated as anonymous because they not
	    // accessible nor available right now. Thus you do not add
	    // the label contained in <wicket:component> to that specific
	    // component. You rather add it to its parent, which in this
	    // case is the page.
	    add(new Label("txt1", "Demo 1"));
	    add(new Label("txt2", "Demo 2"));
    }
}
