package wicket.spring.injection;

import wicket.extensions.injection.ComponentInjector;
import wicket.extensions.injection.InjectorHolder;
import wicket.spring.SpringWebApplication;

/**
 * Convinience subclass of {@link SpringWebApplication} that puts an instance of
 * {@link AnnotSpringInjector} into the {@link InjectorHolder} when the
 * application is initialized.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AnnotSpringWebApplication extends SpringWebApplication
{

	protected void internalInit()
	{
		super.internalInit();
		InjectorHolder.setInjector(new AnnotSpringInjector(getSpringContextLocator()));
		addComponentInstantiationListener(new ComponentInjector());
	}
	

}
