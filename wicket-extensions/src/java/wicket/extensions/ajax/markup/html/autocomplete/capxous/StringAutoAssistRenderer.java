package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import wicket.Response;

public class StringAutoAssistRenderer extends AbstractAutoAssistRenderer
{

	protected void renderAssist(Object object, Response response)
	{
		response.write(object.toString());
	}

	protected String getTextValue(Object object)
	{
		return object.toString();
	}

}
