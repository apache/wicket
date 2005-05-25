/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.customcomponents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebResponse;
import wicket.util.resource.UrlResourceStream;

/**
 * <p>
 * Panel that displays the import result of an URL.
 * </p>
 * @author Eelco Hillenius
 */
public class ImportPanel extends WebComponent
{
	/**
	 * <p>
	 * Valid characters in a scheme.
	 * </p>
	 * <p>
	 * RFC 1738 says the following:
	 * </p>
	 * <blockquote>Scheme names consist of a sequence of characters. The lower case
	 * letters "a"--"z", digits, and the characters plus ("+"), period ("."), and hyphen
	 * ("-") are allowed. For resiliency, programs interpreting URLs should treat upper
	 * case letters as equivalent to lower case in scheme names (e.g., allow "HTTP" as
	 * well as "http"). </blockquote>
	 * <p>
	 * We treat as absolute any URL that begins with such a scheme name, followed by a
	 * colon.
	 * </p>
	 */
	private static final String VALID_SCHEME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

	/**
	 * Construct.
	 * @param id component id
	 */
	public ImportPanel(final String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param model the model
	 */
	public ImportPanel(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param modelObject the model object (will be wrapped in a model)
	 */
	public ImportPanel(String id, String modelObject)
	{
		super(id, new Model(modelObject));
	}

	/**
	 * Imports the contents of the url of the model object.
	 * @return the imported contents
	 */
	protected String importAsString()
	{
		// gets the model object: should provide us with either an absolute or a relative url
		String url = getModelObjectAsString();

		if (isAbsolute(url))
		{
			// this is an absolute url; just create an URL object with it
			try
			{
				return importUrl(new URL(url));
			}
			catch (MalformedURLException e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		return importRelative(url);
	}

	/**
	 * Import a resource from a relative url.
	 * @param url the resource's url
	 * @return the resource as a string
	 */
	private String importRelative(String url)
	{
		// the url is relative; do some request dispatcher magic

		String targetUrl = url;
		HttpServletRequest httpServletRequest =
			((WebRequest)getRequest()).getHttpServletRequest();
		HttpServletResponse httpServletResponse =
			((WebResponse)getResponse()).getHttpServletResponse();

		// normalize the URL if we have an HttpServletRequest
		if (!targetUrl.startsWith("/"))
		{
			String sp = httpServletRequest.getServletPath();
			targetUrl = sp.substring(0, sp.lastIndexOf('/')) + '/' + targetUrl;
		}

		// strip any session id
		targetUrl = stripSession(targetUrl);

		// get and check the request dispatcher
		RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(targetUrl);
		if (dispatcher == null)
		{
			throw new WicketRuntimeException("no dispatcher found for url " + url + " (tried "
					+ targetUrl + ")");
		}
		// include the resource, using our custom wrapper
		ImportResponseWrapper irw = new ImportResponseWrapper(httpServletResponse);

		// spec mandates specific error handling form include()
		try
		{
			dispatcher.include(httpServletRequest, irw);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}

		// disallow inappropriate response codes
		if (irw.getStatus() < 200 || irw.getStatus() > 299)
		{
			throw new WicketRuntimeException(irw.getStatus() + " " + targetUrl);
		}

		// recover the response String from our wrapper
		try
		{
			return irw.getString();
		}
		catch (UnsupportedEncodingException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Imports the contents from the given url.
	 * @param url the url
	 * @return the imported contents
	 */
	private String importUrl(URL url)
	{
		UrlResourceStream resourceStream = new UrlResourceStream(url);
		String content = resourceStream.asString();
		return content;
	}

	/**
	 * Strips a servlet session ID from <tt>url</tt>. The session ID is encoded as a
	 * URL "path parameter" beginning with "jsessionid=". We thus remove anything we find
	 * between ";jsessionid=" (inclusive) and either EOS or a subsequent ';' (exclusive).
	 * @param url the url to strip from
	 * @return the resulting string
	 */
	private String stripSession(String url)
	{
		StringBuffer u = new StringBuffer(url);
		int sessionStart;
		while ((sessionStart = u.toString().indexOf(";jsessionid=")) != -1)
		{
			int sessionEnd = u.toString().indexOf(";", sessionStart + 1);
			if (sessionEnd == -1)
			{
				sessionEnd = u.toString().indexOf("?", sessionStart + 1);
			}
			if (sessionEnd == -1) // still
			{
				sessionEnd = u.length();
			}
			u.delete(sessionStart, sessionEnd);
		}
		return u.toString();
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		String content = importAsString();
		replaceComponentTagBody(markupStream, openTag, content);
	}

	/**
	 * Gets whether the given url is absolute (<tt>true</tt>) or relative (<tt>false</tt>).
	 * @param url the url
	 * @return whether the given url is absolute (<tt>true</tt>) or relative (<tt>false</tt>)
	 */
	protected boolean isAbsolute(String url)
	{
		if (url == null)
		{
			return false;
		}

		// do a fast, simple check first
		int colonPos;
		if ((colonPos = url.indexOf(":")) == -1)
		{
			return false;
		}

		// if we DO have a colon, make sure that every character
		// leading up to it is a valid scheme character
		for (int i = 0; i < colonPos; i++)
		{
			if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1)
			{
				return false;
			}
		}

		// if so, we've got an absolute url
		return true;
	}

	/** Wraps responses to allow us to retrieve results as Strings. */
	private final class ImportResponseWrapper extends HttpServletResponseWrapper
	{
		/*
		 * We provide either a Writer or an OutputStream as requested. We actually have a
		 * true Writer and an OutputStream backing both, since we don't want to use a
		 * character encoding both ways (Writer -> OutputStream -> Writer). So we use no
		 * encoding at all (as none is relevant) when the target resource uses a Writer.
		 * And we decode the OutputStream's bytes using OUR tag's 'charEncoding'
		 * attribute, or ISO-8859-1 as the default. We thus ignore setLocale() and
		 * setContentType() in this wrapper. In other words, the target's asserted
		 * encoding is used to convert from a Writer to an OutputStream, which is
		 * typically the medium through with the target will communicate its ultimate
		 * response. Since we short-circuit that mechanism and read the target's
		 * characters directly if they're offered as such, we simply ignore the target's
		 * encoding assertion.
		 */

		/** The Writer we convey. */
		private StringWriter stringWriter = new StringWriter();

		/** A buffer, alternatively, to accumulate bytes. */
		private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		/** A ServletOutputStream we convey, tied to this Writer. */
		private ServletOutputStream servletOutputStream = new ServletOutputStream()
		{
			public void write(int b) throws IOException
			{
				byteArrayOutputStream.write(b);
			}
		};

		/** 'True' if getWriter() was called; false otherwise. */
		private boolean writerUsed;

		/** 'True if getOutputStream() was called; false otherwise. */
		private boolean streamUsed;

		/** The HTTP status set by the target. */
		private int status = 200;

		/**
		 * Constructs a new ImportResponseWrapper.
		 * @param response the response to wrap
		 */
		public ImportResponseWrapper(HttpServletResponse response)
		{
			super(response);
		}

		/**
		 * Returns a Writer designed to buffer the output.
		 * @return writer
		 */
		public PrintWriter getWriter()
		{
			if (streamUsed)
			{
				throw new IllegalStateException("IMPORT_ILLEGAL_STREAM");
			}
			writerUsed = true;
			return new PrintWriter(stringWriter);
		}

		/**
		 * Returns a ServletOutputStream designed to buffer the output.
		 * @return ServletOutputStream
		 */
		public ServletOutputStream getOutputStream()
		{
			if (writerUsed)
			{
				throw new IllegalStateException("IMPORT_ILLEGAL_WRITER");
			}
			streamUsed = true;
			return servletOutputStream;
		}

		/**
		 * Has no effect.
		 * @param x
		 */
		public void setContentType(String x)
		{
		}

		/**
		 * Has no effect.
		 * @param x
		 */
		public void setLocale(Locale x)
		{
		}

		/**
		 * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
		 */
		public void setStatus(int status)
		{
			this.status = status;
		}

		/**
		 * @return status
		 */
		public int getStatus()
		{
			return status;
		}

		/**
		 * Retrieves the buffered output, using the containing tag's 'charEncoding'
		 * attribute, or the tag's default encoding, <b>if necessary </b>.
		 * @return buffered output
		 * @throws UnsupportedEncodingException
		 */
		public String getString() throws UnsupportedEncodingException
		{
			// not simply toString() because we need to throw
			// UnsupportedEncodingException
			if (writerUsed)
			{
				return stringWriter.toString();
			}
			else if (streamUsed)
			{
				return byteArrayOutputStream.toString();
			}
			else
			{
				return ""; // target didn't write anything
			}
		}
	}

}