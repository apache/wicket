package wicket.spring.injection.annot;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import wicket.Session;
import wicket.application.IComponentInstantiationListener;
import wicket.injection.ComponentInjector;
import wicket.injection.web.InjectorHolder;
import wicket.model.Model;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.portlet.PortletApplication;
import wicket.spring.ISpringContextLocator;

/**
 * {@link IComponentInstantiationListener} that injects component properties
 * annotated with {@link SpringBean} annotations.
 * 
 * To install in yourapplication.init() call
 * <code>addComponentInstantiationListener(new SpringComponentInjector(this));</code>
 * 
 * Non-wicket components such as {@link Session}, {@link Model}, and any other
 * pojo can be injected by calling
 * <code>InjectorHolder.getInjector().inject(this)</code> in their
 * constructor.
 * 
 * @author ivaynberg
 * 
 */
public class SpringComponentInjector extends ComponentInjector {

	/**
	 * Constructor for web appliactions
	 * 
	 * @param webapp
	 */
	public SpringComponentInjector(WebApplication webapp) {
		// locate spring's application context ...
		ServletContext sc = webapp.getWicketServlet().getServletContext();

		final ApplicationContext applicationContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(sc);

		// ... stick it into a locator ...
		ISpringContextLocator locator = new ISpringContextLocator() {

			private static final long serialVersionUID = 1L;

			public ApplicationContext getSpringContext() {
				return applicationContext;
			}

		};

		// ... and create and register the annotation aware injector
		InjectorHolder.setInjector(new AnnotSpringInjector(locator));
	}

	/**
	 * Constructor for portlet applications
	 * 
	 * @param portletapp
	 */
	public SpringComponentInjector(PortletApplication portletapp) {
		throw new IllegalStateException("THIS IS NOT YET SUPPORTED");
		// FIXME add support for resolving app context through the portlet app
	}

}
