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
package wicket.markup.html.form;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.CompoundPropertyModel;

/**
 * A link which can be used exactly like a Button to submit a Form. 
 * The href of the link will use JavaScript to submit the form. 
 * 
 * <p>You can use this class 2 ways. First with the constructor without a Form object
 * then this Link must be inside a Form so that it knows what form to submit to.
 * Second way is to use the Form constructor then that form will be used to submit to.</p>
 * 
 * <pre>
        Form f = new Form("linkForm", new CompoundPropertyModel(mod));
        f.add(new TextField("value1"));
        f.add(new SubmitLink("link1") {
            protected void onSubmit() {
                System.out.println("Link1 was clicked, value1 is: "
                        + mod.getValue1());
            };
        });
        add(new SubmitLink("link2",f) {
            protected void onSubmit() {
                System.out.println("Link2 was clicked, value1 is: "
                        + mod.getValue1());
            };
        });

    <form wicket:id="linkForm" >
        <input wicket:id="value1" type="text" size="30"/>
        <a wicket:id="link1">Press link1 to submit</a>
        <input type="submit" value="Send"/>
    </form>
      <a wicket:id="link2">Press link 2 to submit</a>
    
 * </pre>
 * @author chris
 * @author jcompagner
 */
public class SubmitLink extends Button
{
	private static final long serialVersionUID = 1L;
	
	private Form form;

	/**
     * With this constructor the SubmitLink must be inside a Form.
     * 
     * @param id The id of the submitlink.
     */
    public SubmitLink(String id){
        super(id);
    }
    

	/**
     * With this constructor the SubmitLink will submit the {@link Form} that 
     * is given when the link is clicked on.  
     * 
     * The SubmitLink doesn't have to be in inside the {@link Form}.
     * But currently if it is outside the {@link Form} and the SubmitLink will 
     * be rendered first. Then the {@link Form} will have a generated javascript/css id.
     * The markup javascript/css id that can exist will be overridden. 
     * 
     * @param id The id of the submitlink.
	 * @param form The form which this submitlink must submit.
     */
    public SubmitLink(String id, Form form){
        super(id);
        this.form = form;
    }

    protected void onComponentTag(ComponentTag tag)
    {
        checkComponentTag(tag, "a");
        tag.put("href","#");
        tag.put("onclick",getTriggerJavaScript());
    }
    
    /**
     * The javascript which trigges this link
     * @return The javascript
     */
    private String getTriggerJavaScript()
    {
		Form form = getSubmitLinkForm();
		StringBuffer sb = new StringBuffer(100);
		sb.append("javascript:");
		sb.append("document.getElementById('");
		sb.append(form.getHiddenFieldId());
		sb.append("').name=\'");
		sb.append(getInputName());
		sb.append("';");
		sb.append("document.getElementById('");
		sb.append(form.getJavascriptId());
		sb.append("').submit();");
		return sb.toString();
    }


	/**
	 * @return the Form for which this submit link submits
	 */
	public final Form getSubmitLinkForm()
	{
		if(form == null)
		{
			form = getForm();
		}
		return form;
	}
}

