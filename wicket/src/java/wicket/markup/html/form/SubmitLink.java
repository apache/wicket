/*
 * $Id: SubmitLink.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) $
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

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.html.link.ILinkListener;
import wicket.model.IModel;

/**
 * A link which can be used exactly like a Button to submit a Form. The href of
 * the link will use JavaScript to submit the form.
 * 
 * <p>
 * You can use this class 2 ways. First with the constructor without a Form
 * object then this Link must be inside a Form so that it knows what form to
 * submit to. Second way is to use the Form constructor then that form will be
 * used to submit to.
 * </p>
 * <p>
 * 
 * <pre>
 *                   Form f = new Form(&quot;linkForm&quot;, new CompoundPropertyModel(mod));
 *                   f.add(new TextField(&quot;value1&quot;));
 *                   f.add(new SubmitLink(&quot;link1&quot;) {
 *                       protected void onSubmit() {
 *                           System.out.println(&quot;Link1 was clicked, value1 is: &quot;
 *                                   + mod.getValue1());
 *                       };
 *                   });
 *                   add(new SubmitLink(&quot;link2&quot;,f) {
 *                       protected void onSubmit() {
 *                           System.out.println(&quot;Link2 was clicked, value1 is: &quot;
 *                                   + mod.getValue1());
 *                       };
 *                   });
 *            
 *                &lt;form wicket:id=&quot;linkForm&quot; &gt;
 *                   &lt;input wicket:id=&quot;value1&quot; type=&quot;text&quot; size=&quot;30&quot;/&gt;
 *                   &lt;a wicket:id=&quot;link1&quot;&gt;Press link1 to submit&lt;/a&gt;
 *                   &lt;input type=&quot;submit&quot; value=&quot;Send&quot;/&gt;
 *               &lt;/form&gt;
 *                 &lt;a wicket:id=&quot;link2&quot;&gt;Press link 2 to submit&lt;/a&gt;
 *               
 * </pre>
 * 
 * </p>
 * <p>
 * If this link is not placed in a form or given a form to cooperate with, it
 * will fall back to a normal link behavior, meaning that {@link #onSubmit()}
 * will be called without any other consequences.
 * </p>
 * 
 * @param <T>
 *            The type
 * 
 * @author chris
 * @author jcompagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public abstract class SubmitLink<T> extends Button<T> implements ILinkListener
{
	private static final long serialVersionUID = 1L;

	private Form form;

	/**
	 * With this constructor the SubmitLink must be inside a Form.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            The id of the submitlink.
	 */
	public SubmitLink(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * With this constructor the SubmitLink must be inside a Form.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            The id of the submitlink.
	 * @param model
	 *            The model for this submitlink, It won't be used by the submit
	 *            link itself, but it can be used for keeping state
	 */
	public SubmitLink(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}


	/**
	 * With this constructor the SubmitLink will submit the {@link Form} that is
	 * given when the link is clicked on.
	 * 
	 * The SubmitLink doesn't have to be in inside the {@link Form}. But
	 * currently if it is outside the {@link Form} and the SubmitLink will be
	 * rendered first. Then the {@link Form} will have a generated
	 * javascript/css id. The markup javascript/css id that can exist will be
	 * overridden.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            The id of the submitlink.
	 * @param form
	 *            The form which this submitlink must submit.
	 */
	public SubmitLink(MarkupContainer parent, String id, Form form)
	{
		super(parent, id);
		this.form = form;
	}

	/**
	 * With this constructor the SubmitLink will submit the {@link Form} that is
	 * given when the link is clicked on.
	 * 
	 * The SubmitLink doesn't have to be in inside the {@link Form}. But
	 * currently if it is outside the {@link Form} and the SubmitLink will be
	 * rendered first. Then the {@link Form} will have a generated
	 * javascript/css id. The markup javascript/css id that can exist will be
	 * overridden.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            The id of the submitlink.
	 * @param model
	 *            The model for this submitlink, It won't be used by the submit
	 *            link itself, but it can be used for keeping state
	 * @param form
	 *            The form which this submitlink must submit.
	 */
	public SubmitLink(MarkupContainer parent, String id, IModel model, Form form)
	{
		super(parent, id);
		this.form = form;
	}

	/**
	 * @inheritDoc
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		// If we're disabled
		if (!isEnabled())
		{
			// if the tag is an anchor proper
			if (tag.getName().equalsIgnoreCase("a"))
			{
				// Change anchor link to span tag
				tag.setName("span");

				// Remove any href from the old link
				tag.remove("href");
			}
			else
			{
				// Remove any onclick design time code
				tag.remove("onclick");
			}
		}
		else
		{
			if (tag.getName().equalsIgnoreCase("a"))
			{
				tag.put("href", "#");
			}
			tag.put("onclick", getTriggerJavaScript());
		}
	}

	/**
	 * The javascript which trigges this link
	 * 
	 * @return The javascript
	 */
	private String getTriggerJavaScript()
	{
		Form form = getForm();
		if (form != null)
		{
			StringBuffer sb = new StringBuffer(100);
			sb.append("var e=document.getElementById('");
			sb.append(form.getHiddenFieldId(Form.HIDDEN_FIELD_FAKE_SUBMIT));
			sb.append("'); e.name=\'");
			sb.append(getInputName());
			sb.append("'; e.value='x';");
			sb.append("var f=document.getElementById('");
			sb.append(form.getMarkupId());
			sb.append("');");
			if (shouldInvokeJavascriptFormOnsubmit())
			{
				sb.append("if (f.onsubmit != undefined) { if (f.onsubmit()==false) return false; }");
			}
			sb.append("f.submit();return false;");
			return sb.toString();
		}
		else
		{
			// if we didn't find a parent form, fall back on normal link
			// behavior
			return "location.href='" + urlFor(ILinkListener.INTERFACE).toString() + "';";
		}
	}

	/**
	 * @return the Form for which this submit link submits. Null if there is no
	 *         form.
	 */
	@Override
	public final Form getForm()
	{
		if (form == null)
		{
			// Look for parent form
			form = (Form)findParent(Form.class);
		}
		return form;
	}

	/**
	 * Controls whether or not clicking on this link will invoke form's
	 * javascript onsubmit handler. True by default.
	 * 
	 * @return true if form's javascript onsubmit handler should be invoked,
	 *         false otherwise
	 */
	protected boolean shouldInvokeJavascriptFormOnsubmit()
	{
		return true;
	}
}