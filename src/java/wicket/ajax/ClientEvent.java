/*
 * $Id: AjaxSelfUpdatingTimerBehavior.java,v 1.2 2006/02/02 11:40:46 jdonnerstag
 * Exp $ $Revision: 5816 $ $Date: 2006-05-23 18:43:32 +0000 (Tue, 23 May 2006) $
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
package wicket.ajax;

/**
 * When using an AjaxEventBehavior, this is used to determine on which client
 * side (javascript) event the behavior should be executed.
 * <p>
 * Please note, that not all events works on all tags in all browsers.
 * <p>
 * Wicket doesn't test if a specific ClientEvent is supported on the tag it's
 * being attached to.
 * 
 * @since 2.0
 * 
 * @author Frank Bille
 */
public enum ClientEvent {
	/**
	 * Matches the onabort event
	 */
	ABORT("onabort"),
	/**
	 * Matches the onblur event
	 */
	BLUR("onblur"),
	/**
	 * Matches the onchange event
	 */
	CHANGE("onchange"),
	/**
	 * Matches the onclick event
	 */
	CLICK("onclick"),
	/**
	 * Matches the ondblclick event
	 */
	DBLCLICK("ondblclick"),
	/**
	 * Matches the onerror event
	 */
	ERROR("onerror"),
	/**
	 * Matches the onfocus event
	 */
	FOCUS("onfocus"),
	/**
	 * Matches the onkeydown event
	 */
	KEYDOWN("onkeydown"),
	/**
	 * Matches the onkeypress event
	 */
	KEYPRESS("onkeypress"),
	/**
	 * Matches the onkeyup event
	 */
	KEYUP("onkeyup"),
	/**
	 * Matches the onload event
	 */
	LOAD("onload"),
	/**
	 * Matches the onmousedown event
	 */
	MOUSEDOWN("onmousedown"),
	/**
	 * Matches the onmousemove event
	 */
	MOUSEMOVE("onmousemove"),
	/**
	 * Matches the onmouseover event
	 */
	MOUSEOVER("onmouseover"),
	/**
	 * Matches the onmouseout event
	 */
	MOUSEOUT("onmouseout"),
	/**
	 * Matches the onmouseup event
	 */
	MOUSEUP("onmouseup"),
	/**
	 * Matches the onreset event
	 */
	RESET("onreset"),
	/**
	 * Matches the onresize event
	 */
	RESIZE("onresize"),
	/**
	 * Matches the onselect event
	 */
	SELECT("onselect"),
	/**
	 * Matches the onsubmit event
	 */
	SUBMIT("onsubmit"),
	/**
	 * Matches the onunload event
	 */
	UNLOAD("onunload"),
	/**
	 * Special type, which is used for putting the action on the href attribute
	 * of a &lt;a&gt; tag.
	 */
	HREF("href");

	private final String event;

	private ClientEvent(String event)
	{
		this.event = event;
	}

	/**
	 * Get the actual event string which can be used to put on the tag.
	 * 
	 * @return the event string, like "onclick" for an {@link ClientEvent#CLICK}
	 */
	public String getEvent()
	{
		return event;
	}
}