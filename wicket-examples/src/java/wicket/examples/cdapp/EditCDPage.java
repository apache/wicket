/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.cdapp;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketRuntimeException;
import wicket.contrib.data.model.PersistentObjectModel;
import wicket.contrib.data.model.hibernate.HibernateObjectModel;
import wicket.contrib.data.util.hibernate.HibernateHelper;
import wicket.contrib.data.util.hibernate.HibernateHelperSessionDelegate;
import wicket.examples.WicketExamplePage;
import wicket.examples.cdapp.model.CD;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.markup.html.form.validation.IntegerValidator;
import wicket.markup.html.form.validation.LengthValidator;
import wicket.markup.html.form.validation.RequiredValidator;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.AbstractDetachableModel;
import wicket.model.IModel;


/**
 * Page for editing CD's.
 * 
 * @author Eelco Hillenius
 */
public final class EditCDPage extends WicketExamplePage
{
	/** Logger. */
	private static Log log = LogFactory.getLog(SearchCDPage.class);

	/** model for one cd. */
	private final PersistentObjectModel cdModel;

	/** search page to navigate back to. */
	private final SearchCDPage searchCDPage;

	/**
	 * form for detail editing.
	 */
	private final class DetailForm extends Form
	{
		/**
		 * Construct.
		 * 
		 * @param name component name
		 * @param validationErrorHandler error handler
		 * @param cdModel the model
		 */
		public DetailForm(String name, IValidationFeedback validationErrorHandler,
				PersistentObjectModel cdModel)
		{
			super(name, cdModel, validationErrorHandler);
			TextField titleField = new TextField("title", cdModel, "title");
			titleField.add(RequiredValidator.getInstance());
			titleField.add(LengthValidator.max(50));
			add(titleField);
			TextField performersField = new TextField("performers", cdModel, "performers");
			performersField.add(RequiredValidator.getInstance());
			performersField.add(LengthValidator.max(50));
			add(performersField);
			TextField labelField = new TextField("label", cdModel, "label");
			labelField.add(LengthValidator.max(50));
			add(labelField);
			TextField yearField = new TextField("year", cdModel, "year");
			yearField.add(RequiredValidator.getInstance());
			yearField.add(IntegerValidator.POSITIVE_INT);
			add(yearField);
			add(new Link("cancelButton")
			{
				public void onClick()
				{
					getRequestCycle().setPage(searchCDPage);
				}
			});
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit()
		{
			CD cd = (CD)getModelObject();
			boolean isNew = (cd.getId() == null);
			// note that, as we used the Ognl property model, the fields are
			// allready updated
			Session session = null;
			Transaction tx = null;
			try
			{
				session = HibernateHelper.getSession();
				tx = session.beginTransaction();
				session.saveOrUpdate(cd);
				tx.commit();
				// set message for search page to display on next rendering
				searchCDPage.setInfoMessageForNextRendering("cd " + cd.getTitle() + " saved");
				searchCDPage.modelChangedStructure(); // force reload of data
				if (isNew)
				{
					// if it was a new cd, set the search page to page 1
					searchCDPage.setCurrentResultPageToFirst();
				}
				getRequestCycle().setPage(searchCDPage); // navigate back to search page
			}
			catch (HibernateException e)
			{
				try
				{
					tx.rollback();
				}
				catch (HibernateException ex)
				{
					ex.printStackTrace();
				}
				throw new WicketRuntimeException(e);
			}
		}
	}

	/**
	 * Special model for the title header. It returns the CD title if there's a
	 * loaded object (when the id != null) or it returns a special string in case
	 * there is no loaded object (if id == null).
	 */
	private static class TitleModel extends AbstractDetachableModel
	{
		/** decorated model; provides the current id. */
		private final PersistentObjectModel cdModel;

		/**
		 * Construct.
		 * 
		 * @param cdModel the model to decorate
		 */
		public TitleModel(PersistentObjectModel cdModel)
		{
			this.cdModel = cdModel;
		}

		/**
		 * @see wicket.model.IModel#setObject(java.lang.Object)
		 */
		public void setObject(Object object)
		{
			cdModel.setObject(object);
		}

		/**
		 * @see AbstractDetachableModel#onAttach()
		 */
		protected void onAttach()
		{
			cdModel.attach();
		}

		/**
		 * @see AbstractDetachableModel#onDetach()
		 */
		protected void onDetach()
		{
			cdModel.detach();
		}

		/**
		 * @see AbstractDetachableModel#onGetObject()
		 */
		protected Object onGetObject()
		{
			if (cdModel.getId() != null) // it is allready persistent
			{
				CD cd = (CD)cdModel.getObject();
				return cd.getTitle();
			}
			else // it is a new cd
			{
				return "<NEW CD>";
			}
		}
	}

	/**
	 * Constructor.
	 * @param searchCDPage the search page to navigate back to
	 * @param id the id of the cd to edit
	 */
	public EditCDPage(final SearchCDPage searchCDPage, Long id)
	{
		this.searchCDPage = searchCDPage;
		cdModel = new HibernateObjectModel(id, CD.class, new HibernateHelperSessionDelegate());
		add(new Label("cdTitle", new TitleModel(cdModel)));
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new DetailForm("detailForm", feedback, cdModel));
	}
	
	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		return cdModel;
	}
}