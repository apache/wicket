package wicket.extensions.ajax.markup.html;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;
import wicket.model.Model;


/**
 * DO NOT USE YET, WORK IN PROGRESS
 * @author ivaynberg
 *
 */

public class AjaxCollapsableBorder extends Border
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final WebMarkupContainer header;

	/**
	 * Constructor.
	 * 
	 * @param id Component id
	 */
	public AjaxCollapsableBorder(String id)
	{
		super(id, new Model());
		setCollapsed(true);
		setOutputMarkupId(true);
		
		header = new WebMarkupContainer("header");
		add(header);
		header.add(new AjaxFallbackLink("toggle-link") {

			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				setCollapsed(!isCollapsed());
				if (target!=null) {
					target.addComponent(AjaxCollapsableBorder.this);
				}
			}
			
		});
	}

	/**
	 * @param collapsed True if collapsed
	 */
	public final void setCollapsed(boolean collapsed)
	{
		setModelObject(Boolean.valueOf(collapsed));
		setBorderBodyVisible(!collapsed);
	}

	/**
	 * @return True if collapsed
	 */
	public final boolean isCollapsed()
	{
		return Boolean.TRUE.equals(getModelObject());
	}


	/**
	 * User: nick
	 * Date: Mar 8, 2006
	 * Time: 2:51:17 PM
	 */
	public class AjaxCollapsableDiv extends WebMarkupContainer {

		/**
		 * Constructor.
		 * 
		 * @param id Component id
		 */
		public AjaxCollapsableDiv(String id)
		{
			super(id);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	    /*	private static final long serialVersionUID = 1L;

		static Log log = LogFactory.getLog(AjaxCollapsableDiv.class);

	    private WebMarkupContainer toggleImage;
	    private final AjaxPanel body;
	    private boolean isExpanded = false;
	    private String title;

	    public AjaxCollapsableDiv(final String id, AjaxPanel ap, String title) {
	        super(id);
	        this.body = ap;
	        this.title = title;
	        setOutputMarkupId(true);
	        init();
	    }

	    protected void init() {
	        toggleImage = new WebMarkupContainer("toggleImage");
	        toggleImage.setOutputMarkupId(true);
	        add(toggleImage);

	        body.setOutputMarkupId(true);
	        add(body);

	        AjaxLink link = new AjaxLink("expand") {
	    		private static final long serialVersionUID = 1L;

				public void onClick(AjaxRequestTarget target) {
	                target.addComponent(AjaxCollapsableDiv.this);
	                expanded(!isExpanded);
	            }
	        };
	        link.add(new Label("name", title));
	        add(link);

	        final Cookie cookie = getCookie();
	        boolean shouldExpand = false;
	        String[] divs = getCookieValue(cookie).split(",");

	        for (String div : divs) {
	            shouldExpand = (getId().equals(div));
	            if (shouldExpand) {
	                break;
	            }
	        }

	        expanded(shouldExpand);
	    }

	    protected void expanded(boolean shouldExpand) {
	        if (shouldExpand) {
	            toggleImage.add(new AttributeModifier("src", true,
	                new Model(CollapsableDiv.EXPANDED_IMAGE)));
	            body.addContent();
	            body.setVisible(true);
	            body.add(new AttributeModifier("class", true,
	                new Model(CollapsableDiv.EXPANDED_STYLE)));
	            isExpanded = true;
	            addToCookie();
	        }
	        else {
	            toggleImage.add(new AttributeModifier("src", true,
	                new Model(CollapsableDiv.COLLAPSED_IMAGE)));
	            body.setVisible(false);
	            body.add(new AttributeModifier("class", true,
	                new Model(CollapsableDiv.COLLAPSED_STYLE)));
	            isExpanded = false;
	            removeFromCookie();
	        }
	    }

	    protected Cookie getCookie() {
	        WebRequest webRequest = (WebRequest) getRequest();
	        Cookie[] cookies = webRequest.getCookies();
	        for (Cookie cookie : cookies) {
	            if (cookie.getName().equals(CollapsableDiv.DIVS_TO_EXPAND)) {
	                log.debug("Returning preexisting cookie: " + cookie.getMaxAge());
	                return cookie;
	            }
	        }

	        Cookie cookie = new Cookie(CollapsableDiv.DIVS_TO_EXPAND, "");
	        cookie.setMaxAge(CollapsableDiv.EXPIRATION);

	        setCookie(cookie);
	        return cookie;
	    }

	    protected void addToCookie() {
	        Cookie cookie = getCookie();
	        String[] values = getCookieValue(cookie).split(",");
	        boolean contains = false;
	        for (String s : values) {
	            if (s.equals(getId())) {
	                contains = true;
	            }
	        }

	        if (!contains) {
	            StringBuilder b = new StringBuilder();
	            for (String s : values) {
	                b.append(s).append(",");
	            }
	            b.append(getId());
	            cookie.setValue(URLEncoder.encode(b.toString()));
	            setCookie(cookie);
	        }
	    }

	    protected void removeFromCookie() {
	        Cookie cookie = getCookie();
	        String[] values = getCookieValue(cookie).split(",");
	        StringBuilder b = new StringBuilder();
	        for (int i = 0; i < values.length; i++) {
	            if (!values[i].equals(getId())) {
	                b.append(values[i]);
	                if (i < (values.length -1)) {
	                    b.append(",");
	                }
	            }
	        }

	        cookie.setValue(URLEncoder.encode(b.toString()));
	        setCookie(cookie);
	    }

	    private String getCookieValue(Cookie c) {
	        return URLDecoder.decode(c.getValue());
	    }

	    private void setCookie(Cookie c) {
	        WebResponse webResponse = (WebResponse) getResponse();
	        webResponse.addCookie(c);
	    }
	    
	    */
	}
	
}
