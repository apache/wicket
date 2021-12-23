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
package org.apache.wicket.core.util.lang;

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.wicket.util.lang.Args;

public class LocaleUtils {
	/**
	 * taken from BidiUtils
	 *
	 * A regular expression for matching right-to-left language codes. See
	 * {@link #isRtlLanguage} for the design.
	 */
	private static final Pattern RtlLocalesRe = Pattern.compile("^(ar|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Arab|Hebr|Thaa|Nkoo|Tfng))"
					+ "(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)");

	private LocaleUtils() {
		// utility class
	}

	/**
	 * Check if a BCP 47 / III language code indicates an RTL language, i.e.
	 * either: - a language code explicitly specifying one of the right-to-left
	 * scripts, e.g. "az-Arab", or
	 * <p>
	 * - a language code specifying one of the languages normally written in a
	 * right-to-left script, e.g. "fa" (Farsi), except ones explicitly
	 * specifying Latin or Cyrillic script (which are the usual LTR
	 * alternatives).
	 * <p>
	 * The list of right-to-left scripts appears in the 100-199 range in
	 * http://www.unicode.org/iso15924/iso15924-num.html, of which Arabic and
	 * Hebrew are by far the most widely used. We also recognize Thaana, N'Ko,
	 * and Tifinagh, which also have significant modern usage. The rest (Syriac,
	 * Samaritan, Mandaic, etc.) seem to have extremely limited or no modern
	 * usage and are not recognized. The languages usually written in a
	 * right-to-left script are taken as those with Suppress-Script:
	 * Hebr|Arab|Thaa|Nkoo|Tfng in
	 * http://www.iana.org/assignments/language-subtag-registry, as well as
	 * Sindhi (sd) and Uyghur (ug). The presence of other subtags of the
	 * language code, e.g. regions like EG (Egypt), is ignored.
	 *
	 * @param languageString - locale string
	 * @return <code>true</code> in case passed locale is right-to-left
	 */
	public static boolean isRtlLanguage(final Locale locale) {
		Args.notNull(locale, "locale");
		return RtlLocalesRe.matcher(locale.toLanguageTag()).find();
	}
}
