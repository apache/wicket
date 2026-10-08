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
package org.apache.wicket.examples;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.ajax.WicketAjaxResourceReference;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.JavaScriptLibrarySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lets every example application be started with either the default, JQuery-based
 * {@code wicket-ajax.js} implementation or the JQuery-free plain JavaScript one, selected with a
 * single JVM-wide system property rather than having to change code in each of the many
 * {@link org.apache.wicket.examples.WicketExampleApplication} subclasses individually:
 *
 * <pre>
 * -Dwicket.examples.use=JQUERY   (default - JQuery-based wicket-ajax.js)
 * -Dwicket.examples.use=VANILLA  (JQuery-free, plain JavaScript wicket-ajax.js)
 * </pre>
 *
 * Call {@link #configure(WebApplication)} once from each application's {@code init()}.
 * <p>
 * On top of that JVM-wide default, {@link #setSessionEngine(Engine)} lets a single browser
 * session switch to the other engine at runtime - e.g. from a link in
 * {@link WicketExampleHeader} - so the two implementations can be compared side by side without
 * restarting the application. The override is stored in the session and is picked up by every
 * page from the next full page load on - a client that already has one engine's JavaScript
 * loaded cannot swap it out live, hence "next full page load" rather than instantly.
 * <p>
 * {@link #configure(WebApplication)} installs this by replacing the application's
 * {@link JavaScriptLibrarySettings} with one whose
 * {@link JavaScriptLibrarySettings#getWicketAjaxReference() getWicketAjaxReference()} resolves
 * {@link #getEffectiveEngine()} on every call, rather than by rewriting header items after the
 * fact. That single instance is shared by all sessions, but it stores nothing: it just forwards
 * to whatever {@link Session#get()} resolves to on the calling thread, so concurrent sessions
 * with different choices never interfere with each other. Answering at the source also means
 * everything downstream - the {@link org.apache.wicket.markup.head.ResourceAggregator} resolving
 * dependencies (so JQuery itself is not contributed at all under {@link Engine#VANILLA}), the
 * CSP nonce that {@link org.apache.wicket.csp.CSPNonceHeaderResponseDecorator} stamps onto each
 * script, and dependants such as {@link org.apache.wicket.markup.head.OnDomReadyHeaderItem} -
 * sees the engine this session actually asked for.
 *
 * @see org.apache.wicket.settings.JavaScriptLibrarySettings
 * @see WicketAjaxResourceReference
 */
public final class AjaxEngineSelector
{
	/** The name of the system property that selects the Ajax engine. */
	public static final String SYSTEM_PROPERTY = "wicket.examples.use";

	private static final Logger log = LoggerFactory.getLogger(AjaxEngineSelector.class);

	private static final MetaDataKey<Engine> SESSION_ENGINE_KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** The two available client-side Ajax engines. */
	public enum Engine
	{
		JQUERY,
		VANILLA
	}

	private AjaxEngineSelector()
	{
	}

	/**
	 * Replaces the given application's {@link JavaScriptLibrarySettings} with one that resolves
	 * the {@code wicket-ajax.js} implementation per request from {@link #getEffectiveEngine()} -
	 * the {@value #SYSTEM_PROPERTY} system property, or the current session's override if
	 * {@link #setSessionEngine(Engine)} was called on it.
	 *
	 * @param application
	 *            the application to configure
	 */
	public static void configure(WebApplication application)
	{
		Engine engine = parseEngine(System.getProperty(SYSTEM_PROPERTY, Engine.JQUERY.name()), true);

		application.setJavaScriptLibrarySettings(new JavaScriptLibrarySettings()
		{
			@Override
			public ResourceReference getWicketAjaxReference()
			{
				return referenceFor(getEffectiveEngine());
			}
		});

		log.info("Wicket examples Ajax engine: {}", engine);
		outputAjaxEngineBanner(engine);
	}

	/**
	 * @return the Ajax engine that is actually in effect for the current request: the current
	 *         session's override if {@link #setSessionEngine(Engine)} was called on it,
	 *         otherwise this application's configured default (the {@value #SYSTEM_PROPERTY}
	 *         system property).
	 */
	public static Engine getEffectiveEngine()
	{
		if (Session.exists())
		{
			Engine override = Session.get().getMetaData(SESSION_ENGINE_KEY);
			if (override != null)
			{
				return override;
			}
		}
		return parseEngine(System.getProperty(SYSTEM_PROPERTY, Engine.JQUERY.name()), false);
	}

	/**
	 * Overrides, for the current session only, which Ajax engine is used - every page in this
	 * session will use it from now on. Concurrent sessions (e.g. other browsers, or a different
	 * session in the same browser) are unaffected. Takes effect on the next full page load; a
	 * page whose response has already been sent to the browser keeps whichever engine it was
	 * rendered with, since that JavaScript is already loaded and cannot be swapped out live.
	 *
	 * @param engine
	 *            the engine this session should use from now on
	 */
	public static void setSessionEngine(Engine engine)
	{
		Session.get().setMetaData(SESSION_ENGINE_KEY, engine);
	}

	private static Engine parseEngine(String value, boolean warnIfInvalid)
	{
		try
		{
			return Engine.valueOf(value.trim().toUpperCase());
		}
		catch (IllegalArgumentException notRecognized)
		{
			if (warnIfInvalid)
			{
				log.warn("Unrecognized value '{}' for system property '{}'. Falling back to {}. " +
					"Valid values are {} and {}.", value, SYSTEM_PROPERTY, Engine.JQUERY,
					Engine.JQUERY, Engine.VANILLA);
			}
			return Engine.JQUERY;
		}
	}

	private static ResourceReference referenceFor(Engine engine)
	{
		return engine == Engine.VANILLA ? WicketAjaxResourceReference.get()
			: WicketAjaxJQueryResourceReference.get();
	}

	/**
	 * @return a human-friendly name for the given engine, suitable for display in the UI or in
	 *         console output - "jQuery" or "Plain JavaScript" rather than the raw enum constant.
	 */
	public static String displayName(Engine engine)
	{
		return engine == Engine.VANILLA ? "Plain JavaScript" : "jQuery";
	}

	/**
	 * Prints a banner to stdout announcing which Ajax engine this application started with,
	 * the same way {@link WebApplication#outputDevelopmentModeWarning()} unconditionally prints
	 * to stderr when running in development mode: a plain log statement can be filtered out or
	 * missed depending on logging configuration, but this always shows up in the console of
	 * whatever launched the example (StartExamples, {@code mvn jetty:run}, an application
	 * server, ...).
	 *
	 * @param engine
	 *            the engine that was selected for this application
	 */
	private static void outputAjaxEngineBanner(Engine engine)
	{
		String line1 = "Wicket examples Ajax engine: " + displayName(engine)
			+ (engine == Engine.JQUERY ? " (default)" : " (no jQuery)");
		Engine other = engine == Engine.JQUERY ? Engine.VANILLA : Engine.JQUERY;
		String line2 = "Set -D" + SYSTEM_PROPERTY + "=" + other + " for the "
			+ (other == Engine.VANILLA ? displayName(other) : "default") + " engine instead.";

		// size the border/padding to fit the longer of the two lines, with a minimum width
		// so the banner still looks like a banner for short messages
		String prefix = "*** ";
		String suffix = " ***";
		int width = Math.max(70,
			prefix.length() + suffix.length() + Math.max(line1.length(), line2.length()));

		String border = "*".repeat(width);
		System.out.print(border + "\n"
			+ pad(line1, prefix, suffix, width) + "\n"
			+ pad(line2, prefix, suffix, width) + "\n"
			+ border + "\n");
	}

	/**
	 * Pads a line of text with a leading/trailing "*** ... ***" border so that it lines up
	 * with {@link #outputAjaxEngineBanner(Engine)}'s asterisk borders.
	 */
	private static String pad(String text, String prefix, String suffix, int width)
	{
		int fillLength = Math.max(0, width - prefix.length() - suffix.length() - text.length());
		StringBuilder line = new StringBuilder(prefix).append(text);
		for (int i = 0; i < fillLength; i++)
		{
			line.append(' ');
		}
		return line.append(suffix).toString();
	}
}
