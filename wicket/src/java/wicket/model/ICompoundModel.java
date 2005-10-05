/**
 * 
 */
package wicket.model;

/**
 * This is a marker interface for models that can be used as a shared/compound model
 * for multiply components. 
 * 
 * If a model implements this interface then you can give the parent container this model
 * and all the child components will also get and then set that model as there own.
 * 
 * <pre>
 * 	Form form = new Form("form", new ModelImplementingICompoundModel());
 * 	form.add(new TextField("textfield"));
 * </pre>
 * 
 * 
 * @author jcompagner
 *
 */
public interface ICompoundModel extends IModel
{

}
