package wicked.markup.parser;

import java.io.InputStream;

import wicked.markup.Markup;

public interface IMarkupParser {
	Markup parse (InputStream markupStream);
}
