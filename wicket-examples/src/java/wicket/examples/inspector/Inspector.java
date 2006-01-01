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
package wicket.examples.inspector;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.debug.WicketInspectorBug;

/**
 * A simple page with an inspector bug on it.
 * 
 * @author Jonathan Locke
 */
public class Inspector extends WicketExamplePage
{
    /**
     * Constructor
     */
    public Inspector()
    {
        add(new Label("message", "Hello World!"));
        add(new WicketInspectorBug("bug", this));
    }
}