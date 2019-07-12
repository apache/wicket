package org.apache.wicket.util.value;

import org.apache.wicket.util.lang.Args;

/**
 * Some common header item attributes.
 * This is not a complete list of all possible attributes.
 */
public enum HeaderItemAttribute {

    ID("id"),
    TYPE("type"),

    // script (JavaScript) attributes see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script

    SCRIPT_SRC("src"),
    SCRIPT_DEFER("defer"),
    SCRIPT_ASYNC("async"),
    SCRIPT_NOMODULE("nomodule"),
    SCRIPT_REFERRERPOLICY("referrerpolicy"),

    // link (CSS) attributes

    LINK_HREF("href"),
    LINK_MEDIA("media"),
    LINK_REL("rel"),

    // Content Security Policy attributes, see https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/script-src
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/style-src

    CSP_NONCE("nonce"),

    // SRI Subresource integrity attributes, see https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity

    SRI_INTEGRITY("integrity"),
    SRI_CROSSORIGIN("crossorigin");

    private String name;

    HeaderItemAttribute(String name) {
        Args.notNull(name, "name");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
