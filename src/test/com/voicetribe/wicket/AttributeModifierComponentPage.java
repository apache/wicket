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
package com.voicetribe.wicket;

import com.voicetribe.wicket.markup.ComponentTagAttributeModifier;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;

/**
 * Test page used for checking the attribute modification
 * functionality of Component.
 *
 * @see AttributeModifierComponentTest
 * @author Chris Turner
 */
public class AttributeModifierComponentPage extends HtmlPage {

	/** Serial Version ID */
	private static final long serialVersionUID = 1L;

	public AttributeModifierComponentPage(final PageParameters parameters) {
        // Label with attribute modifier
        Label label1 = new Label("label1", new Model("Label 1"));
        add(label1);

        // Lavel with override attribute modifier
        Label label2 = new Label("label2", new Model("Label 2"));
        label2.addAttributeModifier(
            new ComponentTagAttributeModifier("class", new Model("overrideLabel")));
        label2.addAttributeModifier(
            new ComponentTagAttributeModifier("unknown", new Model("invalid")));
        add(label2);

        // Lavel with attribute inserter
        Label label3 = new Label("label3", new Model("Label 3"));
        label3.addAttributeModifier(
            new ComponentTagAttributeModifier("class", true, new IDetachableModel() {
                private String text = null;
                
                public void detach(RequestCycle cycle) {
                    System.out.println("ComponentTagAttributeModifier model detached");
                    text = null;
                }

                public void attach(RequestCycle cycle) {
                    System.out.println("ComponentTagAttributeModifier model attached");
                    text = "insertLabel";
                }

                public Object getObject() {
                    return text;
                }

                public void setObject(Object object) {
                    text = object.toString();
                }
            }));
        add(label3);
    }

}
