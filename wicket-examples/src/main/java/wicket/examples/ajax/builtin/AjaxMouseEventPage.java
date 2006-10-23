/*
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
package wicket.examples.ajax.builtin;

import wicket.ajax.AjaxRequestTarget;
import wicket.extensions.ajax.AjaxMouseEventBehavior;
import wicket.extensions.ajax.AjaxMouseEventBehavior.MouseEventType;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.MultiLineLabel;

/**
 * Page that demonstrates the use of {@link AjaxMouseEventBehavior} which has
 * support for getting the event properties for mouse based AJAX events on the
 * server. This is values such as X and Y position and which button has been
 * pressed.
 * 
 * @author frankbille
 */
public class AjaxMouseEventPage extends BasePage
{
	/**
	 * Constructor
	 */
	public AjaxMouseEventPage()
	{
		new WebComponent(this, "output").setOutputMarkupId(true);

		addEventType(MouseEventType.CLICK);

		addEventType(MouseEventType.MOUSEOVER);

		addEventType(MouseEventType.MOUSEOUT);

		addEventType(MouseEventType.MOUSEDOWN);

		addEventType(MouseEventType.MOUSEUP);

		addEventType(MouseEventType.MOUSEMOVE);
	}

	private void addEventType(final MouseEventType eventType)
	{
		WebMarkupContainer box = new WebMarkupContainer(this, eventType.name().toLowerCase());
		box.add(new AjaxMouseEventBehavior(eventType)
		{
			@Override
			protected void onEvent(AjaxRequestTarget target, MouseEvent event)
			{
				StringBuffer output = new StringBuffer();

				output.append("Event type: on").append(eventType.name().toLowerCase()).append("\n");
				output.append("Mouse button: ").append(event.getButton()).append("\n");
				output.append("X position: ").append(event.getXPos()).append("\n");
				output.append("Y position: ").append(event.getYPos()).append("\n");
				output.append("Shift key pressed? ").append(event.isShiftKeyPressed()).append("\n");
				output.append("Alt key pressed? ").append(event.isAltKeyPressed()).append("\n");
				output.append("Ctrl key pressed? ").append(event.isCtrlKeyPressed()).append("\n");

				MultiLineLabel label = new MultiLineLabel(AjaxMouseEventPage.this, "output", output
						.toString());
				label.setOutputMarkupId(true);
				target.addComponent(label);
			}
		});
	}
}
