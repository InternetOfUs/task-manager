/*
 * -----------------------------------------------------------------------------
 *
 * Copyright (c) 2019 - 2022 UDT-IA, IIIA-CSIC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * -----------------------------------------------------------------------------
 */

package eu.internetofus.common.api;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import javax.ws.rs.core.HttpHeaders;

import io.vertx.ext.web.api.OperationRequest;

/**
 * Classes used to extract information of an {@link OperationRequest}.
 *
 * @see OperationRequest
 *
 * @author UDT-IA, IIIA-CSIC
 */
public interface OperationRequests {

	/**
	 * Obtain the accepted language defined on the header.
	 *
	 * @param defaultLanguage   the language to return if it can not obtain the
	 *                          accepted language.
	 * @param request           to get the information.
	 * @param poosibleLanguages the possible languages that can be used.
	 *
	 * @return the
	 *
	 * @see HttpHeaders#ACCEPT_LANGUAGE
	 */
	static String acceptedLanguageIn(OperationRequest request, String defaultLanguage, String... poosibleLanguages) {

		try {

			final String acceptLanguages = request.getHeaders().get(HttpHeaders.ACCEPT_LANGUAGE);
			final List<LanguageRange> range = Locale.LanguageRange.parse(acceptLanguages);
			final String lang = Locale.lookupTag(range, Arrays.asList(poosibleLanguages));
			if (lang == null) {

				return defaultLanguage;

			} else {

				return lang;
			}

		} catch (final Throwable badLanguage) {

			return defaultLanguage;
		}

	}

}
