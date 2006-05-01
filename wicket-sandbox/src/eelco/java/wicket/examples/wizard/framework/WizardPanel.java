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

import wicket.AttributeModifier;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;
import wicket.util.lang.Objects;
import wicket.version.undo.Change;

/**
 * The main wizard panel.
 * 
 * @author Eelco Hillenius
 */
public class WizardPanel extends Panel
{
	/** state of the wizard. */
	final WizardState state;

	/**
	 * @param id
	 *            component id
	 * @param configuration
	 *            wizard configuration object
	 */
	public WizardPanel(final String id, WizardConfiguration configuration)
	{
		super(id);

		if (configuration == null)
		{
			throw new NullPointerException("configuration must not be null");
		}

		this.state = configuration.begin();

		WizardForm form = new WizardForm("form");
		add(form);
	}

	/**
	 * Gets the wizard state object.
	 * 
	 * @return the wizard state object
	 */
	protected final WizardState getState()
	{
		return state;
	}

	/**
	 * Gets the editor for the given node.
	 * 
	 * @param editorId
	 *            the id that must be used to create the editor
	 * @return the editor panel
	 */
	protected Panel newEditor(String editorId)
	{
		Node node = state.getCurrentNode();
		if (node != null)
		{
			Panel editor = node.newEditor(editorId);
			if (editor != null)
			{
				return editor;
			}
		}

		return new EmptyPanel(editorId);
	}

	/**
	 * Form for wizard node.
	 */
	private final class WizardForm extends Form
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public WizardForm(String id)
		{
			super(id);
			Panel editor = newEditor("editor");
			add(editor);

			Button previousButton = new Button("previous")
			{
				protected void onSubmit()
				{
					Node current = state.getCurrentNode();
					if (current instanceof Step)
					{
						record(current);
						TransitionLabel result = ((Step)current).previous(WizardForm.this);
						state.move(result);
						WizardForm.this.replace(newEditor("editor"));
					}
				}

				public boolean isVisible()
				{
					Node current = state.getCurrentNode();
					Transitions transitions = state.getTransitions();
					return transitions.exists(current, TransitionLabel.PREVIOUS);
				}
			};
			add(previousButton);

			Button nextButton = new Button("next")
			{
				protected void onSubmit()
				{
					Node current = state.getCurrentNode();
					if (current instanceof Step)
					{
						record(current);
						TransitionLabel result = ((Step)current).next(WizardForm.this);
						state.move(result);
						WizardForm.this.replace(newEditor("editor"));
					}
				}

				public boolean isVisible()
				{
					Node current = state.getCurrentNode();
					Transitions transitions = state.getTransitions();
					return transitions.exists(current, TransitionLabel.NEXT);
				}
			};
			add(nextButton);

			Button exitButton = new Button("exit")
			{
				protected void onSubmit()
				{
					Node current = state.getCurrentNode();
					if (current instanceof Exit)
					{
						record(current);
						TransitionLabel result = ((Step)current).next(WizardForm.this);
						((Exit)current).exit(getRequestCycle());
					}
				}

				public boolean isVisible()
				{
					return (state.getCurrentNode() instanceof Exit);
				}
			};
			exitButton.add(new AttributeModifier("value", new Model()
			{
				public Object getObject(wicket.Component component)
				{
					Node current = state.getCurrentNode();
					if (current instanceof Exit)
					{
						return ((Exit)current).getLabel();
					}
					return null;
				}
			}));
			add(exitButton);
		}
	}

	/**
	 * Record current state.
	 * 
	 * @param currentStep
	 *            step to record for undoing
	 */
	protected void record(final Node currentStep)
	{
		addStateChange(new Change()
		{
			Node keep = (Node)Objects.cloneObject(currentStep);

			public void undo()
			{
				state.setCurrentNode(keep);
			}
		});
	}

	/**
	 * An empty do-nothing panel.
	 */
	private final class EmptyPanel extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public EmptyPanel(String id)
		{
			super(id);
		}
	}
}
