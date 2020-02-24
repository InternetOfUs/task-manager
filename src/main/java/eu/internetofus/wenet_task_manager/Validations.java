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

package eu.internetofus.wenet_task_manager;

/**
 * Generic methods to validate the common fields of the models.
 *
 * @see ValidationErrorException
 *
 * @author UDT-IA, IIIA-CSIC
 */
public interface Validations {

	/**
	 * Verify a string value.
	 *
	 * @param codePrefix the prefix of the code to use for the error message.
	 * @param fieldName  name of the checking field.
	 * @param maxSize    maximum size of the string.
	 * @param value      to verify.
	 *
	 * @return the verified value.
	 *
	 * @throws ValidationErrorException If the value is not a valid string.
	 */
	static String validateNullableStringField(String codePrefix, String fieldName, int maxSize, String value)
			throws ValidationErrorException {

		if (value != null) {

			final String trimmedValue = value.trim();
			if (trimmedValue.length() == 0) {

				return null;

			} else if (trimmedValue.length() > maxSize) {

				throw new ValidationErrorException(codePrefix + "." + fieldName,
						"The '" + trimmedValue + "' is too large. The maximum length is '" + maxSize + "'.");

			} else {

				return trimmedValue;
			}

		} else {

			return null;
		}
	}

}
