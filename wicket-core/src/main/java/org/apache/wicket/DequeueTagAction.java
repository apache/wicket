package org.apache.wicket;

public enum DequeueTagAction
{
	/** dequeue the tag */
	DEQUEUE,
	/** skip this tag and all its children */
	SKIP,
	/** ignore this tag, skip it but do not skip its children */
	IGNORE;
}
