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
package org.apache.wicket.ng.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ng.protocol.http.WicketURLDecoder;
import org.apache.wicket.ng.protocol.http.WicketURLEncoder;
import org.apache.wicket.ng.util.lang.Objects;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;

/**
 * Represents the URL part after Wicket Filter.
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
public final class Url implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<String> segments = new ArrayList<String>();

    private List<QueryParameter> parameters = new ArrayList<QueryParameter>();

    /**
     * Construct.
     */
    public Url()
    {
    }

    /**
     * Construct.
     * 
     * @param url
     *            url being copied
     */
    public Url(Url url)
    {
        Checks.argumentNotNull(url, "url");

        segments.addAll(url.getSegments());
        parameters.addAll(url.getQueryParameters());
    }

    /**
     * Construct.
     * 
     * @param segments
     * @param parameters
     */
    public Url(List<String> segments, List<QueryParameter> parameters)
    {
        Checks.argumentNotNull(segments, "segments");
        Checks.argumentNotNull(parameters, "parameters");

        this.segments.addAll(segments);
        this.parameters.addAll(parameters);
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
     * Returns whether the URL is absolute.
     * 
     * @return <code>true</code> if URL is absolute, <code>false</code> otherwise.
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
    public void removeQueryParameters(String name)
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
    public void removeLeadingSegments(int count)
    {
        Checks.argumentWithinRange(0, segments.size(), count, "count");
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
    public void prependLeadingSegments(List<String> newSegments)
    {
        Checks.argumentNotNull(newSegments, "segments");
        segments.addAll(0, newSegments);
    }

    /**
     * Convenience method that removes all query parameters with given name and adds new query
     * parameter with specified name and value
     * 
     * @param name
     * @param value
     */
    public void setQueryParameter(String name, Object value)
    {
        removeQueryParameters(name);
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
    public QueryParameter getQueryParameter(String name)
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
    public StringValue getQueryParameterValue(String name)
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
     * Represents a single query parameter
     * 
     * @author Matej Knopp
     */
    public final static class QueryParameter
    {
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
        public QueryParameter(String name, String value)
        {
            Checks.argumentNotNull(name, "name");
            Checks.argumentNotNull(value, "value");

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

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof QueryParameter == false)
            {
                return false;
            }
            QueryParameter rhs = (QueryParameter)obj;
            return Objects.equal(this.getName(), rhs.getName()) &&
                    Objects.equal(this.getValue(), rhs.getValue());
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(getName(), getValue());
        }

        @Override
        public String toString()
        {
            StringBuilder result = new StringBuilder();
            result.append(encodeParameter(getName()));
            if (!Strings.isEmpty(getValue()))
            {
                result.append('=');
                result.append(encodeParameter(getValue()));
            }
            return result.toString();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof Url == false)
        {
            return false;
        }
        Url rhs = (Url)obj;

        return getSegments().equals(rhs.getSegments()) &&
                getQueryParameters().equals(rhs.getQueryParameters());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(getSegments(), getQueryParameters());
    }

    private static String encodeSegment(String string)
    {
        return WicketURLEncoder.PATH_INSTANCE.encode(string);
    }

    private static String decodeSegment(String string)
    {
        return WicketURLDecoder.PATH_INSTANCE.decode(string);
    }

    private static String encodeParameter(String string)
    {
        return WicketURLEncoder.QUERY_INSTANCE.encode(string);
    }

    private static String decodeParameter(String string)
    {
        return WicketURLDecoder.QUERY_INSTANCE.decode(string);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String s : getSegments())
        {
            if (!first)
            {
                result.append('/');
            }
            first = false;
            result.append(encodeSegment(s));
        }

        first = true;

        for (QueryParameter p : getQueryParameters())
        {
            if (first)
            {
                result.append("?");
                first = false;
            }
            else
            {
                result.append("&");
            }
            result.append(p.toString());
        }

        return result.toString();
    }

    private static QueryParameter parseQueryParameter(String qp)
    {
        if (qp.indexOf('=') == -1)
        {
            return new QueryParameter(decodeParameter(qp), "");
        }
        String parts[] = qp.split("=");
        if (parts.length == 0)
        {
            return new QueryParameter("", "");
        }
        else if (parts.length == 1)
        {
            return new QueryParameter("", decodeParameter(parts[0]));
        }
        else
        {
            return new QueryParameter(decodeParameter(parts[0]), decodeParameter(parts[1]));
        }
    }

    /**
     * Parses the given URL string.
     * 
     * @param url
     * @return Url object
     */
    public static Url parse(String url)
    {
        Checks.argumentNotNull(url, "url");

        Url result = new Url();

        String segments;
        String query;

        int qIndex = url.indexOf('?');

        if (qIndex == -1)
        {
            segments = url;
            query = "";
        }
        else
        {
            segments = url.substring(0, qIndex);
            query = url.substring(qIndex + 1);
        }

        if (segments.length() > 0)
        {

            boolean removeLast = false;
            if (segments.endsWith("/"))
            {
                // we need to append something and remove it after splitting because otherwise the
                // trailing slashes will be lost
                segments += "/x";
                removeLast = true;
            }

            String segmentArray[] = segments.split("/");

            if (removeLast)
            {
                segmentArray[segmentArray.length - 1] = null;
            }

            for (String s : segmentArray)
            {
                if (s != null)
                {
                    result.segments.add(decodeSegment(s));
                }
            }
        }

        if (query.length() > 0)
        {
            String queryArray[] = query.split("&");
            for (String s : queryArray)
            {
                result.parameters.add(parseQueryParameter(s));
            }
        }

        return result;
    };
}
