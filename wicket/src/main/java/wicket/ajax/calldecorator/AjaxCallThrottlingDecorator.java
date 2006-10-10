/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.ajax.calldecorator;

import wicket.ajax.AbstractDefaultAjaxBehavior;
import wicket.ajax.IAjaxCallDecorator;
import wicket.util.string.Strings;
import wicket.util.time.Duration;

/**
 * Adds throttling to the ajax call. Throttled behaviors only execute once
 * within the given delay even though they can be triggered multiple times.
 * <p>
 * For example, this is useful when attaching an event behavior to the
 * onkeypress event. It is not desirable to have an ajax call made every time
 * the user types so we can throttle that call to a desirable delay, such as
 * once per second. This gives us a near real time ability to provide feedback
 * without overloading the server.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public final class AjaxCallThrottlingDecorator extends AjaxPostprocessingCallDecorator
{
	private static final long serialVersionUID = 1L;

	private final Duration duration;
	private final String id;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            throttle id
	 * @param delay
	 *            throttle delay
	 */
	public AjaxCallThrottlingDecorator(String id, Duration delay)
	{
		this(null, id, delay);
	}

	/**
	 * Construct.
	 * 
	 * @param decorator
	 *            wrapped decorator
	 * @param id
	 *            throttle id
	 * @param delay
	 *            throttle delay
	 */
	public AjaxCallThrottlingDecorator(IAjaxCallDecorator decorator, String id, Duration delay)
	{
		super(decorator);
		if (Strings.isEmpty(id))
		{
			throw new IllegalArgumentException("id cannot be an empty string");
		}
		this.id = id;
		this.duration = delay;
	}


	/**
	 * @see wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator#postDecorateScript(CharSequence)
	 */
	@Override
	public final CharSequence postDecorateScript(CharSequence script)
	{
		return AbstractDefaultAjaxBehavior.throttleScript(script, id, duration);
	}

}
