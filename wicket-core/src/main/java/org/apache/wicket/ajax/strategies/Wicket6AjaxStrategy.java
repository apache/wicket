package org.apache.wicket.ajax.strategies;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.attributes.AjaxAttributeName;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.ajax.json.JsonFunction;
import org.apache.wicket.ajax.json.JsonUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

/**
 *
 */
public class Wicket6AjaxStrategy implements IAjaxStrategy
{
	public void renderHead(final AjaxEventBehavior behavior, final IHeaderResponse response)
	{
		CharSequence js = behavior.getCallbackScript();

		response.render(OnDomReadyHeaderItem.forScript(js.toString()));
	}

	public void onComponentTag(final AjaxEventBehavior behavior, final ComponentTag tag)
	{
	}

	/**
	 * @return the attributes as string in JSON format
	 */
	@Override
	public final JSONObject getAjaxAttributes(AbstractDefaultAjaxBehavior behavior, Component component, AjaxRequestAttributes attributes)
	{
		JSONObject attributesJson = new JSONObject();

		try
		{
			attributesJson.put(AjaxAttributeName.URL.jsonName(), behavior.getCallbackUrl());
			AjaxRequestAttributes.Method method = attributes.getMethod();
			if (AjaxRequestAttributes.Method.POST == method)
			{
				attributesJson.put(AjaxAttributeName.METHOD.jsonName(), method);
			}

			if (component instanceof Page == false)
			{
				String componentId = component.getMarkupId();
				attributesJson.put(AjaxAttributeName.MARKUP_ID.jsonName(), componentId);
			}

			String formId = attributes.getFormId();
			if (Strings.isEmpty(formId) == false)
			{
				attributesJson.put(AjaxAttributeName.FORM_ID.jsonName(), formId);
			}

			if (attributes.isMultipart())
			{
				attributesJson.put(AjaxAttributeName.IS_MULTIPART.jsonName(), true);
			}

			String submittingComponentId = attributes.getSubmittingComponentName();
			if (Strings.isEmpty(submittingComponentId) == false)
			{
				attributesJson.put(AjaxAttributeName.SUBMITTING_COMPONENT_NAME.jsonName(),
						submittingComponentId);
			}

			String indicatorId = behavior.findIndicatorId();
			if (Strings.isEmpty(indicatorId) == false)
			{
				attributesJson.put(AjaxAttributeName.INDICATOR_ID.jsonName(), indicatorId);
			}

			for (IAjaxCallListener ajaxCallListener : attributes.getAjaxCallListeners())
			{
				if (ajaxCallListener != null)
				{
					CharSequence beforeHandler = ajaxCallListener.getBeforeHandler(component);
					appendListenerHandler(beforeHandler, attributesJson,
							AjaxAttributeName.BEFORE_HANDLER.jsonName(),
							BEFORE_HANDLER_FUNCTION_TEMPLATE);

					CharSequence beforeSendHandler = ajaxCallListener
							.getBeforeSendHandler(component);
					appendListenerHandler(beforeSendHandler, attributesJson,
							AjaxAttributeName.BEFORE_SEND_HANDLER.jsonName(),
							BEFORE_SEND_HANDLER_FUNCTION_TEMPLATE);

					CharSequence afterHandler = ajaxCallListener.getAfterHandler(component);
					appendListenerHandler(afterHandler, attributesJson,
							AjaxAttributeName.AFTER_HANDLER.jsonName(), AFTER_HANDLER_FUNCTION_TEMPLATE);

					CharSequence successHandler = ajaxCallListener.getSuccessHandler(component);
					appendListenerHandler(successHandler, attributesJson,
							AjaxAttributeName.SUCCESS_HANDLER.jsonName(),
							SUCCESS_HANDLER_FUNCTION_TEMPLATE);

					CharSequence failureHandler = ajaxCallListener.getFailureHandler(component);
					appendListenerHandler(failureHandler, attributesJson,
							AjaxAttributeName.FAILURE_HANDLER.jsonName(),
							FAILURE_HANDLER_FUNCTION_TEMPLATE);

					CharSequence completeHandler = ajaxCallListener.getCompleteHandler(component);
					appendListenerHandler(completeHandler, attributesJson,
							AjaxAttributeName.COMPLETE_HANDLER.jsonName(),
							COMPLETE_HANDLER_FUNCTION_TEMPLATE);

					CharSequence precondition = ajaxCallListener.getPrecondition(component);
					appendListenerHandler(precondition, attributesJson,
							AjaxAttributeName.PRECONDITION.jsonName(), PRECONDITION_FUNCTION_TEMPLATE);
				}
			}

			JSONArray extraParameters = JsonUtils.asArray(attributes.getExtraParameters());

			if (extraParameters.length() > 0)
			{
				attributesJson.put(AjaxAttributeName.EXTRA_PARAMETERS.jsonName(), extraParameters);
			}

			List<CharSequence> dynamicExtraParameters = attributes.getDynamicExtraParameters();
			if (dynamicExtraParameters != null)
			{
				for (CharSequence dynamicExtraParameter : dynamicExtraParameters)
				{
					String func = String.format(DYNAMIC_PARAMETER_FUNCTION_TEMPLATE,
							dynamicExtraParameter);
					JsonFunction function = new JsonFunction(func);
					attributesJson.append(AjaxAttributeName.DYNAMIC_PARAMETER_FUNCTION.jsonName(),
							function);
				}
			}

			if (attributes.isAsynchronous() == false)
			{
				attributesJson.put(AjaxAttributeName.IS_ASYNC.jsonName(), false);
			}

			String[] eventNames = attributes.getEventNames();
			if (eventNames.length == 1)
			{
				attributesJson.put(AjaxAttributeName.EVENT_NAME.jsonName(), eventNames[0]);
			}
			else
			{
				for (String eventName : eventNames)
				{
					attributesJson.append(AjaxAttributeName.EVENT_NAME.jsonName(), eventName);
				}
			}

			AjaxChannel channel = attributes.getChannel();
			if (channel != null)
			{
				attributesJson.put(AjaxAttributeName.CHANNEL.jsonName(), channel);
			}

			if (attributes.isPreventDefault())
			{
				attributesJson.put(AjaxAttributeName.IS_PREVENT_DEFAULT.jsonName(), true);
			}

			if (AjaxRequestAttributes.EventPropagation.STOP
					.equals(attributes.getEventPropagation()))
			{
				attributesJson.put(AjaxAttributeName.EVENT_PROPAGATION.jsonName(), "stop");
			}
			else if (AjaxRequestAttributes.EventPropagation.STOP_IMMEDIATE.equals(attributes
					.getEventPropagation()))
			{
				attributesJson.put(AjaxAttributeName.EVENT_PROPAGATION.jsonName(), "stopImmediate");
			}

			Duration requestTimeout = attributes.getRequestTimeout();
			if (requestTimeout != null)
			{
				attributesJson.put(AjaxAttributeName.REQUEST_TIMEOUT.jsonName(),
						requestTimeout.getMilliseconds());
			}

			boolean wicketAjaxResponse = attributes.isWicketAjaxResponse();
			if (wicketAjaxResponse == false)
			{
				attributesJson.put(AjaxAttributeName.IS_WICKET_AJAX_RESPONSE.jsonName(), false);
			}

			String dataType = attributes.getDataType();
			if (AjaxRequestAttributes.XML_DATA_TYPE.equals(dataType) == false)
			{
				attributesJson.put(AjaxAttributeName.DATATYPE.jsonName(), dataType);
			}

			ThrottlingSettings throttlingSettings = attributes.getThrottlingSettings();
			if (throttlingSettings != null)
			{
				JSONObject throttlingSettingsJson = new JSONObject();
				String throttleId = throttlingSettings.getId();
				if (throttleId == null)
				{
					throttleId = component.getMarkupId();
				}
				throttlingSettingsJson.put(AjaxAttributeName.THROTTLING_ID.jsonName(), throttleId);
				throttlingSettingsJson.put(AjaxAttributeName.THROTTLING_DELAY.jsonName(),
						throttlingSettings.getDelay().getMilliseconds());
				if (throttlingSettings.getPostponeTimerOnUpdate())
				{
					throttlingSettingsJson.put(
							AjaxAttributeName.THROTTLING_POSTPONE_ON_UPDATE.jsonName(), true);
				}
				attributesJson.put(AjaxAttributeName.THROTTLING.jsonName(), throttlingSettingsJson);
			}

			postprocessConfiguration(behavior, attributesJson, component);
		}
		catch (JSONException e)
		{
			throw new WicketRuntimeException(e);
		}

		return attributesJson;
	}

