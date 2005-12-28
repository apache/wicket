package wicket.spring.injection.cattr;

import wicket.injection.web.InjectorHolder;
import wicket.spring.SpringWebApplication;

/**
 * Convinience subclass of {@link SpringWebApplication} that puts an instance of
 * {@link CommonsAttributeSpringInjector} into the {@link InjectorHolder} when the
 * application is initialized.
 *
 * @author Karthik Gurumurthy
 *
 */
public class CommonsAttributeSpringWebApplication extends SpringWebApplication
{

	protected void internalInit()
	{
		super.internalInit();
		InjectorHolder.setInjector(new CommonsAttributeSpringInjector(getSpringContextLocator()));
	}

}
