package wicked.markup.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wicked.markup.ComponentFragment;
import wicked.markup.IFragmentCollector;
import wicked.markup.Markup;
import wicked.markup.StaticFragment;
import wicked.markup.Tag;

public class BasicSAXParser implements IMarkupParser {

	public Markup parse(InputStream markupStream) {

		try {
			SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
			Markup markup=new Markup();
			parser.parse(markupStream, new Handler(markup));
			markup.init();
			return markup;
			
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
	}
	
	public class Handler extends DefaultHandler {
		private StringBuilder textBuffer=new StringBuilder();
		private Stack<Boolean> componentTags=new Stack<Boolean>();
		
		private Stack<IFragmentCollector> fragments=new Stack<IFragmentCollector>();
		
		public Handler(Markup markup) {
			fragments.push(markup);
		}
		
		public void startComponent(Tag tag) {
			ComponentFragment frag=new ComponentFragment(tag);
			fragments.peek().addFragment(frag);
			fragments.push(frag);
		}
		
		public void endComponent() {
			fragments.pop();
		}

		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			if (hasWicketId(attributes)) {

				recordStaticMarkup();
				
				Tag tag=createTag(qName, attributes);
				componentTags.push(true);
				startComponent(tag);
				
			} else {
				printTagToTextBuffer(qName, attributes);
				componentTags.push(false);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (componentTags.peek()==true) {

				recordStaticMarkup();
				
				endComponent();
				
			} else {
				textBuffer.append("</").append(qName).append(">");
			}
			
			componentTags.pop();
		}
		
		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			textBuffer.append(ch, start, length);
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			textBuffer.append(ch, start, length);
		}

		@Override
		public void endDocument() throws SAXException {
			recordStaticMarkup();
		}
		
		private void recordStaticMarkup() {
			if (textBuffer.length()>0) {
				fragments.peek().addFragment(new StaticFragment(textBuffer.toString()));
				textBuffer=new StringBuilder();
			}
		}

		
		private Tag createTag(String name, Attributes attributes) {
			Tag tag=new Tag(name);
			for (int i=0;i<attributes.getLength();i++) {
				tag.getAttributes().put(attributes.getQName(i), attributes.getValue(i));
			}
			return tag;
		}
		
		private boolean hasWicketId(Attributes attributes) {
			for (int i=0;i<attributes.getLength();i++) {
				if (attributes.getQName(i).equals("wicket:id")) {
					return true;
				}
			}
			return false;
		}
		
		private void printTagToTextBuffer(String name, Attributes attributes) {
			textBuffer.append("<").append(name);
			for (int i=0;i<attributes.getLength();i++) {
				textBuffer.append(" ")
				.append(attributes.getQName(i))
				.append("=\"")
				.append(attributes.getValue(i))
				.append("\"");
			}
			textBuffer.append(">");
		}

		
	}

}
