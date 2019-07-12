package org.apache.wicket.markup.head.filter;

import org.apache.wicket.Application;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.head.CssContentHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.CssUrlReferenceHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptUrlReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.HeaderItemAttribute;
import org.apache.wicket.util.value.HeaderItemAttributeMap;

/**
 * Add CSP nonce to all relevant JavaScript and CSS header items
 * <p>
 * Note: please don't forget to wrap with {@link ResourceAggregator}
 *  when setting it up with {@link Application#setHeaderResponseDecorator},
 *  otherwise dependencies will not be rendered
 */
public abstract class CspNonceHeaderResponse extends DecoratingHeaderResponse {

    public CspNonceHeaderResponse(IHeaderResponse real) {
        super(real);
    }

    @Override
    public void render(HeaderItem item) {
        while (item instanceof IWrappedHeaderItem) {
            item = ((IWrappedHeaderItem)item).getWrapped();
        }

        final String nonce = getNonce();

        if (item instanceof JavaScriptContentHeaderItem) {
            item = new JavaScriptContentWithNonceHeaderItem(
                    ((JavaScriptContentHeaderItem) item).getJavaScript(),
                    ((JavaScriptContentHeaderItem) item).getId()
            ).setNonce(nonce);
        } if (item instanceof JavaScriptReferenceHeaderItem) {
            JavaScriptReferenceHeaderItem headerItem = (JavaScriptReferenceHeaderItem) item;
            item = new JavaScriptReferenceWithNonceHeaderItem(
                    headerItem.getReference(),
                    headerItem.getPageParameters(),
                    headerItem.getId(),
                    headerItem.isDefer(),
                    headerItem.getCharset()
            ).setNonce(nonce);
        } else if (item instanceof JavaScriptUrlReferenceHeaderItem) {
            JavaScriptUrlReferenceHeaderItem headerItem = (JavaScriptUrlReferenceHeaderItem) item;
            item = new JavaScriptUrlReferenceWithNonceHeaderItem(
                    headerItem.getUrl(),
                    headerItem.getId(),
                    headerItem.isDefer(),
                    headerItem.getCharset()
            ).setNonce(nonce);
        } else if (item instanceof OnDomReadyHeaderItem) {
            OnDomReadyHeaderItem headerItem = (OnDomReadyHeaderItem) item;
            item = new OnDomReadyHeaderWithNonceItem(headerItem.getJavaScript()).setNonce(nonce);
        } else if (item instanceof OnLoadHeaderItem) {
            OnLoadHeaderItem headerItem = (OnLoadHeaderItem) item;
            item = new OnLoadWithNonceHeaderItem(headerItem.getJavaScript()).setNonce(nonce);
        } else if (item instanceof CssContentHeaderItem) {
            CssContentHeaderItem headerItem = (CssContentHeaderItem) item;
            item = new CssContentHeaderWithNonceItem(headerItem.getCss(), headerItem.getId()).setNonce(nonce);
        } else if (item instanceof CssReferenceHeaderItem) {
            CssReferenceHeaderItem headerItem = (CssReferenceHeaderItem) item;
            item = new CssReferenceWithNonceHeaderItem(
                    headerItem.getReference(),
                    headerItem.getPageParameters(),
                    headerItem.getMedia(),
                    headerItem.getRel()
            ).setNonce(nonce);
        } else if (item instanceof CssUrlReferenceHeaderItem) {
            CssUrlReferenceHeaderItem headerItem = (CssUrlReferenceHeaderItem) item;
            item = new CssUrlReferenceWithNonceHeaderItem(
                    headerItem.getUrl(),
                    headerItem.getMedia(),
                    headerItem.getRel()
            ).setNonce(nonce);
        }

        super.render(item);
    }

    protected abstract String getNonce();

    protected static void renderScriptReferenceHeaderItem(
            Response response,
            final CharSequence url,
            final String id,
            boolean defer,
            String charset,
            boolean async,
            String nonce
    ) {
        Args.notEmpty(url, "url");
        Args.notEmpty(nonce, "nonce");
        boolean isAjax = RequestCycle.get().find(IPartialPageRequestHandler.class).isPresent();
        // the url needs to be escaped when Ajax, because it will break the Ajax Response XML (WICKET-4777)
        CharSequence escapedUrl = isAjax ? Strings.escapeMarkup(url): url;

        HeaderItemAttributeMap attributes = new HeaderItemAttributeMap();
        attributes.add(HeaderItemAttribute.SCRIPT_SRC, String.valueOf(escapedUrl));
        attributes.add(HeaderItemAttribute.TYPE, "text/javascript");
        if (id != null) {
            attributes.add(HeaderItemAttribute.ID, id);
        }
        if (defer) {
            attributes.add(HeaderItemAttribute.SCRIPT_DEFER, "defer");
        }
        if (async) {
            attributes.add(HeaderItemAttribute.SCRIPT_ASYNC, "async");
        }
        attributes.add(HeaderItemAttribute.CSP_NONCE, nonce);
        if (!Strings.isEmpty(charset)) {
            attributes.add("charset", charset);
        }

        JavaScriptUtils.writeJavaScriptUrl(response, attributes);
    }

    protected static void renderCssReferenceHeaderItem(
            Response response, String id, String url, String media, String rel, String nonce
    ) {
        Args.notEmpty(url, "url");

        HeaderItemAttributeMap attributes = new HeaderItemAttributeMap();
        if (Strings.isEmpty(rel) == false)
        {
            attributes.add(HeaderItemAttribute.LINK_REL, rel);
        } else {
            attributes.add(HeaderItemAttribute.LINK_REL, "stylesheet");
        }

        attributes.add(HeaderItemAttribute.LINK_HREF, String.valueOf(Strings.escapeMarkup(url)));

        if (Strings.isEmpty(media) == false)
        {
            attributes.add(HeaderItemAttribute.LINK_MEDIA, media.toString());
        }
        if (Strings.isEmpty(id) == false)
        {
            attributes.add(HeaderItemAttribute.ID, id);
        }

        attributes.add(HeaderItemAttribute.CSP_NONCE, nonce);

        CssUtils.writeLinkUrl(response, attributes);

        response.write("\n");
    }

