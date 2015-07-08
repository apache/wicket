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
package org.apache.wicket.ajax.attributes;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.util.lang.Args;

/**
 * ajax attribute names
 * 
 * @author mosmann
 */
public enum AjaxAttributeName {

	/**
	 * throttling settings (tr)
	 * 
	 * @see AjaxRequestAttributes#getThrottlingSettings()
	 */
	THROTTLING("tr"),

	/**
	 * throttling - postpone timer on upate (p)
	 * 
	 * @see ThrottlingSettings#getPostponeTimerOnUpdate()
	 */
	THROTTLING_POSTPONE_ON_UPDATE("p"),

	/**
	 * throttling - delay (d)
	 * 
	 * @see ThrottlingSettings#getDelay()
	 */
	THROTTLING_DELAY("d"),

	/**
	 * throttling - id (id)
	 * 
	 * @see ThrottlingSettings#getId()
	 */
	THROTTLING_ID("id"),

	/**
	 * datatype (dt)
	 * 
	 * @see AjaxRequestAttributes#getDataType()
	 */
	DATATYPE("dt"),

	/**
	 * is wicket ajax response (wr)
	 * 
	 * @see AjaxRequestAttributes#isWicketAjaxResponse()
	 */
	IS_WICKET_AJAX_RESPONSE("wr"),

	/**
	 * request timeout (rt)
	 * 
	 * @see AjaxRequestAttributes#getRequestTimeout()
	 */
	REQUEST_TIMEOUT("rt"),

	/**
	 * allow default
	 * 
	 * @see AjaxRequestAttributes#isAllowDefault()
	 */
	IS_ALLOW_DEFAULT("ad"),

	/**
	 * stop propagation
	 *
	 * @see AjaxRequestAttributes#setEventPropagation(org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation)
	 */
	EVENT_PROPAGATION("sp"),

	/**
	 * channel (ch)
	 * 
	 * @see AjaxRequestAttributes#getChannel()
	 */
	CHANNEL("ch"),

	/**
	 * event name (e)
	 * 
	 * @see AjaxRequestAttributes#getEventNames()
	 */
	EVENT_NAME("e"),

	/**
	 * is async (async)
	 * 
	 * @see AjaxRequestAttributes#isAsynchronous()
	 */
	IS_ASYNC("async"),

	/**
	 * dynamic parameters (dep)
	 * 
	 * @see AjaxRequestAttributes#getDynamicExtraParameters()
	 */
	DYNAMIC_PARAMETER_FUNCTION("dep"),

	/**
	 * extra parameters (ep)
	 * 
	 * @see AjaxRequestAttributes#getExtraParameters()
	 */
	EXTRA_PARAMETERS("ep"),

	/**
	 * precondition (pre)
	 * 
	 * @see AjaxCallListener#getPrecondition(org.apache.wicket.Component)
	 */
	PRECONDITION("pre"),

	/**
	 * complete handler (coh)
	 * 
	 * @see AjaxCallListener#getCompleteHandler(org.apache.wicket.Component)
	 */
	COMPLETE_HANDLER("coh"),

	/**
	 * failure handler (fh)
	 * 
	 * @see AjaxCallListener#getFailureHandler(org.apache.wicket.Component)
	 */
	FAILURE_HANDLER("fh"),

	/**
	 * success handler (sh)
	 * 
	 * @see AjaxCallListener#getSuccessHandler(org.apache.wicket.Component)
	 */
	SUCCESS_HANDLER("sh"),

	/**
	 * after handler (ah)
	 * 
	 * @see AjaxCallListener#getAfterHandler(org.apache.wicket.Component)
	 */
	AFTER_HANDLER("ah"),

	/**
	 * before send handler (bsh)
	 * 
	 * @see AjaxCallListener#getBeforeSendHandler(org.apache.wicket.Component)
	 */
	BEFORE_SEND_HANDLER("bsh"),

	/**
	 * before handler (bh)
	 * 
	 * @see AjaxCallListener#getBeforeHandler(org.apache.wicket.Component)
	 */
	BEFORE_HANDLER("bh"),

	/**
	 * init handler (ih)
	 * 
	 * @see AjaxCallListener#getInitHandler(org.apache.wicket.Component)
	 */
	INIT_HANDLER("ih"),

	/**
	 * done handler (dh)
	 *
	 * @see AjaxCallListener#getDoneHandler(org.apache.wicket.Component)
	 */
	DONE_HANDLER("dh"),

	/**
	 * the indicator id, if any found (i)
	 * 
	 * @see AbstractDefaultAjaxBehavior#findIndicatorId()
	 */
	INDICATOR_ID("i"),

	/**
	 * submitting component name (sc)
	 * 
	 * @see AjaxRequestAttributes#getSubmittingComponentName()
	 */
	SUBMITTING_COMPONENT_NAME("sc"),

	/**
	 * is multipart (mp)
	 * 
	 * @see AjaxRequestAttributes#isMultipart()
	 */
	IS_MULTIPART("mp"),

	/**
	 * form id (f)
	 * 
	 * @see AjaxRequestAttributes#getFormId()
	 */
	FORM_ID("f"),

	/**
	 * markup id of behavior attached component (c)
	 * 
	 * @see AbstractDefaultAjaxBehavior#renderAjaxAttributes(org.apache.wicket.Component)
	 */
	MARKUP_ID("c"),

	/**
	 * http method (m)
	 * 
	 * @see AjaxRequestAttributes#getMethod()
	 */
	METHOD("m"),

	/**
	 * @see AbstractDefaultAjaxBehavior#getCallbackUrl();
	 */
	URL("u"),

	/**
	 * @see org.apache.wicket.ajax.attributes.AjaxRequestAttributes#childSelector
	 */
	CHILD_SELECTOR("sel");

	private final String jsonName;

	private AjaxAttributeName(String jsonName)
	{
		this.jsonName = Args.notNull(jsonName, "jsonName");
	}

	/**
	 * the json parameter name
	 * 
	 * @return as string
	 */
	public String jsonName()
	{
		return jsonName;
	}

	@Override
	public String toString()
	{
		return jsonName;
	}
}
