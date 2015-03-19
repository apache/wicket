/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.HtmlSpecialTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.IXmlPullParser.HttpTagType;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skip duplicated mismatched markup inside conditional comments
 * 
 * @author Pedro Santos
 * @author Juergen Donnerstag
 */
public class ConditionalCommentFilter extends AbstractMarkupFilter
{
	private static final Logger log = LoggerFactory.getLogger(ConditionalCommentFilter.class);

	private Queue<ComponentTag> queue = new LinkedList<ComponentTag>();
	private Map<Integer, Set<String>> skipByLevelMap = Generics.newHashMap();
	private int level;

	@Override
	protected MarkupElement onSpecialTag(final HtmlSpecialTag tag) throws ParseException
	{
		if (tag.getHttpTagType() == HttpTagType.CONDITIONAL_COMMENT)
		{

		}
		return tag;
	}

// public MarkupElement nextTag() throws ParseException
// {
// MarkupElement next = null;
// if (queue.size() > 0)
// {
// next = queue.poll();
// }
// else
// {
// next = getNextFilter().nextTag();
// }
// if (next == null)
// {
// return null;
// }
// else if (next instanceof ConditionalComment)
// {
// ConditionalComment conditionalComment = (ConditionalComment)next;
// if (conditionalComment.isStartIf())
// {
// ConditionalTags conditionalTags = new ConditionalTags();
// conditionalTags.iterate(getNextFilter());
// ComponentTag mismatch = conditionalTags.getMismatch();
// if (mismatch != null)
// {
//
// if (mismatch.isOpen())
// {
// // start skipping it at this level
// log.debug("Start to skip: " + mismatch.getName());
// startSkip(mismatch.getName());
// }
// else
// {
// if (shouldSkip(mismatch.getName()))
// {
// log.debug("Skipping: " + mismatch.getName());
// // just skipping the close tag
// }
// else
// {
// throw new ParseException("Not opened tag: " + mismatch,
// conditionalTags.mismatchTag.getPos());
// }
// }
// }
// queue.addAll(conditionalTags.getValidTags());
// // start the recursion
// return nextTag();
// }
// else
// {
// // ConditionalTags should have iterated over the close tag already
// throw new ParseException("Not opened conditional comment close tag",
// conditionalComment.getPos());
// }
// }
// else
// {
// ComponentTag componentTag = (ComponentTag)next;
// if (componentTag.isOpen())
// {
// level++;
// }
// else if (componentTag.isClose())
// {
// if (shouldSkip(componentTag.getName()))
// {
// log.debug("Skipping close tag outside conditional comment: " +
// componentTag.getName());
// // tag outside conditional comment are skipped once
// stopSkipping(componentTag.getName());
// // skipping
// return nextTag();
// }
// level--;
// }
// return next;
// }
// }
//
// private void startSkip(String tagName)
// {
// Set<String> toSkipSet = skipByLevelMap.get(level);
// if (toSkipSet == null)
// {
// skipByLevelMap.put(level, toSkipSet = new HashSet<String>());
//
// }
// toSkipSet.add(tagName);
// }
//
// private void stopSkipping(String tagName)
// {
// skipByLevelMap.get(level).remove(tagName);
// }
//
// private boolean shouldSkip(String tagName)
// {
// Set<String> skipList = skipByLevelMap.get(level);
// return skipList != null && skipList.contains(tagName);
// }
//
// /**
// * Track the markup inside an conditional tag
// *
// * @author Pedro Santos
// */
// private static class ConditionalTags
// {
// private TagStack tagStack = new TagStack();
// private ArrayList<ComponentTag> tagSequence = new ArrayList<ComponentTag>();
// private ComponentTag mismatchTag;
//
// public void iterate(IMarkupFilter nextFilter) throws ParseException
// {
// MarkupElement markupElement = null;
// do
// {
// markupElement = nextFilter.nextTag();
// if (markupElement instanceof ConditionalComment)
// {
// ConditionalComment conditionalComment = (ConditionalComment)markupElement;
// if (conditionalComment.isStartIf())
// {
// throw new ParseException("nested conditional comments are not suported",
// conditionalComment.getPos());
// }
// }
// else
// {
// ComponentTag componentTag = (ComponentTag)markupElement;
// try
// {
// tagStack.assertValidInStack(componentTag);
// tagSequence.add(componentTag);
// }
// catch (ParseException e)
// {
// mismatchTag = componentTag;
// }
// }
// }
// while (markupElement != null && !(markupElement instanceof ConditionalComment));
// }
//
// private List<ComponentTag> getValidTags()
// {
// ComponentTag notClosed = tagStack.getNotClosedTag();
// @SuppressWarnings("unchecked")
// List<ComponentTag> validTags = (List<ComponentTag>)tagSequence.clone();
// if (notClosed != null)
// {
// validTags.remove(notClosed);
// }
// return validTags;
// }
//
// private boolean hasMismatch()
// {
// return mismatchTag != null || tagStack.getNotClosedTag() != null;
// }
//
// private ComponentTag getMismatch()
// {
// return mismatchTag != null ? mismatchTag : tagStack.getNotClosedTag();
// }
//
// private boolean equalsTo(ConditionalTags other)
// {
// if (tagSequence.size() != other.tagSequence.size())
// {
// return false;
// }
// else
// {
// for (int i = 0; i < tagSequence.size(); i++)
// {
// if (!tagSequence.get(i).getName().equals(other.tagSequence.get(i).getName()) ||
// !tagSequence.get(i).getType().equals(other.tagSequence.get(i).getType()))
// {
// return false;
// }
// }
// }
// return true;
// }
// }

	/**
	 * Post-process the markup if necessary
	 */
	@Override
	public void postProcess(final Markup markup)
	{
	}

	/**
	 * Noop
	 */
	@Override
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		return tag;
	}
}
