/**
 * 
 */
package wicket.ajax;

import java.util.List;

/**
 * @author jcompagner
 *
 */
public class JavaScript
{
	/**
	 * can only be called by subclasses  
	 */
	protected JavaScript()
	{
		super();
	}
	
	
	/**
	 * @param object The browser that is used.
	 * @return The javascript implementation for the browser
	 */
	public static JavaScript getInstance(Object object)
	{
		return new JavaScript();
//		if(ie)
//		{
//			return new IEJavaScript();
//		}
//		else if (ff)
//		{
//			return new FFJavaScript();
//		}
	}
	
	
	/**
	 * @param output The StringBuffer where the output is appended to
	 * @param element The element where the childs should be removed from
	 */
	public void removeChilds(StringBuffer output, String element)
	{
		output.append("var ");
		output.append(element);
		output.append(" = document.getElementById('");
		output.append(element);
		output.append("');");
		output.append("var children = ");
		output.append(element);
		output.append(".childNodes;");
		output.append("for(var i=children.length;--i >= 0;){ feedbackul.removeChild(children[i])};");
	}
	
	
	/**
	 * @param output
	 * @param parent
	 * @param liText
	 */
	public void createLINode(StringBuffer output, String parent, String liText)
	{
		output.append("var li = document.createElement('li');");
		output.append("var linkText = document.createTextNode('");
		output.append(liText.replace('\'','"')); // TODO simple escape of qoutes
		output.append("');");
		output.append("li.appendChild(linkText);");
		output.append("document.getElementById('");
		output.append(parent);
		output.append("').appendChild(li);");
	}

	/**
	 * @param output
	 * @param parent
	 * @param liText
	 */
	public void createLINodes(StringBuffer output, String parent, List liText)
	{
		if(liText.size() == 0) return;
		if(liText.size() == 1) 
		{
			createLINode(output, parent, (String)liText.get(0));
			return;
		}
		output.append("var liTextArray = new Array(");
		for (int i = 0; i < liText.size(); i++)
		{
			String message = (String)liText.get(i);
			output.append("'");
			output.append(message.replace('\'', '\"')); // TODO better support for qoutes
			output.append("',");
		}
		output.setLength(output.length()-1);
		output.append("); var ");
		output.append(parent);
		output.append(" = document.getElementById('");
		output.append(parent);
		output.append("');");
		output.append("for(var i=0;i&lt;liTextArray.length;i++){");
		output.append("var li = document.createElement('li');");
		output.append("var linkText = document.createTextNode(liTextArray[i]);");
		output.append("li.appendChild(linkText);");
		output.append(parent);
		output.append(".appendChild(li);}");
	}
	
}
