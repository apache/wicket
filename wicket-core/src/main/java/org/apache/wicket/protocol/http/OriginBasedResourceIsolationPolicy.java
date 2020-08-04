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
package org.apache.wicket.protocol.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OriginBasedResourceIsolationPolicy implements ResourceIsolationPolicy {
  private static final Logger log = LoggerFactory
      .getLogger(OriginBasedResourceIsolationPolicy.class);

  /**
   * A white list of accepted origins (host names/domain names) presented as
   * &lt;domainname&gt;.&lt;TLD&gt;. The domain part can contain subdomains.
   */
  private Collection<String> acceptedOrigins = new ArrayList<>();

  /**
   * Adds an origin (host name/domain name) to the white list. An origin is in the form of
   * &lt;domainname&gt;.&lt;TLD&gt;, and can contain a subdomain. Every Origin header that matches
   * a domain from the whitelist is accepted and not checked any further for CSRF issues.
   *
   * E.g. when {@code example.com} is in the white list, this allows requests from (i.e. with an
   * {@code Origin:} header containing) {@code example.com} and {@code blabla.example.com} but
   * rejects requests from {@code blablaexample.com} and {@code example2.com}.
   *
   * @param acceptedOrigin
   *            the acceptable origin
   * @return this
   */
  public OriginBasedResourceIsolationPolicy addAcceptedOrigin(String acceptedOrigin)
  {
    Checks.notNull("acceptedOrigin", acceptedOrigin);

    // strip any leading dot characters
    final int len = acceptedOrigin.length();
    int i = 0;
    while (i < len && acceptedOrigin.charAt(i) == '.')
    {
      i++;
    }
    acceptedOrigins.add(acceptedOrigin.substring(i));
    return this;
  }

  /**
   * This origin-based listener can be used in combination with the {@link FetchMetadataRequestCycleListener}
   * to add support for legacy browsers that don't send Sec-Fetch-* headers yet.
   * @return whether the request is allowed based on its origin
   */
  @Override
  public boolean isRequestAllowed(HttpServletRequest request, IRequestablePage targetPage) {
    String sourceUri = getSourceUri(request);

    if (sourceUri == null || sourceUri.isEmpty())
    {
      log.debug("Source URI not present in request to {}", request.getPathInfo());
      return true;
    }
    sourceUri = sourceUri.toLowerCase(Locale.ROOT);

    // if the origin is a know and trusted origin, don't check any further but allow the request
    if (isWhitelistedHost(sourceUri))
    {
      return true;
    }

    // check if the origin HTTP header matches the request URI
    if (!isLocalOrigin(request, sourceUri))
    {
      log.debug("Source URI conflicts with request origin");
      return false;
    }

    return true;
  }

  /**
   * Checks whether the {@code Origin} HTTP header of the request matches where the request came
   * from.
   *
   * @param containerRequest
   *            the current container request
   * @param originHeader
   *            the contents of the {@code Origin} HTTP header
   * @return {@code true} when the origin of the request matches the {@code Origin} HTTP header
   */
  protected boolean isLocalOrigin(HttpServletRequest containerRequest, String originHeader)
  {
    // Make comparable strings from Origin and Location
    String origin = normalizeUri(originHeader);
    if (origin == null)
      return false;

    String request = getTargetUriFromRequest(containerRequest);
    if (request == null)
      return false;

    return origin.equalsIgnoreCase(request);
  }

  /**
   * Creates a RFC-6454 comparable URI from the {@code request} requested resource.
   *
   * @param request
   *            the incoming request
   * @return only the scheme://host[:port] part, or {@code null} when the origin string is not
   *         compliant
   */
  protected final String getTargetUriFromRequest(HttpServletRequest request)
  {
    // Build scheme://host:port from request
    StringBuilder target = new StringBuilder();
    String scheme = request.getScheme();
    if (scheme == null)
    {
      return null;
    }
    else
    {
      scheme = scheme.toLowerCase(Locale.ROOT);
    }
    target.append(scheme);
    target.append("://");

    String host = request.getServerName();
    if (host == null)
    {
      return null;
    }
    target.append(host);

    int port = request.getServerPort();
    if ("http".equals(scheme) && port != 80 || "https".equals(scheme) && port != 443)
    {
      target.append(':');
      target.append(port);
    }

    return target.toString();
  }

  /**
   * Resolves the source URI from the request headers ({@code Origin} or {@code Referer}).
   *
   * @param containerRequest
   *            the current container request
   * @return the normalized source URI.
   */
  private String getSourceUri(HttpServletRequest containerRequest)
  {
    String sourceUri = containerRequest.getHeader(WebRequest.HEADER_ORIGIN);
    if (Strings.isEmpty(sourceUri))
    {
      sourceUri = containerRequest.getHeader(WebRequest.HEADER_REFERER);
    }
    return normalizeUri(sourceUri);
  }

  /**
   * Creates a RFC-6454 comparable URI from the {@code uri} string.
   *
   * @param uri
   *            the contents of the Origin or Referer HTTP header
   * @return only the scheme://host[:port] part, or {@code null} when the URI string is not
   *         compliant
   */
  protected final String normalizeUri(String uri)
  {
    // the request comes from a privacy sensitive context, flag as non-local origin. If
    // alternative action is required, an implementor can override any of the onAborted,
    // onSuppressed or onAllowed and implement such needed action.

    if (Strings.isEmpty(uri) || "null".equals(uri))
      return null;

    StringBuilder target = new StringBuilder();

    try
    {
      URI originUri = new URI(uri);
      String scheme = originUri.getScheme();
      if (scheme == null)
      {
        return null;
      }
      else
      {
        scheme = scheme.toLowerCase(Locale.ROOT);
      }

      target.append(scheme);
      target.append("://");

      String host = originUri.getHost();
      if (host == null)
      {
        return null;
      }
      target.append(host);

      int port = originUri.getPort();
      boolean portIsSpecified = port != -1;
      boolean isAlternateHttpPort = "http".equals(scheme) && port != 80;
      boolean isAlternateHttpsPort = "https".equals(scheme) && port != 443;

      if (portIsSpecified && (isAlternateHttpPort || isAlternateHttpsPort))
      {
        target.append(':');
        target.append(port);
      }
      return target.toString();
    }
    catch (URISyntaxException e)
    {
      log.debug("Invalid URI provided: {}, marked conflicting", uri);
      return null;
    }
  }

  /**
   * Checks whether the domain part of the {@code sourceUri} ({@code Origin} or {@code Referer}
   * header) is whitelisted.
   *
   * @param sourceUri
   *            the contents of the {@code Origin} or {@code Referer} HTTP header
   * @return {@code true} when the source domain was whitelisted
   */
  protected boolean isWhitelistedHost(final String sourceUri)
  {
    try
    {
      final String sourceHost = new URI(sourceUri).getHost();
      if (Strings.isEmpty(sourceHost))
        return false;
      for (String whitelistedOrigin : acceptedOrigins)
      {
        if (sourceHost.equalsIgnoreCase(whitelistedOrigin) ||
            sourceHost.endsWith("." + whitelistedOrigin))
        {
          log.trace("Origin {} matched whitelisted origin {}, request accepted",
              sourceUri, whitelistedOrigin);
          return true;
        }
      }
    }
    catch (URISyntaxException e)
    {
      log.debug("Origin: {} not parseable as an URI. Whitelisted-origin check skipped.",
          sourceUri);
    }

    return false;
  }
}
