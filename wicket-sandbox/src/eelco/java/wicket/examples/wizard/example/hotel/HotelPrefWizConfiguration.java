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
package wicket.examples.wizard.example.hotel;

import wicket.examples.wizard.framework.Node;
import wicket.examples.wizard.framework.Step;
import wicket.examples.wizard.framework.Transition;
import wicket.examples.wizard.framework.TransitionLabel;
import wicket.examples.wizard.framework.Transitions;
import wicket.examples.wizard.framework.WizardConfiguration;

/**
 * Configuration object for the preferences wizard.
 * 
 * @author Eelco Hillenius
 */
public final class HotelPrefWizConfiguration extends WizardConfiguration
{
	/**
	 * Construct.
	 */
	public HotelPrefWizConfiguration()
	{
		super();

		HotelPreferences preferences = new HotelPreferences();
		HotelPreferencesModel model = new HotelPreferencesModel(preferences);

		Step personalData = new PersonalDataStep(model);
		Step roomSelection = new RoomSelectionStep(model);
		Step stayPreferences = new StayPreferencesStep(model);
		Step review = new ReviewStep(model);

		Transitions transitions = new Transitions(personalData);
		transitions.put(personalData, new NextCommand(TransitionLabel.NEXT, roomSelection));
		transitions.put(roomSelection, new NextCommand(TransitionLabel.PREVIOUS, personalData));
		transitions.put(roomSelection, new NextCommand(TransitionLabel.NEXT, stayPreferences));
		transitions.put(stayPreferences, new NextCommand(TransitionLabel.PREVIOUS, roomSelection));
		transitions.put(stayPreferences, new NextCommand(TransitionLabel.NEXT, roomSelection));
		// transitions.put(step3, new End("next"));

		setTransitions(transitions);
	}

	/**
	 * Command for navigating to the next step.
	 */
	private final class NextCommand extends Transition
	{
		private final Node next;

		/**
		 * Construct.
		 * 
		 * @param label
		 * @param next
		 */
		public NextCommand(TransitionLabel label, Node next)
		{
			super(label);
			this.next = next;
		}

		/**
		 * @see wicket.examples.wizard.framework.Transition#next(wicket.examples.wizard.framework.Step)
		 */
		public Node next(Node current)
		{
			return next;
		}
	}
}
