package org.apache.wicket.protocol.http;

import org.apache.wicket.protocol.http.ResourceIsolationPolicy;

import javax.servlet.http.HttpServletRequest;

public class DefaultResourceIsolationPolicy implements ResourceIsolationPolicy {

  @Override
  public boolean isRequestAllowed(HttpServletRequest request) {
    String site = request.getHeader(SEC_FETCH_SITE_HEADER);

    // TODO: Is this needed?
    site = site.toLowerCase();

    // Allow same-site and browser-initiated requests
    if (SAME_ORIGIN.equals(site) || SAME_SITE.equals(site) || NONE.equals(site)) {
      return true;
    }

    // Allow simple top-level navigations except <object> and <embed>
    return isAllowedTopLevelNavigation(request);
  }

  private boolean isAllowedTopLevelNavigation(HttpServletRequest request) {
    // TODO: Assert these headers are never null at this point?
    String mode = request.getHeader(SEC_FETCH_MODE_HEADER);
    String dest = request.getHeader(SEC_FETCH_DEST_HEADER);

    // TODO: Does the method need to be upper cased?
    boolean isSimpleTopLevelNavigation = MODE_NAVIGATE.equals(mode) || "GET".equals(request.getMethod());
    boolean isNotObjectOrEmbedRequest = !DEST_EMBED.equals(dest) && !DEST_OBJECT.equals(dest);

    return isSimpleTopLevelNavigation && isNotObjectOrEmbedRequest;
  }
}
