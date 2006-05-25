/*
 * $Id$ $Revision$ $Date$
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
package wicket;

import wicket.util.string.AppendingStringBuffer;

/**
 * A response filter can be added to the
 * {@link wicket.settings.IRequestCycleSettings#addResponseFilter(IResponseFilter)}
 * object The will be called from the Buffered Response objects right before
 * they would send it to the real responses. You have to use the
 * {@link wicket.settings.IRequestCycleSettings#setBufferResponse(boolean)}(to
 * true which is the default) for this filtering to work.
 * 
 * @author jcompagner
 * 
 * @see wicket.settings.IRequestCycleSettings#addResponseFilter(IResponseFilter)
 */
public interface IResponseFilter
{
	/**
	 * Filters the response buffer and returns the filtered response that can be
	 * used in the next filter or returned to the real output itself.
	 * 
	 * A filter may alter the response buffer and return the response buffer
	 * itself.
	 * 
	 * @param responseBuffer
	 *            The response buffer to be filtered
	 * @return The changed buffer or the response buffer itself (changed or not)
	 */
	AppendingStringBuffer filter(AppendingStringBuffer responseBuffer);
}