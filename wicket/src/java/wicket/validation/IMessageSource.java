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
package wicket.validation;

import java.util.Map;

/**
 * Represents a message store that stores messages by a <code>key</code>.
 * This store is usually localized.
 * 
 * @author ivaynberg
 */
public interface IMessageSource
{
	/**
	 * Retrieves a message with the given <code>key</code>. Performs variable
	 * substitution for variables defined in
	 * <code>params:varname->varvalue</code> map using the
	 * <code>${varname}</code> syntax to identify variables in the message.
	 * 
	 * @param key
	 *            message key
	 * @param params
	 *            variable substitution map:varname->varvalue
	 * @return message or null if not found
	 */
	String getMessage(String key, Map<String, Object> params);
}
