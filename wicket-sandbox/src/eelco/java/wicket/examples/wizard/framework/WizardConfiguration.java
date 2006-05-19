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

import java.io.Serializable;

/**
 * Configures a wizard.
 * 
 * @author Eelco Hillenius
 */
public class WizardConfiguration implements Serializable
{
	/** the transitions of the wizard. */
	private Transitions transitions;

	/**
	 * Construct.
	 */
	public WizardConfiguration()
	{
	}

	/**
	 * Gets the transitions of the wizard.
	 * 
	 * @return the transitions of the wizard
	 */
	public Transitions getTransitions()
	{
		return transitions;
	}

	/**
	 * Sets the transitions of the wizard.
	 * 
	 * @param transitions
	 *            the transitions of the wizard
	 */
	public void setTransitions(Transitions transitions)
	{
		this.transitions = transitions;
	}

	/**
	 * Start processing the wizard.
	 * 
	 * @return the first step
	 */
	public WizardState begin()
	{
		if (transitions == null)
		{
			throw new IllegalStateException("no transitions configured!");
		}
		WizardState state = newState();
		state.setCurrentNode(transitions.getFirst());
		return state;
	}

	/**
	 * Creates a new wizard state instance.
	 * 
	 * @return a new wizard state instance
	 */
	protected WizardState newState()
	{
		return new WizardState(this);
	}
}