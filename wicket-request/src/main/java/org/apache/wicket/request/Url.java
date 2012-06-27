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
package org.apache.wicket.request;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.util.encoding.UrlDecoder;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;

/**
 * Represents the URL part <b>after Wicket Filter</b>. For example if Wicket Filter is mapped to
 * <code>/app/*</code> then with URL <code>/app/my/url</code> the {@link Url} object would represent
 * part <code>my/url</code>. If Wicket Filter is mapped to <code>/*</code> then with URL
 * <code>/my/url</code> the {@link Url} object would represent <code>my/url</code> (without leading
 * the slash).
 * <p>
 * URL consists of segments and query parameters.
 * <p>
 * Example URLs:
 * 
 * <pre>
 * foo/bar/baz?a=1&amp;b=5    - segments: [&quot;foo&quot;,&quot;bar,&quot;baz], query parameters: [&quot;a&quot;=&quot;1&quot;, &quot;b&quot;=&quot;5&quot;]
 * foo/bar//baz?=4&amp;6      - segments: [&quot;foo&quot;, &quot;bar&quot;, &quot;&quot;, &quot;baz&quot;], query parameters: [&quot;&quot;=&quot;4&quot;, &quot;6&quot;=&quot;&quot;]
 * /foo/bar/              - segments: [&quot;&quot;, &quot;foo&quot;, &quot;bar&quot;, &quot;&quot;]
 * foo/bar//              - segments: [&quot;foo&quot;, &quot;bar&quot;, &quot;&quot;, &quot;&quot;]
 * ?a=b                   - segments: [ ], query parameters: [&quot;a&quot;=&quot;b&quot;]
 * /                      - segments: [&quot;&quot;, &quot;&quot;]   (note that Url represents part after Wicket Filter 
 *                                                - so if Wicket filter is mapped to /* this would be
 *                                                an additional slash, i.e. //
 * </pre>
 * 
 * The Url class takes care of encoding and decoding of the segments and parameters.
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg
 */
