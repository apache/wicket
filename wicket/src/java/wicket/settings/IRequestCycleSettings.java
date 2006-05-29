package wicket.settings;

import java.util.List;

import wicket.IResponseFilter;
import wicket.RequestCycle;
import wicket.markup.html.pages.BrowserInfoPage;
import wicket.protocol.http.WebRequestCycle;
import wicket.settings.IExceptionSettings.UnexpectedExceptionDisplay;

/**
 * Inteface for request related settings
 * <p>
 * <i>bufferResponse </i> (defaults to true) - True if the application should
 * buffer responses. This does require some additional memory, but helps keep
 * exception displays accurate because the whole rendering process completes
 * before the page is sent to the user, thus avoiding the possibility of a
 * partially rendered page.
 * <p>
 * <i>renderStrategy </i>- Sets in what way the render part of a request is
 * handled. Basically, there are two different options:
 * <ul>
 * <li>Direct, ApplicationSettings.ONE_PASS_RENDER. Everything is handled in
 * one physical request. This is efficient, and is the best option if you want
 * to do sophisticated clustering. It does not however, shield you from what is
 * commonly known as the <i>Double submit problem </i></li>
 * <li>Using a redirect. This follows the pattern <a
 * href="http://www.theserverside.com/articles/article.tss?l=RedirectAfterPost"
 * >as described at the serverside </a> and that is commonly known as Redirect
 * after post. Wicket takes it one step further to do any rendering after a
 * redirect, so that not only form submits are shielded from the double submit
 * problem, but also the IRequestListener handlers (that could be e.g. a link
 * that deletes a row). With this pattern, you have two options to choose from:
 * <ul>
 * <li>ApplicationSettings.REDIRECT_TO_RENDER. This option first handles the
 * 'action' part of the request, which is either page construction (bookmarkable
 * pages or the home page) or calling a IRequestListener handler, such as
 * Link.onClick. When that part is done, a redirect is issued to the render
 * part, which does all the rendering of the page and its components. <strong>Be
 * aware </strong> that this may mean, depending on whether you access any
 * models in the action part of the request, that attachement and detachement of
 * some models is done twice for a request.</li>
 * <li>ApplicationSettings.REDIRECT_TO_BUFFER. This option handles both the
 * action- and the render part of the request in one physical request, but
 * instead of streaming the result to the browser directly, it is kept in
 * memory, and a redirect is issue to get this buffered result (after which it
 * is immediately removed). This option currently is the default render
 * strategy, as it shields you from the double submit problem, while being more
 * efficient and less error prone regarding to detachable models.</li>
 * </ul>
 * </li>
 * </ul>
 * Note that this parameter sets the default behavior, but that you can manually
 * set whether any redirecting is done by calling method
 * RequestCycle.setRedirect. Setting the redirect flag when the application is
 * configured to use ONE_PASS_RENDER, will result in a redirect of type
 * REDIRECT_TO_RENDER. When the application is configured to use
 * REDIRECT_TO_RENDER or REDIRECT_TO_BUFFER, setting the redirect flag to false,
 * will result in that request begin rendered and streamed in one pass.
 * <p>
 * More documentation is available about each setting in the setter method for
 * the property.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IRequestCycleSettings
{
	/**
	 * Enumerated type for different ways of handling the render part of
	 * requests.
	 */
	public static enum RenderStrategy {
		/**
		 * All logical parts of a request (the action and render part) are
		 * handled within the same request. To enable a the client side redirect
		 * for a request, users can set the 'redirect' property of
		 * {@link RequestCycle}to true (getRequestCycle.setRedirect(true)),
		 * after which the behavior will be like RenderStragegy
		 * 'REDIRECT_TO_RENDER'.
		 * <p>
		 * This strategy is more efficient than the 'REDIRECT_TO_RENDER'
		 * strategy, and doesn't have some of the potential problems of it, it
		 * also does not solve the double submit problem. It is however the best
		 * option to use when you want to do sophisticated (non-sticky session)
		 * clustering.
		 * </p>
		 */
		ONE_PASS_RENDER,

		/**
		 * All logical parts of a request (the action and render part) are
		 * handled within the same request. To enable a the client side redirect
		 * for a request, users can set the 'redirect' property of
		 * {@link RequestCycle}to true (getRequestCycle.setRedirect(true)),
		 * after which the behavior will be like RenderStragegy
		 * 'REDIRECT_TO_RENDER'.
		 * <p>
		 * This strategy is more efficient than the 'REDIRECT_TO_RENDER'
		 * strategy, and doesn't have some of the potential problems of it, it
		 * also does not solve the double submit problem. It is however the best
		 * option to use when you want to do sophisticated (non-sticky session)
		 * clustering.
		 * </p>
		 */
		REDIRECT_TO_BUFFER,

		/**
		 * The render part of a request (opposed to the 'action part' which is
		 * either the construction of a bookmarkable page or the execution of a
		 * IRequestListener handler) is handled by a seperate request by
		 * issueing a redirect request to the browser. This is commonly known as
		 * the 'redirect after submit' pattern, though in our case, we use it
		 * for GET and POST requests instead of just the POST requests. To
		 * cancel the client side redirect for a request, users can set the
		 * 'redirect' property of {@link RequestCycle}to false
		 * (getRequestCycle.setRedirect(false)).
		 * <p>
		 * This pattern solves the 'refresh' problem. While it is a common
		 * feature of browsers to refresh/ reload a web page, this results in
		 * problems in many dynamic web applications. For example, when you have
		 * a link with an event handler that e.g. deletes a row from a list, you
		 * usually want to ignore refresh requests after that link is clicked
		 * on. By using this strategy, the refresh request only results in the
		 * re-rendering of the page without executing the event handler again.
		 * </p>
		 * <p>
		 * Though it solves the refresh problem, it introduces potential
		 * problems, as the request that is logically one, are actually two
		 * seperate request. Not only is this less efficient, but this also can
		 * mean that within the same request attachement/ detachement of models
		 * is done twice (in case you use models in the bookmarkable page
		 * constructors and IRequestListener handlers). If you use this
		 * strategy, you should be aware of this possibily, and should also be
		 * aware that for one logical request, actually two instances of
		 * RequestCycle are created and processed.
		 * </p>
		 */
		REDIRECT_TO_RENDER
	}

	/**
	 * Adds a response filter to the list. Filters are evaluated in the order
	 * they have been added.
	 * 
	 * @param responseFilter
	 *            The {@link IResponseFilter} that is added
	 */
	void addResponseFilter(IResponseFilter responseFilter);

	/**
	 * @return True if this application buffers its responses
	 */
	boolean getBufferResponse();

	/**
	 * Gets whether Wicket should try to get extensive client info by
	 * redirecting to
	 * {@link BrowserInfoPage a page that polls for client capabilities}. This
	 * method is used by the default implementation of
	 * {@link WebRequestCycle#newClientInfo()}, so if that method is overriden,
	 * there is no guarantee this method will be taken into account.
	 * 
	 * @return Whether to gather extensive client info
	 */
	boolean getGatherExtendedBrowserInfo();

	/**
	 * Gets in what way the render part of a request is handled.
	 * 
	 * @return the render strategy
	 */
	IRequestCycleSettings.RenderStrategy getRenderStrategy();

	/**
	 * @return an unmodifiable list of added response filters, null if none
	 */
	List<IResponseFilter> getResponseFilters();

	/**
	 * In order to do proper form parameter decoding it is important that the
	 * response and the following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for
	 * additional information.
	 * 
	 * @return The request and response encoding
	 */
	String getResponseRequestEncoding();

	/**
	 * @see wicket.settings.IExceptionSettings#getUnexpectedExceptionDisplay()
	 * 
	 * @return UnexpectedExceptionDisplay
	 */
	UnexpectedExceptionDisplay getUnexpectedExceptionDisplay();

	/**
	 * @param bufferResponse
	 *            True if this application should buffer responses.
	 */
	void setBufferResponse(boolean bufferResponse);

	/**
	 * Sets whether Wicket should try to get extensive client info by
	 * redirecting to
	 * {@link BrowserInfoPage a page that polls for client capabilities}. This
	 * method is used by the default implementation of
	 * {@link WebRequestCycle#newClientInfo()}, so if that method is overriden,
	 * there is no guarantee this method will be taken into account.
	 * 
	 * @param gatherExtendedBrowserInfo
	 *            Whether to gather extensive client info
	 */
	void setGatherExtendedBrowserInfo(boolean gatherExtendedBrowserInfo);

	/**
	 * Sets in what way the render part of a request is handled. Basically,
	 * there are two different options:
	 * <ul>
	 * <li>Direct, ApplicationSettings.ONE_PASS_RENDER. Everything is handled
	 * in one physical request. This is efficient, and is the best option if you
	 * want to do sophisticated clustering. It does not however, shield you from
	 * what is commonly known as the <i>Double submit problem </i></li>
	 * <li>Using a redirect. This follows the pattern <a
	 * href="http://www.theserverside.com/articles/article.tss?l=RedirectAfterPost"
	 * >as described at the serverside </a> and that is commonly known as
	 * Redirect after post. Wicket takes it one step further to do any rendering
	 * after a redirect, so that not only form submits are shielded from the
	 * double submit problem, but also the IRequestListener handlers (that could
	 * be e.g. a link that deletes a row). With this pattern, you have two
	 * options to choose from:
	 * <ul>
	 * <li>ApplicationSettings.REDIRECT_TO_RENDER. This option first handles
	 * the 'action' part of the request, which is either page construction
	 * (bookmarkable pages or the home page) or calling a IRequestListener
	 * handler, such as Link.onClick. When that part is done, a redirect is
	 * issued to the render part, which does all the rendering of the page and
	 * its components. <strong>Be aware </strong> that this may mean, depending
	 * on whether you access any models in the action part of the request, that
	 * attachement and detachement of some models is done twice for a request.
	 * </li>
	 * <li>ApplicationSettings.REDIRECT_TO_BUFFER. This option handles both the
	 * action- and the render part of the request in one physical request, but
	 * instead of streaming the result to the browser directly, it is kept in
	 * memory, and a redirect is issue to get this buffered result (after which
	 * it is immediately removed). This option currently is the default render
	 * strategy, as it shields you from the double submit problem, while being
	 * more efficient and less error prone regarding to detachable models.</li>
	 * </ul>
	 * Note that this parameter sets the default behavior, but that you can
	 * manually set whether any redirecting is done by calling method
	 * RequestCycle.setRedirect. Setting the redirect flag when the application
	 * is configured to use ONE_PASS_RENDER, will result in a redirect of type
	 * REDIRECT_TO_RENDER. When the application is configured to use
	 * REDIRECT_TO_RENDER or REDIRECT_TO_BUFFER, setting the redirect flag to
	 * false, will result in that request begin rendered and streamed in one
	 * pass.
	 * 
	 * @param renderStrategy
	 *            the render strategy that should be used by default.
	 */
	void setRenderStrategy(IRequestCycleSettings.RenderStrategy renderStrategy);

	/**
	 * In order to do proper form parameter decoding it is important that the
	 * response and the following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for
	 * additional information.
	 * 
	 * Default encoding: UTF-8
	 * 
	 * @param responseRequestEncoding
	 *            The request and response encoding to be used.
	 */
	void setResponseRequestEncoding(final String responseRequestEncoding);

	/**
	 * @see wicket.settings.IExceptionSettings#setUnexpectedExceptionDisplay(wicket.settings.Settings.UnexpectedExceptionDisplay)
	 * 
	 * @param unexpectedExceptionDisplay
	 */
	void setUnexpectedExceptionDisplay(final UnexpectedExceptionDisplay unexpectedExceptionDisplay);
}