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
package org.apache.wicket.islands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.islands.serializer.IslandSerializationException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.ValidationError;

/**
 * An island that is a form component: its client-side peer edits a value of type {@code T}, which
 * takes part in the form like the value of any other form component, with conversion, validators,
 * {@code required}, feedback messages, and normal and Ajax submits.
 * <p>
 * The value travels as JSON in a hidden input inside the host element. The peer reads and writes
 * it through its adapter (e.g. {@code useFormValue()} in React), which also fires
 * <code>input</code> and <code>change</code> events, so Ajax behaviors on this component work as
 * on any other. Besides its own props, the peer gets the form state: whether the value is
 * required, whether the component is enabled, its label, and its feedback messages.
 * <p>
 * <strong>The submitted value comes from the browser and is untrusted.</strong> It is converted
 * into {@code T} by the island serializer, so it can only hold what {@code T} declares, and it is
 * validated like any other input. A value that is markup, such as the HTML of a rich text
 * editor, must be sanitized before it is ever rendered without escaping; override
 * {@link #sanitize(Object)} to apply a sanitizer of your choice. Wicket bundles none.
 * <p>
 * After an Ajax request that validated it, a form island adds itself to the request target, so
 * its feedback messages reach the peer; see {@link #setUpdateOnValidation(boolean)}.
 *
 * @param <T>
 *            the type of the value
 * @param <P>
 *            the type of the props
 * @since 12.0.0
 */
