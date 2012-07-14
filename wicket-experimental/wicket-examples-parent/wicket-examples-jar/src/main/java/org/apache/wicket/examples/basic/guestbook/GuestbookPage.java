package org.apache.wicket.examples.basic.guestbook;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.bootstrap.Bootstrap;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

@SuppressWarnings("serial")
public class GuestbookPage extends WebPage {
	private static final CopyOnWriteArrayList<Comment> comments = new CopyOnWriteArrayList<Comment>();

	private final TextArea<String> commentField;
	private final TextField<String> authorField;

	public GuestbookPage() {
		add(new ListView<Comment>("comments", getCommentsModel()) {
			@Override
			protected void populateItem(ListItem<Comment> item) {
				item.add(new Label("author", item.getModelObject().getAuthor()));
				item.add(new Label("comment", item.getModelObject().getText()));
			}
		});

		Form<Void> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				String author = authorField.getModelObject();
				String text = commentField.getModelObject();

				// clear the comment field
				commentField.setModelObject("");

				comments.add(0, new Comment(author, text));
			}
		};
		add(form);
		form.add(authorField = new TextField<String>("author", Model.of("")));
		form.add(commentField = new TextArea<String>("comment", Model.of("")));
	}

	public IModel<List<Comment>> getCommentsModel() {
		IModel<List<Comment>> commentsModel = new LoadableDetachableModel<List<Comment>>() {
			@Override
			protected List<Comment> load() {
				return comments;
			}
		};
		return commentsModel;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		// Clean up comments for comments older than one hour
		for (Comment comment : comments) {
			Time createdAt = Time.valueOf(comment.getCreatedAt());
			if (Duration.elapsed(createdAt).seconds() > 10) {
				comments.remove(comment);
			}
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
//		Bootstrap.renderHead(response);
//		response.render(CssHeaderItem.forCSS(
//				"body { padding-top: 60px; padding-bottom: 40px; }",
//				"custom-wicket-examples"));
	}
}
