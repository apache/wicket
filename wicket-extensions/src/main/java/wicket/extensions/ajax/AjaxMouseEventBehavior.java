/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.ajax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.Request;
import wicket.RequestCycle;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;

/**
 * An event behavior which gives detailed information about the client side
 * event properties on the server.
 * <p>
 * Example:
 * 
 * <pre>
 *  WebMarkupContainer boxArea = new WebMarkupContainer(this, &quot;boxArea&quot;);
 *  boxArea.add(new AjaxMouseEventBehavior(MouseEventType.CLICK)
 *  {
 *     protected void onEvent(AjaxRequestTarget target, MouseEvent event)
 *     {
 *        if (event.isShiftPressed())
 *        {
 *           ...
 *        }
 *     }
 *  });
 * </pre>
 * 
 * PLEASE NOTE! Be careful when using {@link MouseEventType#MOUSEMOVE} because
 * it will generate *a lot* of AJAX requests.
 * 
 * @author frankbille
 */
public abstract class AjaxMouseEventBehavior extends AjaxEventBehavior
{
	/**
	 * A subset of ClientEvent with only the event types that is supported by
	 * this behavior.
	 */
	public static enum MouseEventType {
		/**
		 * Matches the onclick event
		 */
		CLICK,
		/**
		 * Matches the ondblclick event
		 */
		DBLCLICK,
		/**
		 * Matches the onmouseover event
		 */
		MOUSEOVER,
		/**
		 * Matches the onmouseout event
		 */
		MOUSEOUT,
		/**
		 * Matches the onmousedown event
		 */
		MOUSEDOWN,
		/**
		 * Matches the onmouseup event
		 */
		MOUSEUP,
		/**
		 * Matches the onmousemove event
		 */
		MOUSEMOVE;

		/**
		 * @return The ClientEvent matching the MouseEventType
		 */
		public ClientEvent getClientEvent()
		{
			return ClientEvent.valueOf(this.name());
		}
	}

	/**
	 * Client side event properties which gives client specific information
	 * about a given mouse event.
	 */
	public class MouseEvent
	{
		private boolean shiftKeyPressed;
		private boolean altKeyPressed;
		private boolean ctrlKeyPressed;
		private int xPos;
		private int yPos;
		private int button;

		private MouseEvent(boolean shiftKey, boolean altKey, boolean ctrlKey, int xPos, int yPos,
				int button)
		{
			this.shiftKeyPressed = shiftKey;
			this.altKeyPressed = altKey;
			this.ctrlKeyPressed = ctrlKey;
			this.xPos = xPos;
			this.yPos = yPos;
			this.button = button;
		}

		/**
		 * @return Was the ALT key pressed while the event was executed.
		 */
		public boolean isAltKeyPressed()
		{
			return altKeyPressed;
		}

		/**
		 * @return The mouse button which was click on when the event was fired.
		 *         This only works for {@link MouseEventType#MOUSEDOWN} and
		 *         {@link MouseEventType#MOUSEUP} events.
		 *         <P>
		 *         PLEASE NOTE! Right now it's the browser that defines which
		 *         button is clicked. It is therefore not consistent which
		 *         button number means what physical button was actually
		 *         pressed.
		 */
		public int getButton()
		{
			return button;
		}

		/**
		 * @return Was the CTRL key pressed while the event was executed.
		 */
		public boolean isCtrlKeyPressed()
		{
			return ctrlKeyPressed;
		}

		/**
		 * @return Was the SHIFT key pressed while the event was executed.
		 */
		public boolean isShiftKeyPressed()
		{
			return shiftKeyPressed;
		}

		/**
		 * @return The X position of the cursor on the page when the event was
		 *         fired.
		 */
		public int getXPos()
		{
			return xPos;
		}

		/**
		 * @return The Y position of the cursor on the page when the event was
		 *         fired.
		 */
		public int getYPos()
		{
			return yPos;
		}

	}

	private static final Pattern AJAX_URL_PATTERN = Pattern.compile("^wicketAjaxGet\\('([^']+)'$");

	/**
	 * Construct.
	 * 
	 * @param eventType
	 *            The mouse event type that we should listen on.
	 */
	public AjaxMouseEventBehavior(MouseEventType eventType)
	{
		super(eventType.getClientEvent());
	}

	protected CharSequence getCallbackScript(CharSequence partialCall,
			CharSequence onSuccessScript, CharSequence onFailureScript)
	{
		String superCallbackScript = super.getCallbackScript(partialCall, onSuccessScript,
				onFailureScript).toString();

		Matcher mat = AJAX_URL_PATTERN.matcher(partialCall);

		if (mat.matches())
		{
			StringBuffer url = new StringBuffer(mat.group(1));

			// CtrlClick
			url.append("&");
			url.append("shiftKeyPressed='+event.shiftKey+'");
			url.append("&");
			url.append("altKeyPressed='+event.altKey+'");
			url.append("&");
			url.append("ctrlKeyPressed='+event.ctrlKey+'");
			url.append("&");
			url.append("xPos='+event.clientX+'");
			url.append("&");
			url.append("yPos='+event.clientY+'");
			url.append("&");
			url.append("button='+event.button+'");

			superCallbackScript = superCallbackScript.replace(mat.group(1), url.toString());
		}

		return superCallbackScript;
	}

	@Override
	protected final void onEvent(AjaxRequestTarget target)
	{
		Request request = RequestCycle.get().getRequest();

		boolean shiftKey = Boolean.parseBoolean(request.getParameter("shiftKeyPressed"));
		boolean altKey = Boolean.parseBoolean(request.getParameter("altKeyPressed"));
		boolean ctrlKey = Boolean.parseBoolean(request.getParameter("ctrlKeyPressed"));
		int xPos = Integer.parseInt(request.getParameter("xPos"));
		int yPos = Integer.parseInt(request.getParameter("yPos"));
		int button = Integer.parseInt(request.getParameter("button"));

		onEvent(target, new MouseEvent(shiftKey, altKey, ctrlKey, xPos, yPos, button));
	}

	/**
	 * Listener for ajax events with detailed supprt for the properties of the
	 * event.
	 * 
	 * @param target
	 * @param event
	 */
	protected abstract void onEvent(AjaxRequestTarget target, MouseEvent event);

}
