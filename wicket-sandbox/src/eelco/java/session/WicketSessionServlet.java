package session;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import wicket.Session;

public class WicketSessionServlet extends HttpServlet
{

	/**
	 * Constructor of the object.
	 */
	public WicketSessionServlet()
	{
		super();
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>Wicket Session Test Servlet</TITLE></HEAD>");
		out.println("  <BODY>");

		TestSession wicketSession = (TestSession)Session.get();

		HttpSession httpSession = request.getSession(false);

		out.println("<p><h3>current Wicket session object: " + wicketSession + "<h3>");
		if (wicketSession != null)
		{
			out.println("<br />\t&nbsp;&nbsp;name: " + wicketSession.getName() + "<br />");
		}

		out.println("<p><h3>session values</h3><ul>");
		if (httpSession != null)
		{
			Enumeration e = httpSession.getAttributeNames();
			while (e.hasMoreElements())
			{
				String name = (String)e.nextElement();
				Object value = httpSession.getAttribute(name);
				out.println("\t<li>");
				out.println("\t\t" + name + "\t\t\t" + value);
				out.println("\t</li>");
			}
		}
		out.println("</ul></p>");

		out.println("  </BODY>");
		out.println("</HTML>");

		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException
	{
	}
}