public class FormIsland<T, P> extends FormComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The attribute marking the hidden input that holds the value.
	 */
	public static final String ATTR_VALUE = "data-wicket-island-value";

	private final IslandSupport island;

	private final Class<T> valueType;

	private final IModel<P> props;

	private boolean updateOnValidation = true;

	/**
	 * Creates a form island without props of its own.
	 *
	 * @param id
	 *            the component id
	 * @param type
	 *            the island type, naming the client-side module; letters, digits, '-' and '_'
	 * @param model
	 *            the model of the value
	 * @param valueType
	 *            the type of the value, which submitted JSON is converted into
	 */
	public FormIsland(String id, String type, IModel<T> model, Class<T> valueType)
	{
		this(id, type, model, valueType, null);
	}

	/**
	 * @param id
	 *            the component id
	 * @param type
	 *            the island type, naming the client-side module; letters, digits, '-' and '_'
	 * @param model
	 *            the model of the value
	 * @param valueType
	 *            the type of the value, which submitted JSON is converted into
	 * @param props
	 *            the model of the props, {@code null} for none
	 */
	public FormIsland(String id, String type, IModel<T> model, Class<T> valueType,
		IModel<P> props)
	{
		super(id, model);
		this.valueType = Args.notNull(valueType, "valueType");
		setType(valueType);
		this.props = props;
		island = new IslandSupport(type);
		setOutputMarkupId(true);
		setEscapeModelStrings(false);
		add(island.getBehavior());
	}

	/**
	 * @return the island type
	 */
	public final String getIslandType()
	{
		return island.getType();
	}

	/**
	 * @return the key identifying this island instance across re-renders
	 */
	public final String getKey()
	{
		return island.getKey();
	}

	final IslandSupport getIslandSupport()
	{
		return island;
	}

	/**
	 * Registers a call that returns a result.
	 *
	 * @see Island#onCall(String, Class, Class, IslandCallHandler)
	 */
	public final <Q, R> FormIsland<T, P> onCall(String name, Class<Q> requestType,
		Class<R> resultType, IslandCallHandler<Q, R> handler)
	{
		island.addEndpoint(name, requestType, resultType, handler);
		return this;
	}

	/**
	 * Registers a call that returns no result.
	 *
	 * @see Island#onCall(String, Class, IslandCallConsumer)
	 */
	public final <Q> FormIsland<T, P> onCall(String name, Class<Q> requestType,
		IslandCallConsumer<Q> handler)
	{
		island.addEndpoint(name, requestType, Void.class, IslandSupport.withoutResult(handler));
		return this;
	}

	/**
	 * @param updateOnValidation
	 *            whether this island adds itself to the Ajax request target after it was validated,
	 *            so its peer receives its feedback messages; {@code true} by default
	 * @return {@code this}
	 */
	public FormIsland<T, P> setUpdateOnValidation(boolean updateOnValidation)
	{
		this.updateOnValidation = updateOnValidation;
		return this;
	}

	/**
	 * Converts the submitted JSON into the value with the island serializer, and passes it through
	 * {@link #sanitize(Object)}. JSON that does not match the value type is a conversion error.
	 */
	@Override
	public void convertInput()
	{
		String input = getInput();
		if (Strings.isEmpty(input))
		{
			setConvertedInput(null);
			return;
		}
		try
		{
			setConvertedInput(sanitize(IslandSupport.serializer().fromJson(input, valueType)));
		}
		catch (IslandSerializationException e)
		{
			error(new ValidationError().addKey("FormIsland.ConversionError")
				.setVariable("label", getLabel() != null ? getLabel().getObject() : getId())
				.setMessage("The value is not valid."));
		}
	}

	/**
	 * Gives a subclass the chance to clean a submitted value before it is validated and reaches
	 * the model, e.g. by running HTML through a sanitizer. Returns the value unchanged by default.
	 *
	 * @param value
	 *            the value converted from the submitted JSON, never {@code null}
	 * @return the value to use
	 */
	protected T sanitize(T value)
	{
		return value;
	}

	@Override
	protected String getModelValue()
	{
		T value = getModelObject();
		return value == null ? "" : IslandSupport.serializer().toJson(value);
	}

	/**
	 * Makes a page-less endpoint callable by the client-side peer under the endpoint's id.
	 *
	 * @param endpointId
	 *            the id the endpoint is registered under in {@link PagelessEndpoints}
	 * @return {@code this}
	 */
	public final FormIsland<T, P> usePageless(String endpointId)
	{
		return usePageless(endpointId, endpointId);
	}

	/**
	 * Makes a page-less endpoint callable by the client-side peer under the given name. The peer
	 * calls it like any other call; it runs outside the page lock and cannot update components.
	 *
	 * @param name
	 *            the name the client calls
	 * @param endpointId
	 *            the id the endpoint is registered under in {@link PagelessEndpoints}
	 * @return {@code this}
	 */
	public final FormIsland<T, P> usePageless(String name, String endpointId)
	{
		island.addPageless(name, endpointId, null);
		return this;
	}

	/**
	 * Makes a page-less endpoint callable by the client-side peer under the given name, with a
	 * handle for the given context. The context is read when the island renders and kept on the
	 * server, in the {@link IslandSettings#getHandleStore() handle store}; the client only holds the
	 * handle and sends it with each call, and the endpoint reads the context with
	 * {@link PagelessCall#getHandle(Class)}. Use it for context the client must not choose, such as
	 * the record the page shows. The handle is reissued when the context changes.
	 * <p>
	 * <strong>A handle is not an authorization mechanism.</strong> The endpoint still checks that
	 * the session may access the context.
	 *
	 * @param name
	 *            the name the client calls
	 * @param endpointId
	 *            the id the endpoint is registered under in {@link PagelessEndpoints}
	 * @param context
	 *            the context of the calls; small, serializable and with a meaningful
	 *            {@code equals}
	 * @return {@code this}
	 */
	public final FormIsland<T, P> usePageless(String name, String endpointId,
		IModel<? extends Serializable> context)
	{
		island.addPageless(name, endpointId, Args.notNull(context, "context"));
		return this;
	}

	/**
	 * Lets calls reach a page Wicket recreated after the original expired. The recreated island has
	 * a new key, so the call is answered as expired instead of being handled.
	 */
	@Override
	public boolean canCallListenerAfterExpiry()
	{
		return true;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		tag.remove("name");
		island.onComponentTag(tag);
	}

	/**
	 * Leaves the host element as it is: the peer learns from the form state that the island is
	 * disabled.
	 */
	@Override
	protected void onDisabled(ComponentTag tag)
	{
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		String value = "<input type=\"hidden\" " + ATTR_VALUE + " name=\"" +
			Strings.escapeMarkup(getInputName()) + "\" value=\"" + Strings.escapeMarkup(getValue()) +
			"\">";
		replaceComponentTagBody(markupStream, openTag,
			value + island.renderState(props != null ? props.getObject() : null, renderFormState()));
	}

	private String renderFormState()
	{
		List<String> errors = new ArrayList<>();
		for (FeedbackMessage message : getFeedbackMessages().toList())
		{
			message.markRendered();
			errors.add(String.valueOf(message.getMessage()));
		}
		Map<String, Object> state = new LinkedHashMap<>();
		state.put("required", isRequired());
		state.put("enabled", isEnabledInHierarchy());
		state.put("label", getLabel() != null ? getLabel().getObject() : null);
		state.put("errors", errors);
		return IslandSupport.serializer().toJson(state);
	}

	@Override
	protected void onValid()
	{
		super.onValid();
		addToAjaxTarget();
	}

	@Override
	protected void onInvalid()
	{
		super.onInvalid();
		addToAjaxTarget();
	}

	private void addToAjaxTarget()
	{
		if (updateOnValidation)
		{
			RequestCycle.get().find(AjaxRequestTarget.class).ifPresent(target -> target.add(this));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		island.renderHead(response);
	}

	@Override
	protected void onDetach()
	{
		if (props != null)
		{
			props.detach();
		}
		island.detach();
		super.onDetach();
	}
}
