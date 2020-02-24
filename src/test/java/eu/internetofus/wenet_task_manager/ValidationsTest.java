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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test the {@link Validations}.
 *
 * @see Validations
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class ValidationsTest {

	/**
	 * A string with 256 characters.
	 */
	public static final String STRING_256 = "0Ncu2eQI7boSct2Ga6VHViEPJn0HqffPajWKyL3TmgUyLG4ZjVLbaZSx7DZXuY0EAWGqWnWOB35Uql92cV2zTbBrSi4gVR0y9jJ3a5zsHnnXNFucmHRyplXw0v98l7BD4d8jvKro7QBIuZM4A4fARUol9gSrRAIoZ7PpUxtbNfteFkVhxfRUhGAkHKfRUsMulmgui5bRQaCM8ivevTJm8N4jXXUlgfkPepeMsQeQPzktJRnZDR3PxDrKLtKjoE24";

	/**
	 * Check that a field can be null.
	 *
	 * @see Validations#validateNullableStringField(String, String, int,String)
	 */
	@Test
	public void shouldNullStringFieldBeValid() {

		assertThat(Validations.validateNullableStringField("codePrefix", "fieldName", 255, null)).isEqualTo(null);
	}

	/**
	 * Check that an empty is right but is changed to null.
	 *
	 * @see Validations#validateNullableStringField(String, String, int,String)
	 */
	@Test
	public void shouldEmptyStringFieldBeValid() {

		assertThat(Validations.validateNullableStringField("codePrefix", "fieldName", 255, "")).isEqualTo(null);
	}

	/**
	 * Check that an white value is right but is changed to null.
	 *
	 * @see Validations#validateNullableStringField(String, String, int,String)
	 */
	@Test
	public void shouldWhiteStringFieldBeValid() {

		assertThat(Validations.validateNullableStringField("codePrefix", "fieldName", 255, "       ")).isEqualTo(null);
	}

	/**
	 * Check that the value is trimmed to be valid.
	 *
	 * @see Validations#validateNullableStringField(String, String, int,String)
	 */
	@Test
	public void shouldStringWithWhiteFieldBeValid() {

		assertThat(Validations.validateNullableStringField("codePrefix", "fieldName", 255, "   a b c    "))
				.isEqualTo("a b c");
	}

	/**
	 * Check that the value of the field is not valid if it is too large.
	 *
	 * @see Validations#validateNullableStringField(String, String, int,String)
	 */
	@Test
	public void shouldNotBeValidIfStringIsTooLarge() {

		assertThat(assertThrows(ValidationErrorException.class,
				() -> Validations.validateNullableStringField("codePrefix", "fieldName", 255, ValidationsTest.STRING_256))
						.getCode()).isEqualTo("codePrefix.fieldName");
	}

}
