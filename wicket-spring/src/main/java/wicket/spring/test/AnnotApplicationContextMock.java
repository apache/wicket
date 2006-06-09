package wicket.spring.test;

import wicket.extensions.injection.InjectorHolder;
import wicket.spring.injection.AnnotSpringInjector;

/**
 * Spring application context mock that does all the initialization required to
 * setup an {@link AnnotSpringInjector} that will use this mock context as its
 * source of beans.
 * <p>
 * Example
 * 
 * <pre>
 *  AnnotApplicationContextMock appctx = new AnnotApplicationContextMock();
 *  appctx.putBean(&quot;contactDao&quot;, dao);
 *  
 *  WicketTester app = new WicketTester();
 *  
 *  Page deletePage=new DeleteContactPage(new DummyHomePage(), 10));
 * </pre>
 * 
 * DeleteContactPage will have its dependencies initialized by the
 * {@link AnnotSpringInjector}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AnnotApplicationContextMock extends ApplicationContextMock
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * <p>
	 * Sets up an {@link AnnotSpringInjector} that will use this mock context as
	 * its source of beans
	 * 
	 * 
	 */
	public AnnotApplicationContextMock()
	{
		SpringContextLocatorMock ctxLocator = new SpringContextLocatorMock(this);

		AnnotSpringInjector injector = new AnnotSpringInjector(ctxLocator);

		InjectorHolder.setInjector(injector);
	}
}
