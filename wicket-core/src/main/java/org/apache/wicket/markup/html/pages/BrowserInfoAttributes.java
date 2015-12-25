package org.apache.wicket.markup.html.pages;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.ajax.json.JsonFunction;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * @author Rakesh.A
 */
public class BrowserInfoAttributes
{

	private static enum AttributeName
	{
		FORM_ID("f"), AFTER_COLLECT_HANDLER("ac");

		private final String jsonName;

		AttributeName(String jsonName)
		{
			this.jsonName = Args.notNull(jsonName, "jsonName");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return jsonName;
		}
	}

	private static final String SUBMIT_CALL_TEMPLATE = "\nWicket.BrowserInfo.submitForm(%s)";
	private static final String AFTER_COLLECT_HANDLER_TEMPLATE = "function(info){%s}";

	private final String formId;
	private StringBuilder afterCollectHandler;

	public BrowserInfoAttributes(String formId)
	{
		this.formId = formId;
	}

	/**
	 * method allows customization to browser information collection process.
	 * One can override this method and return JavaScript function which will be
	 * executed after, basic/default browser information is collected.
	 *
	 * This function gets one argument, which will be a JavaScript object
	 * containing already collected browser information.
	 *
	 * @return afterCollectHandler JavaScript function
	 */
	public final StringBuilder getAfterCollectHandler()
	{
		if (afterCollectHandler == null)
		{
			afterCollectHandler = new StringBuilder();
		}
		return afterCollectHandler;
	}

	/**
	 * method prepares and returns browser info form submit javascript method
	 * call.
	 *
	 * @return Browser info form submit JavaScript method call
	 */
	public final CharSequence getCallbackScript()
	{
		CharSequence attrs = renderAttributes();
		return String.format(SUBMIT_CALL_TEMPLATE, attrs);
	}

	/**
	 * @return the formId
	 */
	protected final String getFormId()
	{
		return formId;
	}

	/**
	 * method prepares JSON object string, containing required attributes for
	 * browser info form submit JavaScript call.
	 *
	 * @return JSON object with required attributes for browser info form submit
	 */
	protected final CharSequence renderAttributes()
	{
		JSONObject attributesJson = new JSONObject();

		try
		{
			attributesJson.put(AttributeName.FORM_ID.toString(), this.getFormId());

			StringBuilder handler = this.getAfterCollectHandler();
			if (Strings.isEmpty(handler) == false)
			{
				String func = String.format(AFTER_COLLECT_HANDLER_TEMPLATE, handler);
				JsonFunction function = new JsonFunction(func);

				attributesJson.put(AttributeName.AFTER_COLLECT_HANDLER.toString(), function);
			}
		}
		catch (JSONException e)
		{
			throw new WicketRuntimeException(e);
		}

		return attributesJson.toString();
	}
}
