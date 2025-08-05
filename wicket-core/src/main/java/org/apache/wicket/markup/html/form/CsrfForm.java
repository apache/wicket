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
package org.apache.wicket.markup.html.form;

import java.util.UUID;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link Form} that adds cross-site request forgery protection.
 *
 * @param <T>
 *            The model object type
 */
public class CsrfForm<T> extends Form<T> {

	private static final long serialVersionUID = 1L;

	// we can't use LogCategory as it is not visible here.
	private static final Logger LOGGER = LoggerFactory.getLogger(CsrfForm.class);

	public static class CSRFViolationException extends WicketRuntimeException {

		private static final long serialVersionUID = 1L;

		public CSRFViolationException(String message) {
			super(message);
		}
	}

	// serves as a mean to transfer CSRF protection token between server and client (and vice versa)
	private String clientCSRFToken;
	private final String serverCSRFToken;

	private HiddenField<String> csrfTokenHiddenField;

	/**
	 * Constructs a CsrfForm
	 *
	 * @param id See Component
	 */
	public CsrfForm(String id)
	{
		super(id);
		clientCSRFToken = generateCSRFToken();
		// keep an immutable copy on the server side.
		serverCSRFToken = clientCSRFToken;
	}

	/**
	 * Constructs a CsrfForm
	 *
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public CsrfForm(String id, IModel<T> model)
	{
		super(id, model);
		clientCSRFToken = generateCSRFToken();
		// keep an immutable copy on the server side.
		serverCSRFToken = clientCSRFToken;
	}

	/**
	 * Override to use a different way to generate a token. Default uses
	 * {@link UUID#randomUUID()}
	 *
	 * @return a CSRF token
	 */
	protected String generateCSRFToken()
	{
		return UUID.randomUUID().toString();
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		// the client will submit clientCSRFToken, and it will be set in clientCSRFToken on the server side
		// initially server sets this value, and it is expecting it back, unchanged, from the client along with
		// submitted data
		csrfTokenHiddenField = new HiddenField<>("CRSFFormVersion", LambdaModel.of(CsrfForm.this::getClientCSRFToken, CsrfForm.this::setClientCSRFToken), String.class);
		add(csrfTokenHiddenField);
	}

	@Override
	protected void delegateSubmit(IFormSubmitter submittingComponent) {
		// we compare the version stored on the server with the version the client sent
		// form is only processed if they match
		if (serverCSRFToken.equals(clientCSRFToken))
		{
			super.delegateSubmit(submittingComponent);
		}
		else
		{
			onCSRFViolation(clientCSRFToken, serverCSRFToken);
		}
	}

	/**
	 * Called when server stored token and the client sent token do not match.
	 *
	 * @param clientCSRFToken The client submitted token
	 * @param serverCSRFToken The server stored token.
	 */
	protected void onCSRFViolation(String clientCSRFToken, String serverCSRFToken)
	{
		String message = String.format("Preventing possible CSRF attack! Client clientCSRFToken='%s' and server serverCSRFToken='%s' do not match!", clientCSRFToken, serverCSRFToken);
		LOGGER.error(message);
		throw new CSRFViolationException(message);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		getResponse().write("<input type=\"hidden\" class=\"csrf-form\" name=\"" + csrfTokenHiddenField.getInputName() + "\" value =\"" + Strings.escapeMarkup(serverCSRFToken) + "\"/>");
		super.onComponentTagBody(markupStream, openTag);
	}

	public String getClientCSRFToken()
	{
		return clientCSRFToken;
	}

	public void setClientCSRFToken(String clientCSRFToken)
	{
		this.clientCSRFToken = clientCSRFToken;
	}
}