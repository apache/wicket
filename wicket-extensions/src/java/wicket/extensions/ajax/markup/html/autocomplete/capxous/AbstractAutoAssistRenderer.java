package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import wicket.Response;

public abstract class AbstractAutoAssistRenderer implements IAutoAssistRenderer
{

	public void render(Object object, Response response)
	{
		response.write("<div onSelect=\"this.txtBox.value='");
		response.write(getTextValue(object));
		response.write("';\">");
		renderAssist(object, response);
		response.write("</div>");
	}
	
	protected abstract void renderAssist(Object object, Response response);
	
	protected abstract String getTextValue(Object object);

}
