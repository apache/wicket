/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.request;

import wicket.IRequestTarget;
import wicket.RequestCycle;

/**
 * <p>
 * The request cycle processor is responsible for handling the steps of a
 * request cycle. It's methods are called in a pre-defined order:
 * <ul>
 * <li> {@link #resolve(RequestCycle, RequestParameters)} is called to get the
 * request target. A request might refer to e.g. a bookmarkable page, a listener
 * interface call on a component on a previously rendered page, a shared
 * resource or e.g. a non-wicket resource that resides in the web application
 * folder. </li>
 * <li> {@link #processEvents(RequestCycle)} is called after the target is
 * resolved. It is meant to handle/ distribute events like e.g. listener
 * interface calls on components. During this processing, the request target may
 * be changed (e.g. by calling setResponsePage). What actually happens is that
 * {@link wicket.RequestCycle} holds a stack of targets, of which it will take
 * to last addition as the recent one, but walks the whole stack in order to do
 * cleaning up after the request is handled.</li>
 * <li> {@link #respond(RequestCycle)} is called to create a response to the
 * requesting client. Typically, the actual response handling is to be (or
 * delegated) by the request target implementation, but different strategies
 * might do as they seem fit. </li>
 * <li> {@link #respond(RuntimeException, RequestCycle)} is called whenever an uncaught
 * exception occurs during the event handling or response phase so that an
 * appropriate exception response can be generated. This method is guaranteed to
 * be called whenever such an exception happens, but will never be called
 * otherwise. </li>
 * </ul>
 * </p>
 * <p>
 * A convience implementation that makes breaking up this processor in smaller
 * delegate strategies easier can be found as
 * {@link wicket.request.compound.CompoundRequestCycleProcessor} (or
 * {@link wicket.request.compound.AbstractCompoundRequestCycleProcessor}).
 * </p>
 * 
 * @author hillenius
 */
public interface IRequestCycleProcessor
{
	/**
	 * Gets the object that is responsible for encoding request targets (like
	 * url's in links etc) and decoding urls and request parameters etc into
	 * {@link wicket.request.RequestParameters} objects.
	 * 
	 * @return the request encoder
	 */
	IRequestCodingStrategy getRequestCodingStrategy();

	/**
	 * <p>
	 * Resolves the request and returns the request target. Typically, the
	 * resolver uses the {@link wicket.request.RequestParameters} object that is
	 * passed in.
	 * </p>
	 * <p>
	 * Implementors of this method should be careful not to mix this code with
	 * event handling code; method {@link #processEvents(RequestCycle)} is meant
	 * for that purpose.
	 * </p>
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestParameters
	 *            The request parameters object as decoded by this processor's
	 *            {@link IRequestCodingStrategy}.
	 * @return the request target; has to be non-null!
	 */
	IRequestTarget resolve(RequestCycle requestCycle, RequestParameters requestParameters);

	/**
	 * After a page is restored, this method is responsible for calling any
	 * event handling code based on the request. For example, when a link is
	 * clicked, {@link #resolve(RequestCycle, RequestParameters)} should return
	 * the page that that link resides on, and this method should call the
	 * {@link wicket.markup.html.link.ILinkListener} interface on that
	 * component.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 */
	void processEvents(RequestCycle requestCycle);

	/**
	 * After the target is resolved and the request events are handled, it is
	 * time to respond to the request. This method is responsible for executing
	 * the proper response sequence given the current request target and
	 * response.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 */
	void respond(RequestCycle requestCycle);

	/**
	 * Whenever a unhandled exception is encountered during the processing of a
	 * request cycle, this method is called to respond to the request in a
	 * proper way.
	 * 
	 * @param e
	 *            any unhandled exception
	 * @param requestCycle
	 *            the current request cycle
	 */
	void respond(RuntimeException e, RequestCycle requestCycle);
}