    private static class JavaScriptContentWithNonceHeaderItem extends JavaScriptContentHeaderItem {

        private String nonce;

        public JavaScriptContentWithNonceHeaderItem(CharSequence javaScript, String id) {
            super(javaScript, id, null);
        }

        @Override
        public void render(Response response) {
            String id = getId();
            HeaderItemAttributeMap attributes = new HeaderItemAttributeMap();
            attributes.add(HeaderItemAttribute.CSP_NONCE, this.nonce);
            if (id != null) {
                attributes.add(HeaderItemAttribute.ID, id);
            }
            JavaScriptUtils.writeJavaScript(response, getJavaScript(), attributes);
        }

        public JavaScriptContentWithNonceHeaderItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }
    }

    private static class JavaScriptReferenceWithNonceHeaderItem extends JavaScriptReferenceHeaderItem {

        private String nonce;

        public JavaScriptReferenceWithNonceHeaderItem(ResourceReference reference, PageParameters pageParameters, String id, boolean defer, String charset) {
            super(reference, pageParameters, id, defer, charset, null);
        }

        @Override
        public void render(Response response) {
            renderScriptReferenceHeaderItem(response, getUrl(), getId(), isDefer(), getCharset(), isAsync(), nonce);
        }

        public JavaScriptReferenceWithNonceHeaderItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }

    }

    private static class JavaScriptUrlReferenceWithNonceHeaderItem extends JavaScriptUrlReferenceHeaderItem {

        private String nonce;

        public JavaScriptUrlReferenceWithNonceHeaderItem(String url, String id, boolean defer, String charset) {
            super(url, id, defer, charset, null);
        }

        @Override
        public void render(Response response) {
            renderScriptReferenceHeaderItem(response, getUrl(), getId(), isDefer(), getCharset(), isAsync(), nonce);
        }

        public JavaScriptUrlReferenceWithNonceHeaderItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }
    }

    private static class OnDomReadyHeaderWithNonceItem extends OnDomReadyHeaderItem {

        private String nonce;

        public OnDomReadyHeaderWithNonceItem(CharSequence javaScript) {
            super(javaScript);
        }

        @Override
        public void render(Response response) {
            HeaderItemAttributeMap attributes = new HeaderItemAttributeMap();
            attributes.add(HeaderItemAttribute.CSP_NONCE, this.nonce);
            attributes.add(HeaderItemAttribute.TYPE, "text/javascript");
            CharSequence js = getJavaScript();
            if (!Strings.isEmpty(js))
            {
                JavaScriptUtils.writeJavaScript(response, "Wicket.Event.add(window, \"domready\", " +
                        "function(event) { " + js + ";});", attributes);
            }
        }

        public OnDomReadyHeaderWithNonceItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }
    }

    private static class OnLoadWithNonceHeaderItem extends OnLoadHeaderItem {

        private String nonce;

        public OnLoadWithNonceHeaderItem(CharSequence javaScript)
        {
            super(javaScript);
        }

        @Override
        public void render(Response response) {
            HeaderItemAttributeMap attributes = new HeaderItemAttributeMap();
            attributes.add(HeaderItemAttribute.CSP_NONCE, this.nonce);
            attributes.add(HeaderItemAttribute.TYPE, "text/javascript");
            CharSequence js = getJavaScript();
            if (!Strings.isEmpty(js))
            {
                JavaScriptUtils.writeJavaScript(response, "Wicket.Event.add(window, \"load\", " +
                        "function(event) { " + js + ";});", attributes);
            }
        }

        public OnLoadWithNonceHeaderItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }

    }

    public static class CssContentHeaderWithNonceItem extends CssContentHeaderItem {

        private String nonce;

        public CssContentHeaderWithNonceItem(CharSequence css, String id) {
            super(css, id, null);
        }

        @Override
        public void render(Response response) {
            HeaderItemAttributeMap attributes = new HeaderItemAttributeMap();
            attributes.add(HeaderItemAttribute.ID, getId());
            attributes.add(HeaderItemAttribute.CSP_NONCE, this.nonce);
            CssUtils.writeCss(response, getCss(), attributes);
        }

        public CssContentHeaderWithNonceItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }
    }

    public static class CssReferenceWithNonceHeaderItem extends CssReferenceHeaderItem {

        private String nonce;

        public CssReferenceWithNonceHeaderItem(ResourceReference reference, PageParameters pageParameters, String media, String rel) {
            super(reference, pageParameters, media, rel);
        }

        @Override
        public void render(Response response) {
            renderCssReferenceHeaderItem(response, getId(), getUrl(), getMedia(), getRel(), nonce);
        }

        public CssReferenceWithNonceHeaderItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }

    }

    public static class CssUrlReferenceWithNonceHeaderItem extends CssUrlReferenceHeaderItem {

        private String nonce;

        public CssUrlReferenceWithNonceHeaderItem(String url, String media, String rel) {
            super(url, media, null, rel);
        }

        @Override
        public void render(Response response) {
            renderCssReferenceHeaderItem(response, getId(), getUrl(), getMedia(), getRel(), nonce);
        }

        public CssUrlReferenceWithNonceHeaderItem setNonce(String nonce) {
            Args.notNull(nonce, "nonce");
            this.nonce = nonce;
            return this;
        }

    }

}
