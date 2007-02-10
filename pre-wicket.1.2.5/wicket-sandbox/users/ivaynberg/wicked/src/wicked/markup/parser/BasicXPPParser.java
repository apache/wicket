package wicked.markup.parser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import wicked.markup.ComponentFragment;
import wicked.markup.IFragmentCollector;
import wicked.markup.Markup;
import wicked.markup.StaticFragment;
import wicked.markup.Tag;

public class BasicXPPParser implements IMarkupParser {

	public Markup parse(InputStream markupStream) {
		try {
			MXParser xpp = new MXParser();
			xpp.setInput(markupStream, null);

			Markup markup = new Markup();

			EventListener listener = new Handler(markup);

			listener.onStartDocument();

			Map<String, String> attrs;

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					attrs = new HashMap();
					for (int i = 0; i < xpp.getAttributeCount(); i++) {
						String name = xpp.getAttributeName(i);
						String prefix = xpp.getAttributePrefix(i);
						if (prefix != null) {
							name = prefix + name;
						}
						attrs.put(name, xpp.getAttributeValue(i));
					}
					String name = xpp.getName();
					String prefix = xpp.getPrefix();
					if (prefix != null) {
						name = prefix + name;
					}
					final boolean empty = xpp.isEmptyElementTag();
					listener.onStartElement(name, attrs, empty);

				} else if (eventType == XmlPullParser.END_TAG) {
					String name = xpp.getName();
					String prefix = xpp.getPrefix();
					if (prefix != null) {
						name = prefix + name;
					}
					listener.onEndElement(name);
				} else if (eventType == XmlPullParser.TEXT) {
					listener.onText(xpp.getText());
				} else if (eventType == XmlPullParser.IGNORABLE_WHITESPACE) {
					listener.onText(xpp.getText());
				}
				eventType = xpp.next();
			}

			listener.onEndDocument();

			markup.init();
			return markup;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public interface EventListener {
		// TODO currently we lose order of attributes as they appear in markup,
		// is it important?
		/** name is the fully qualified name */
		void onStartElement(String name, Map<String, String> attrs,
				boolean empty);

		/**
		 * 
		 * @param name
		 *            fully qualified name of element
		 */
		void onEndElement(String name);

		void onText(String text);

		void onEndDocument();

		void onStartDocument();
	}

	public class Handler implements EventListener {
		private StringBuilder textBuffer = new StringBuilder();

		private Stack<Boolean> componentTags = new Stack<Boolean>();

		private Stack<IFragmentCollector> fragments = new Stack<IFragmentCollector>();
		private Stack<Boolean> emptyTags=new Stack<Boolean>();
		
		public Handler(Markup markup) {
			fragments.push(markup);
		}

		public void onStartElement(String name, Map<String, String> attrs,
				boolean empty) {

			if (hasWicketId(attrs)) {

				recordStaticMarkup();

				Tag tag = createTag(name, attrs, empty);
				componentTags.push(true);
				startComponent(tag);

			} else {
				printTagToTextBuffer(name, attrs, empty);
				componentTags.push(false);
			}
			
			emptyTags.push(empty);
		}

		public void onEndElement(String name) {
			if (componentTags.peek() == true) {

				recordStaticMarkup();

				endComponent();

			} else {
				if (!emptyTags.peek()) {
					textBuffer.append("</").append(name).append(">");
				}
			}

			componentTags.pop();
			
			emptyTags.pop();

		}

		public void onText(String text) {
			if (text.startsWith("[testpanel]")) {
				int a = 2;
				int b = a + 2;
			}
			textBuffer.append(text);

		}

		public void onEndDocument() {
			recordStaticMarkup();

		}

		public void onStartDocument() {

		}

		public void startComponent(Tag tag) {
			ComponentFragment frag = new ComponentFragment(tag);
			fragments.peek().addFragment(frag);
			fragments.push(frag);
		}

		public void endComponent() {
			fragments.pop();
		}

		private void recordStaticMarkup() {
			if (textBuffer.length() > 0) {
				fragments.peek().addFragment(
						new StaticFragment(textBuffer.toString()));
				textBuffer = new StringBuilder();
			}
		}

		private Tag createTag(String name, Map<String, String> attributes,
				boolean empty) {
			return new Tag(name, attributes, empty);
		}

		private boolean hasWicketId(Map<String, String> attrs) {
			for (Map.Entry<String, String> attr : attrs.entrySet()) {
				final String name = attr.getKey();
				if (name.equals("wicket:id")) {
					return true;
				}
			}
			return false;
		}

		private void printTagToTextBuffer(String name, Map<String, String> attrs, boolean empty) {
			textBuffer.append("<").append(name);

			for (Map.Entry<String, String> attr : attrs.entrySet()) {
				textBuffer.append(" ").append(attr.getKey()).append("=\"")
						.append(attr.getValue()).append("\"");
			}
			if (empty) {
				textBuffer.append("/>");
			} else {
				textBuffer.append(">");
			}
		}

	}

}
