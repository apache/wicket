package wicket.spring.injection.annot;

import wicket.injection.ComponentInjector;
import wicket.injection.web.InjectorHolder;
import wicket.spring.SpringWebApplication;

/**
 * Convinience subclass of {@link SpringWebApplication} that puts an instance of
 * {@link AnnotSpringInjector} into the {@link InjectorHolder} when the
 * application is initialized.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @deprecated instead in application.init() do
 *             <code>addComponentInstantiationListener(new SpringComponentInjector(this));</code>
 * 
 */
public abstract class AnnotSpringWebApplication extends SpringWebApplication {

	protected void internalInit() {
		super.internalInit();
		InjectorHolder.setInjector(new AnnotSpringInjector(
				getSpringContextLocator()));
		addComponentInstantiationListener(new ComponentInjector());
	}

}