public class Url implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_CHARSET_NAME = "UTF-8";

	private final List<String> segments;

	private final List<QueryParameter> parameters;

	private String charsetName;
	private transient Charset _charset;

	private String protocol;
	private Integer port;
	private String host;

	/**
	 * Modes with which urls can be stringized
	 * 
	 * @author igor
	 */
	public static enum StringMode 
	{
		/** local urls are rendered without the host name */
		LOCAL,
		/**
		 * full urls are written with hostname. if the hostname is not set or one of segments is
		 * {@literal ..} an {@link IllegalStateException} is thrown.
		 */
		FULL;
	}

	/**
	 * Construct.
	 */
	public Url()
	{
		segments = Generics.newArrayList();
		parameters = Generics.newArrayList();
	}

	/**
	 * Construct.
	 * 
	 * @param charset
	 */
	public Url(final Charset charset)
	{
		this();
		setCharset(charset);
	}


	/**
	 * copy constructor
	 * 
	 * @param url
	 *            url being copied
	 */
	public Url(final Url url)
	{
		Args.notNull(url, "url");

		this.protocol = url.protocol;
		this.host = url.host;
		this.port = url.port;
		this.segments = new ArrayList<String>(url.segments);
		this.parameters = new ArrayList<QueryParameter>(url.parameters);
		this.charsetName = url.charsetName;
		this._charset = url._charset;
	}

	/**
	 * Construct.
	 * 
	 * @param segments
	 * @param parameters
	 */
	public Url(final List<String> segments, final List<QueryParameter> parameters)
	{
		this(segments, parameters, null);
	}

	/**
	 * Construct.
	 * 
	 * @param segments
	 * @param charset
	 */
	public Url(final List<String> segments, final Charset charset)
	{
		this(segments, Collections.<QueryParameter> emptyList(), charset);
	}

	/**
	 * Construct.
	 * 
	 * @param segments
	 * @param parameters
	 * @param charset
	 */
	public Url(final List<String> segments, final List<QueryParameter> parameters,
		final Charset charset)
	{
		Args.notNull(segments, "segments");
		Args.notNull(parameters, "parameters");

		this.segments = new ArrayList<String>(segments);
		this.parameters = new ArrayList<QueryParameter>(parameters);
		setCharset(charset);
	}

	/**
	 * Parses the given URL string.
	 * 
	 * @param url
	 *            absolute or relative url with query string
	 * @return Url object
	 */
	public static Url parse(final CharSequence url)
	{
		return parse(url, null);
	}

	/**
	 * Parses the given URL string.
	 * 
	 * @param _url
	 *            absolute or relative url with query string
	 * @param charset
	 * @return Url object
	 */
	public static Url parse(CharSequence _url, Charset charset)
	{
		Args.notNull(_url, "_url");

		final Url result = new Url(charset);

		// the url object resolved the charset, use that
		charset = result.getCharset();

		String url = _url.toString();
		// extract query string part
		final String queryString;
		final String absoluteUrl;

		final int queryAt = url.indexOf('?');

		if (queryAt == -1)
		{
			queryString = "";
			absoluteUrl = url;
		}
		else
		{
			absoluteUrl = url.substring(0, queryAt);
			queryString = url.substring(queryAt + 1);
		}

		// get absolute / relative part of url
		String relativeUrl;

		// absolute urls contain a scheme://
		final int idxOfFirstSlash = absoluteUrl.indexOf('/');
		final int protocolAt = absoluteUrl.indexOf("://");

		if (protocolAt > -1 && (protocolAt < idxOfFirstSlash))
		{
			result.protocol = absoluteUrl.substring(0, protocolAt).toLowerCase(Locale.US);

			final String afterProto = absoluteUrl.substring(protocolAt + 3);
			final String hostAndPort;

			final int relativeAt = afterProto.indexOf('/');

			if (relativeAt == -1)
			{
				relativeUrl = "";
				hostAndPort = afterProto;
			}
			else
			{
				relativeUrl = afterProto.substring(relativeAt);
				hostAndPort = afterProto.substring(0, relativeAt);
			}

			final int portAt = hostAndPort.lastIndexOf(':');

			if (portAt == -1)
			{
				result.host = hostAndPort;
				result.port = getDefaultPortForProtocol(result.protocol);
			}
			else
			{
				result.host = hostAndPort.substring(0, portAt);
				result.port = Integer.parseInt(hostAndPort.substring(portAt + 1));
			}

			if (relativeAt < 0)
			{
				relativeUrl = "/";
			}
		}
		else
		{
			relativeUrl = absoluteUrl;
		}

		if (relativeUrl.length() > 0)
		{
			boolean removeLast = false;
			if (relativeUrl.endsWith("/"))
			{
				// we need to append something and remove it after splitting
				// because otherwise the
				// trailing slashes will be lost
				relativeUrl += "/x";
				removeLast = true;
			}

			String segmentArray[] = Strings.split(relativeUrl, '/');

			if (removeLast)
			{
				segmentArray[segmentArray.length - 1] = null;
			}

			for (String s : segmentArray)
			{
				if (s != null)
				{
					result.segments.add(decodeSegment(s, charset));
				}
			}
		}

		if (queryString.length() > 0)
		{
			String queryArray[] = Strings.split(queryString, '&');
			for (String s : queryArray)
			{
				if (Strings.isEmpty(s) == false)
				{
					result.parameters.add(parseQueryParameter(s, charset));
				}
			}
		}

		return result;
	}

	/**
	 * 
	 * @param qp
	 * @param charset
	 * @return query parameters
	 */
	private static QueryParameter parseQueryParameter(final String qp, final Charset charset)
	{
		if (qp.indexOf('=') == -1)
		{
			// name => empty value
			return new QueryParameter(decodeParameter(qp, charset), "");
		}

		String parts[] = Strings.split(qp, '=');
		return new QueryParameter(decodeParameter(parts[0], charset), decodeParameter(parts[1],
			charset));
	}

	/**
	 * get default port number for protocol
	 * 
	 * @param protocol
	 *            name of protocol
	 * @return default port for protocol or <code>null</code> if unknown
	 */
	private static Integer getDefaultPortForProtocol(String protocol)
	{
		if ("http".equals(protocol))
		{
			return 80;
		}
		else if ("https".equals(protocol))
		{
			return 443;
		}
		else if ("ftp".equals(protocol))
		{
			return 21;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 
	 * @return charset
	 */
	public Charset getCharset()
	{
		if (Strings.isEmpty(charsetName))
		{
			charsetName = DEFAULT_CHARSET_NAME;
		}
		if (_charset == null)
		{
			_charset = Charset.forName(charsetName);
		}
		return _charset;
	}

	/**
	 * 
	 * @param charset
	 */
	private void setCharset(final Charset charset)
	{
		if (charset == null)
		{
			charsetName = "UTF-8";
			_charset = null;
		}
		else
		{
			charsetName = charset.name();
			_charset = charset;
		}
	}

	/**
	 * Returns segments of the URL. Segments form the part before query string.
	 * 
	 * @return mutable list of segments
	 */
	public List<String> getSegments()
	{
		return segments;
	}

	/**
	 * Returns query parameters of the URL.
	 * 
	 * @return mutable list of query parameters
	 */
	public List<QueryParameter> getQueryParameters()
	{
		return parameters;
	}

	/**
	 * Returns whether the Url is absolute. Absolute Urls start with a '{@literal /}'.
	 * 
	 * @return <code>true</code> if Url is absolute, <code>false</code> otherwise.
	 */
	public boolean isAbsolute()
	{
		return !getSegments().isEmpty() && Strings.isEmpty(getSegments().get(0));
	}

	/**
	 * Convenience method that removes all query parameters with given name.
	 * 
	 * @param name
	 *            query parameter name
	 */
	public void removeQueryParameters(final String name)
	{
		for (Iterator<QueryParameter> i = getQueryParameters().iterator(); i.hasNext();)
		{
			QueryParameter param = i.next();
			if (Objects.equal(name, param.getName()))
			{
				i.remove();
			}
		}
	}

	/**
	 * Convenience method that removes <code>count</code> leading segments
	 * 
	 * @param count
	 */
	public void removeLeadingSegments(final int count)
	{
		Args.withinRange(0, segments.size(), count, "count");
		for (int i = 0; i < count; i++)
		{
			segments.remove(0);
		}
	}

	/**
	 * Convenience method that prepends <code>segments</code> to the segments collection
	 * 
	 * @param newSegments
	 */
	public void prependLeadingSegments(final List<String> newSegments)
	{
		Args.notNull(newSegments, "segments");
		segments.addAll(0, newSegments);
	}

	/**
	 * Convenience method that removes all query parameters with given name and adds new query
	 * parameter with specified name and value
	 * 
	 * @param name
	 * @param value
	 */
	public void setQueryParameter(final String name, final Object value)
	{
		removeQueryParameters(name);
		addQueryParameter(name, value);
	}

	/**
	 * Convenience method that removes adds a query parameter with given name
	 * 
	 * @param name
	 * @param value
	 */
	public void addQueryParameter(final String name, final Object value)
	{
		if (value != null)
		{
			QueryParameter parameter = new QueryParameter(name, value.toString());
			getQueryParameters().add(parameter);
		}
	}

	/**
	 * Returns first query parameter with specified name or null if such query parameter doesn't
	 * exist.
	 * 
	 * @param name
	 * @return query parameter or <code>null</code>
	 */
	public QueryParameter getQueryParameter(final String name)
	{
		for (QueryParameter parameter : parameters)
		{
			if (Objects.equal(name, parameter.getName()))
			{
				return parameter;
			}
		}
		return null;
	}

	/**
	 * Returns the value of first query parameter with specified name. Note that this method never
	 * returns <code>null</code>. Not even if the parameter does not exist.
	 * 
	 * @see StringValue#isNull()
	 * 
	 * @param name
	 * @return {@link StringValue} instance wrapping the parameter value
	 */
	public StringValue getQueryParameterValue(final String name)
	{
		QueryParameter parameter = getQueryParameter(name);
		if (parameter == null)
		{
			return StringValue.valueOf((String)null);
		}
		else
		{
			return StringValue.valueOf(parameter.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if ((obj instanceof Url) == false)
		{
			return false;
		}
		Url rhs = (Url)obj;

		return getSegments().equals(rhs.getSegments()) &&
			getQueryParameters().equals(rhs.getQueryParameters());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return Objects.hashCode(getSegments(), getQueryParameters());
	}

	/**
	 * 
	 * @param string
	 * @param charset
	 * @return encoded segment
	 */
	private static String encodeSegment(final String string, final Charset charset)
	{
		return UrlEncoder.PATH_INSTANCE.encode(string, charset);
	}

	/**
	 * 
	 * @param string
	 * @param charset
	 * @return decoded segment
	 */
	private static String decodeSegment(final String string, final Charset charset)
	{
		return UrlDecoder.PATH_INSTANCE.decode(string, charset);
	}

	/**
	 * 
	 * @param string
	 * @param charset
	 * @return encoded parameter
	 */
	private static String encodeParameter(final String string, final Charset charset)
	{
		return UrlEncoder.QUERY_INSTANCE.encode(string, charset);
	}

	/**
	 * 
	 * @param string
	 * @param charset
	 * @return decoded parameter
	 */
	private static String decodeParameter(final String string, final Charset charset)
	{
		return UrlDecoder.QUERY_INSTANCE.decode(string, charset);
	}

	/**
	 * Renders a url with {@link StringMode#LOCAL} using the url's charset
	 */
	@Override
	public String toString()
	{
		return toString(getCharset());
	}

        /**
	 * Stringizes this url
	 * 
	 * @param mode
	 *            {@link StringMode} that determins how to stringize the url
	 * @param charset
	 *            charset
	 * @return sringized version of this url
	 * 
	 */
	public String toString(StringMode mode, Charset charset)
	{
		StringBuilder result = new StringBuilder();
		final String path = getPath(charset);

		if (StringMode.FULL == mode)
		{
			if (Strings.isEmpty(host))
			{
				throw new IllegalStateException("Cannot render this url in " +
					StringMode.FULL.name() + " mode because it does not have a host set.");
			}

			String protocol = this.protocol;
			if (Strings.isEmpty(protocol))
			{
				protocol = "http";
			}

			// output scheme://host:port if specified
			result.append(protocol);
			result.append("://");
			result.append(host);

			if (port != null && port.equals(getDefaultPortForProtocol(protocol)) == false)
			{
				result.append(':');
				result.append(port);
			}

			if (path.contains(".."))
			{
				throw new IllegalStateException("Cannot render this url in " +
					StringMode.FULL.name() + " mode because it has a `..` segment: " + toString());
			}

			if (!path.startsWith("/"))
			{
				result.append("/");
			}

		}


		result.append(path);
		result.append(getQueryString(charset));
		return result.toString();
	}

	/**
	 * Stringizes this url using the specifid {@link StringMode} and url's charset
	 * 
	 * @param mode
	 *            {@link StringMode} that determins how to stringize the url
	 * @return stringized url
	 */
	public String toString(StringMode mode)
	{
		return toString(mode, getCharset());
	}


	/**
	 * Stringizes this url using {@link StringMode#LOCAL} and the specified charset
	 * 
	 * @param charset
	 * @return stringized url
	 */
	public String toString(final Charset charset)
	{
		return toString(StringMode.LOCAL, charset);
	}

	/**
	 * 
	 * @return true if last segment contains a name and not something like "." or "..".
	 */
	private boolean isLastSegmentReal()
	{
		if (segments.isEmpty())
		{
			return false;
		}
		String last = segments.get(segments.size() - 1);
		return (last.length() > 0) && !".".equals(last) && !"..".equals(last);
	}

	/**
	 * @param segments
	 * @return true if last segment is empty
	 */
	private boolean isLastSegmentEmpty(final List<String> segments)
	{
		if (segments.isEmpty())
		{
			return false;
		}
		String last = segments.get(segments.size() - 1);
		return last.length() == 0;
	}

	/**
	 * 
	 * @return true, if last segement is empty
	 */
	private boolean isLastSegmentEmpty()
	{
		return isLastSegmentEmpty(segments);
	}

	/**
	 * 
	 * @param segments
	 * @return true if at least one segement is real
	 */
	private boolean isAtLeastOneSegmentReal(final List<String> segments)
	{
		for (String s : segments)
		{
			if ((s.length() > 0) && !".".equals(s) && !"..".equals(s))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Concatenate the specified segments; The segments can be relative - begin with "." or "..".
	 * 
	 * @param segments
	 */
	public void concatSegments(List<String> segments)
	{
		boolean checkedLastSegment = false;

		if (!isAtLeastOneSegmentReal(segments) && !isLastSegmentEmpty(segments))
		{
			segments = new ArrayList<String>(segments);
			segments.add("");
		}

		for (String s : segments)
		{
			if (".".equals(s))
			{
				continue;
			}
			else if ("..".equals(s) && !this.segments.isEmpty())
			{
				this.segments.remove(this.segments.size() - 1);
			}
			else
			{
				if (!checkedLastSegment)
				{
					if (isLastSegmentReal() || isLastSegmentEmpty())
					{
						this.segments.remove(this.segments.size() - 1);
					}
					checkedLastSegment = true;
				}
				this.segments.add(s);
			}
		}

		if ((this.segments.size() == 1) && (this.segments.get(0).length() == 0))
		{
			this.segments.clear();
		}
	}

	/**
	 * Represents a single query parameter
	 * 
	 * @author Matej Knopp
	 */
	public final static class QueryParameter implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String name;
		private final String value;

		/**
		 * Creates new {@link QueryParameter} instance. The <code>name</code> and <code>value</code>
		 * parameters must not be <code>null</code>, though they can be empty strings.
		 * 
		 * @param name
		 *            parameter name
		 * @param value
		 *            parameter value
		 */
		public QueryParameter(final String name, final String value)
		{
			Args.notNull(name, "name");
			Args.notNull(value, "value");

			this.name = name;
			this.value = value;
		}

		/**
		 * Returns query parameter name.
		 * 
		 * @return query parameter name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Returns query parameter value.
		 * 
		 * @return query parameter value
		 */
		public String getValue()
		{
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if ((obj instanceof QueryParameter) == false)
			{
				return false;
			}
			QueryParameter rhs = (QueryParameter)obj;
			return Objects.equal(getName(), rhs.getName()) &&
				Objects.equal(getValue(), rhs.getValue());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			return Objects.hashCode(getName(), getValue());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return toString(Charset.forName(DEFAULT_CHARSET_NAME));
		}

		/**
		 * 
		 * @param charset
		 * @return see toString()
		 */
		public String toString(final Charset charset)
		{
			StringBuilder result = new StringBuilder();
			result.append(encodeParameter(getName(), charset));
			if (!Strings.isEmpty(getValue()))
			{
				result.append('=');
				result.append(encodeParameter(getValue(), charset));
			}
			return result.toString();
		}
	}

	/**
	 * Makes this url the result of resolving the {@code relative} url against this url.
	 * <p>
	 * Segments will be properly resolved, handling any {@code ..} references, while the query
	 * parameters will be completely replaced with {@code relative}'s query parameters.
	 * </p>
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * wicket/page/render?foo=bar
	 * </pre>
	 * 
	 * resolved with
	 * 
	 * <pre>
	 * ../component/render?a=b
	 * </pre>
	 * 
	 * will become
	 * 
	 * <pre>
	 * wicket/component/render?a=b
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param relative
	 *            relative url
	 */
	public void resolveRelative(final Url relative)
	{
		if (getSegments().size() > 0)
		{
			// strip the first non-folder segment
			getSegments().remove(getSegments().size() - 1);
		}

		// remove leading './' (current folder) and empty segments, process any ../ segments from the
		// relative url
		while (!relative.getSegments().isEmpty())
		{
			if (".".equals(relative.getSegments().get(0)))
			{
				relative.getSegments().remove(0);
			}
			else if ("".equals(relative.getSegments().get(0)))
			{
				relative.getSegments().remove(0);
			}
			else if ("..".equals(relative.getSegments().get(0)))
			{
				relative.getSegments().remove(0);
				if (getSegments().isEmpty() == false)
				{
					getSegments().remove(getSegments().size() - 1);
				}
			}
			else
			{
				break;
			}
		}

		// append the remaining relative segments
		getSegments().addAll(relative.getSegments());

		// replace query params with the ones from relative
		parameters.clear();
		parameters.addAll(relative.getQueryParameters());
	}

	/**
	 * Gets the protocol of this url (http/https/etc)
	 * 
	 * @return protocol or {@code null} if none has been set
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * Sets the protocol of this url (http/https/etc)
	 * 
	 * @param protocol
	 */
	public void setProtocol(final String protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * Gets the port of this url
	 * 
	 * @return port or {@code null} if none has been set
	 */
	public Integer getPort()
	{
		return port;
	}

	/**
	 * Sets the port of this url
	 * 
	 * @param port
	 */
	public void setPort(final Integer port)
	{
		this.port = port;
	}

	/**
	 * Gets the host name of this url
	 * 
	 * @return host name or {@code null} if none is seto
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Sets the host name of this url
	 * 
	 * @param host
	 */
	public void setHost(final String host)
	{
		this.host = host;
	}

	/**
	 * return path for current url in given encoding
	 * 
	 * @param charset
	 *            character set for encoding
	 * 
	 * @return path string
	 */
	public String getPath(Charset charset)
	{
		Args.notNull(charset, "charset");

		StringBuilder path = new StringBuilder();
		boolean slash = false;

		for (String segment : getSegments())
		{
			if (slash)
			{
				path.append('/');
			}
			path.append(encodeSegment(segment, charset));
			slash = true;
		}
		return path.toString();
	}

	/**
	 * return path for current url in original encoding
	 * 
	 * @return path string
	 */
	public String getPath()
	{
		return getPath(getCharset());
	}

	/**
	 * return query string part of url in given encoding
	 * 
	 * @param charset
	 *            character set for encoding
	 * 
	 * @return query string
	 */
	public String getQueryString(Charset charset)
	{
		Args.notNull(charset, "charset");

		StringBuilder query = new StringBuilder();

		for (QueryParameter parameter : getQueryParameters())
		{
			query.append(query.length() == 0 ? '?' : '&');
			query.append(parameter.toString(charset));
		}
		return query.toString();
	}

	/**
	 * return query string part of url in original encoding
	 * 
	 * @return query string
	 */
	public String getQueryString()
	{
		return getQueryString(getCharset());
	}
	
	
	/**
	 * Try to reduce url by eliminating '..' and '.' from the path where appropriate
	 * (this is somehow similar to {@link java.io.File#getCanonicalPath()}).
	 * Either by different / unexpected browser behavior or by malicious attacks it 
	 * can happen that these kind of redundant urls are processed by wicket. These urls 
	 * can cause some trouble when mapping the request.
	 * <p/> 
	 * <strong>example:</strong>
	 * 
	 * the url
	 * 
	 * <pre>  /example/..;jsessionid=234792?0</pre> 
	 * 
	 * will not get normalized by the browser due to the ';jsessionid' string that 
	 * gets appended by the servlet container. After wicket strips the 
	 * jsessionid part the resulting internal url will be
	 * 
	 * <pre>  /example/..</pre>
	 * 
	 * instead of
	 * 
	 * <pre>  /</pre>
	 * 
	 * <p/>
	 * 
	 * This code correlates to 
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4303">WICKET-4303</a>
	 *
	 * @return canonical url
	 */
	public Url canonical()
	{
		Url url = new Url(this);
		url.segments.clear();

		for (int i = 0; i < this.segments.size(); i++)
		{
			final String segment = this.segments.get(i);

			// drop '.' from path  
			if (".".equals(segment))
			{
				continue;
			}

			// skip segment if following segment is a '..'
			if ((i + 1) < this.segments.size() && "..".equals(this.segments.get(i + 1)))
			{
				i++;
				continue;
			}

			url.segments.add(segment);
		}
		return url;
	}
}
