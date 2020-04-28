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

package eu.internetofus.common.api.models;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * This is implemented by any model that can be merged with another model of the
 * same type.
 *
 * @param <T> type of models that can be merged.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public interface Mergeable<T> {

	/**
	 * Merge the current model with a new one, and verify the result is valid.
	 *
	 * @param source     model to merge with the current one.
	 * @param codePrefix the prefix of the code to use for the error message.
	 * @param vertx      the event bus infrastructure to use.
	 *
	 * @return the future that provide the merged model that has to be valid. If it
	 *         can not merge or the merged value is not valid the cause will be a
	 *         {@link ValidationErrorException}.
	 *
	 * @see ValidationErrorException
	 */
	Future<T> merge(T source, String codePrefix, Vertx vertx);

}