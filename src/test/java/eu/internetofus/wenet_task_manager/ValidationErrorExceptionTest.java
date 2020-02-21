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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Test the {@link ValidationErrorException}
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class ValidationErrorExceptionTest {

	/**
	 * Check that create with a code and message.
	 */
	@Test
	@Tag("unit")
	public void shouldCreateWithCodeAndMessage() {

		final ValidationErrorException error = new ValidationErrorException("code", "message");
		assertThat(error.getCode()).isEqualTo("code");
		assertThat(error.getMessage()).isEqualTo("message");
	}

	/**
	 * Check that create with a code and cause.
	 */
	@Test
	@Tag("unit")
	public void shouldCreateWithCodeAndCause() {

		final Throwable cause = new Throwable("cause");
		final ValidationErrorException error = new ValidationErrorException("code", cause);
		assertThat(error.getCode()).isEqualTo("code");
		assertThat(error.getMessage()).isEqualTo(cause.toString());
		assertThat(error.getCause()).isEqualTo(cause);
	}

	/**
	 * Check that create with a code, cause and message.
	 */
	@Test
	@Tag("unit")
	public void shouldCreateWithCodeCauseAndMessage() {

		final Throwable cause = new Throwable("cause");
		final ValidationErrorException error = new ValidationErrorException("code", "message", cause);
		assertThat(error.getCode()).isEqualTo("code");
		assertThat(error.getMessage()).isEqualTo("message");
		assertThat(error.getCause()).isEqualTo(cause);
	}

}
