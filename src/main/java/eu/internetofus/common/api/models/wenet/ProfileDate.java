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

package eu.internetofus.common.api.models.wenet;

import eu.internetofus.common.api.models.Mergeable;
import eu.internetofus.common.api.models.Model;
import eu.internetofus.common.api.models.Validable;
import eu.internetofus.common.api.models.ValidationErrorException;
import io.swagger.v3.oas.annotations.media.Schema;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * Represents a date.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Schema(name = "date", description = "The information of a date.")
public class ProfileDate extends Model implements Validable, Mergeable<ProfileDate> {

	/**
	 * The year of the date.
	 */
	@Schema(description = "The year of the date.", example = "1976")
	public int year;

	/**
	 * The year of the date.
	 */
	@Schema(description = "The month of the date,from 1 to 12 (1: Jan and 12: Dec).", example = "4")
	public byte month;

	/**
	 * The day of the date.
	 */
	@Schema(description = "The day of the date.", example = "23")
	public byte day;

	/**
	 * Create an empty date.
	 */
	public ProfileDate() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Future<Void> validate(String codePrefix, Vertx vertx) {

		final Promise<Void> promise = Promise.promise();
		if (this.month < 1 || this.month > 12) {

			promise.fail(new ValidationErrorException(codePrefix + ".month", "The month has to be on the range [1,12]"));

		} else if (this.day < 1 || this.day > 31) {

			promise.fail(new ValidationErrorException(codePrefix + ".day", "The day has to be on the range [1,31]"));

		} else {

			try {

				java.time.LocalDate.of(this.year, this.month, this.day);
				promise.complete();

			} catch (final Throwable exception) {

				promise.fail(new ValidationErrorException(codePrefix, exception));
			}
		}

		return promise.future();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Future<ProfileDate> merge(ProfileDate source, String codePrefix, Vertx vertx) {

		if (source != null) {

			final ProfileDate merged = new ProfileDate();
			if (source.year != 0) {

				merged.year = source.year;

			} else {

				merged.year = this.year;
			}
			if (source.month != 0) {

				merged.month = source.month;

			} else {

				merged.month = this.month;
			}
			if (source.day != 0) {

				merged.day = source.day;

			} else {

				merged.day = this.day;
			}

			return merged.validate(codePrefix, vertx).map(empty -> merged);

		} else {

			return Future.succeededFuture(this);

		}

	}

}
