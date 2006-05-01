/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.wizard.framework;

import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;

/**
 * Empty stub for steps.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractStep implements Step
{
	/**
	 * Construct.
	 */
	public AbstractStep()
	{
	}

	/**
	 * @see wicket.examples.wizard.framework.Step#newEditor(java.lang.String)
	 */
	public Panel newEditor(String editorId)
	{
		return null;
	}

	/**
	 * @see wicket.examples.wizard.framework.Step#next(wicket.markup.html.form.Form)
	 */
	public TransitionLabel next(Form form)
	{
		return null;
	}

	/**
	 * @see wicket.examples.wizard.framework.Step#previous(wicket.markup.html.form.Form)
	 */
	public TransitionLabel previous(Form form)
	{
		return null;
	}
}
