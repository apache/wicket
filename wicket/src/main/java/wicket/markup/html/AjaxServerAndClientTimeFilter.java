package wicket.markup.html;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.IResponseFilter;
import wicket.RequestCycle;
import wicket.Session;
import wicket.model.Model;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.JavascriptUtils;

/**
 * This is a filter that injects javascript code to the top head portion and
 * after the body so that the time can me measured what the client parse time
 * was for this page and for ajax calls done on the page. 
 * It also reports the total server parse/response time in the client and logs 
 * the server response time and response size it took for a specific response 
 * in the server log.
 * 
 * You can specify what the status text should be like this:
 * ServerAndClientTimeFilter.statustext=My Application, Server parsetime:
 * ${servertime}, Client parsetime: ${clienttime} likewise for ajax request use
 * ajax.ServerAndClientTimeFilter.statustext
 * 
 * @author jcompagner
 */
public class AjaxServerAndClientTimeFilter implements IResponseFilter
{
	private static Log log = LogFactory.getLog(AjaxServerAndClientTimeFilter.class);

	/**
	 * @see wicket.IResponseFilter#filter(java.lang.StringBuffer)
	 */
	public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
	{
		int headIndex = responseBuffer.indexOf("<head>");
		int bodyIndex = responseBuffer.indexOf("</body>");
		int ajaxStart = responseBuffer.indexOf("<ajax-response>");
		int ajaxEnd = responseBuffer.indexOf("</ajax-response>");
		long timeTaken = System.currentTimeMillis() - RequestCycle.get().getStartTime();
		if (headIndex != -1 && bodyIndex != -1)
		{
			AppendingStringBuffer endScript = new AppendingStringBuffer(150);
			endScript.append("\n").append(JavascriptUtils.SCRIPT_OPEN_TAG);
			endScript.append("\nwindow.defaultStatus='");
			endScript.append(getStatusString(timeTaken, "ServerAndClientTimeFilter.statustext"));
			endScript.append("';\n").append(JavascriptUtils.SCRIPT_CLOSE_TAG).append("\n");
			responseBuffer.insert(bodyIndex - 1, endScript);
			responseBuffer.insert(headIndex + 6, "\n" + JavascriptUtils.SCRIPT_OPEN_TAG
					+ "\nvar clientTimeVariable = new Date().getTime();\n"
					+ JavascriptUtils.SCRIPT_CLOSE_TAG + "\n");
		}
		else if (ajaxStart != -1 && ajaxEnd != -1)
		{
			AppendingStringBuffer startScript = new AppendingStringBuffer(250);
			startScript.append("<evaluate><![CDATA[window.defaultStatus='");
			startScript.append(getStatusString(timeTaken,
					"ajax.ServerAndClientTimeFilter.statustext"));
			startScript.append("';]]></evaluate>");
			responseBuffer.insert(ajaxEnd, startScript.toString());
			responseBuffer.insert(ajaxStart + 15,
					"<evaluate><![CDATA[clientTimeVariable = new Date().getTime();]]></evaluate>");
		}
		log.info(timeTaken + "ms server time taken for request "
				+ RequestCycle.get().getRequest().getURL() + " response size: "
				+ responseBuffer.length());
		return responseBuffer;
	}

	/**
	 * Returns a locale specific status message about the server and client
	 * time.
	 * 
	 * @param timeTaken
	 *            the server time it took
	 * @param resourceKey
	 *            The key for the locale specific string lookup
	 * @return String with the status message
	 */
	private String getStatusString(long timeTaken, String resourceKey)
	{
		Map<String,String> map = new HashMap<String,String>(4);
		map.put("clienttime", "' + (new Date().getTime() - clientTimeVariable)/1000 +  's");
		map.put("servertime", ((double)timeTaken) / 1000 + "s");
		AppendingStringBuffer defaultValue = new AppendingStringBuffer(128);
		defaultValue.append("Server parsetime: ");
		defaultValue.append(((double)timeTaken) / 1000);
		defaultValue.append("s, Client parsetime: ' + (new Date().getTime() - clientTimeVariable)/1000 +  's");
		String txt = Application.get().getResourceSettings().getLocalizer().getString(resourceKey,
				null, Model.valueOf(map), Session.get().getLocale(), Session.get().getStyle(),
				defaultValue.toString());
		return txt;
	}
}