	@Override
	public void postprocessConfiguration(AbstractDefaultAjaxBehavior behavior, JSONObject attributesJson,
		Component component) throws JSONException
	{
		behavior.postprocessConfiguration(attributesJson, component);
	}

	@Override
	public CharSequence getCallbackScript(Component component, JSONObject ajaxAttributes)
	{
		return "Wicket.Ajax.ajax(" + ajaxAttributes + ");";
	}

	@Override
	public CharSequence getCallbackFunction(AjaxRequestAttributes attributes, CallbackParameter... extraParameters)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("function (");
		boolean first = true;
		for (CallbackParameter curExtraParameter : extraParameters)
		{
			if (curExtraParameter.getFunctionParameterName() != null)
			{
				if (!first)
					sb.append(',');
				else
					first = false;
				sb.append(curExtraParameter.getFunctionParameterName());
			}
		}
		sb.append(") {\n");
		sb.append(getCallbackFunctionBody(attributes, extraParameters));
		sb.append("}\n");
		return sb;
	}

	@Override
	public CharSequence getCallbackFunctionBody(AjaxRequestAttributes attributes, CallbackParameter... extraParameters)
	{
		attributes.setEventNames();
		StringBuilder sb = new StringBuilder();
		sb.append("var attrs = ");
		sb.append(attributes);
		sb.append(";\n");
		sb.append("var params = {");
		boolean first = true;
		for (CallbackParameter curExtraParameter : extraParameters)
		{
			if (curExtraParameter.getAjaxParameterName() != null)
			{
				if (!first)
					sb.append(',');
				else
					first = false;
				sb.append('\'').append(curExtraParameter.getAjaxParameterName()).append("': ")
						.append(curExtraParameter.getAjaxParameterCode());
			}
		}
		sb.append("};\n");
		if (attributes.getExtraParameters().isEmpty())
		{
			sb.append("attrs.").append(AjaxAttributeName.EXTRA_PARAMETERS).append(" = params;\n");
		}
		else
		{
			sb.append("attrs.").append(AjaxAttributeName.EXTRA_PARAMETERS).append(" = Wicket.merge(attrs.")
					.append(AjaxAttributeName.EXTRA_PARAMETERS).append(", params);\n");
		}
		sb.append("Wicket.Ajax.ajax(attrs);\n");
		return sb;
	}

	private void appendListenerHandler(final CharSequence handler, final JSONObject attributesJson,
		final String propertyName, final String functionTemplate) throws JSONException
	{
		if (Strings.isEmpty(handler) == false)
		{
			final JsonFunction function;
			if (handler instanceof JsonFunction)
			{
				function = (JsonFunction)handler;
			}
			else
			{
				String func = String.format(functionTemplate, handler);
				function = new JsonFunction(func);
			}
			attributesJson.append(propertyName, function);
		}
	}

}
