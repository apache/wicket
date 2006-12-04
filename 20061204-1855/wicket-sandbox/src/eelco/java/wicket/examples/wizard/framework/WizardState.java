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
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of the wizard state.
 * 
 * @author Eelco Hillenius
 */
public class WizardState implements Serializable
{
	/**
	 * The wizard configuration.
	 */
	private final WizardConfiguration configuration;

	/**
	 * The steps allready taken.
	 */
	private List/* <Step> */history = new ArrayList();

	/**
	 * The current node.
	 */
	private Node currentNode;

	/**
	 * Construct.
	 * 
	 * @param configuration
	 *            The wizard configuration object
	 */
	public WizardState(WizardConfiguration configuration)
	{
		this.configuration = configuration;
	}

	/**
	 * Gets the current step.
	 * 
	 * @return current step
	 */
	public Node getCurrentNode()
	{
		return currentNode;
	}

	/**
	 * Sets the current step.
	 * 
	 * @param currentStep
	 *            current step
	 */
	public void setCurrentNode(Node currentStep)
	{
		this.currentNode = currentStep;
	}

	/**
	 * Moves the current step to the one according to the configuration.
	 * 
	 * @param label
	 *            the transition label
	 */
	public void move(TransitionLabel label)
	{
		if (label != TransitionLabel.CURRENT)
		{
			Transitions transitions = configuration.getTransitions();
			Transition next = transitions.get(getCurrentNode(), label);
			this.currentNode = next.next(currentNode);
		}
	}

	/**
	 * Gets the transitions.
	 * 
	 * @return the transitions.
	 */
	public final Transitions getTransitions()
	{
		return configuration.getTransitions();
	}
}