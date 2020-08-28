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

package eu.internetofus.common.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link Merges}.
 *
 * @see Merges
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class MergesTest {

  /**
   * Assert that a model can not be merged
   *
   * @param target      model to merge.
   * @param source      model to merge.
   * @param vertx       event bus to use.
   * @param testContext test context to use.
   * @param <T>         type of model to merge.
   */
  public static <T> void assertCannotMerge(final Mergeable<T> target, final T source, final Vertx vertx, final VertxTestContext testContext) {

    assertCannotMerge(target, source, null, vertx, testContext);

  }

  /**
   * Assert that a model is not valid because a field is wrong.
   *
   * @param target      model to merge.
   * @param source      model to merge.
   * @param fieldName   name of the field that is not valid.
   * @param vertx       event bus to use.
   * @param testContext test context to use.
   * @param <T>         type of model to merge.
   */
  public static <T> void assertCannotMerge(final Mergeable<T> target, final T source, final String fieldName, final Vertx vertx, final VertxTestContext testContext) {

    target.merge(source, "codePrefix", vertx).onComplete(testContext.failing(error -> testContext.verify(() -> {

      assertThat(error).isInstanceOf(ValidationErrorException.class);
      var expectedCode = "codePrefix";
      if (fieldName != null) {

        expectedCode += "." + fieldName;

      }
      assertThat(((ValidationErrorException) error).getCode()).isEqualTo(expectedCode);

      testContext.completeNow();

    })));

  }

  /**
   * Assert that a model has been merged.
   *
   * @param target      model to merge.
   * @param source      model to merge.
   * @param vertx       event bus to use.
   * @param testContext test context to use.
   * @param <T>         model to test.
   */
  public static <T extends Validable> void assertCanMerge(final Mergeable<T> target, final T source, final Vertx vertx, final VertxTestContext testContext) {

    assertCanMerge(target, source, vertx, testContext, null);

  }

  /**
   * Assert that a model has been merged.
   *
   * @param target      model to merge.
   * @param source      model to merge.
   * @param vertx       event bus to use.
   * @param testContext test context to use.
   * @param expected    function to validate the merged value.
   * @param <T>         model to test.
   */
  public static <T extends Validable> void assertCanMerge(final Mergeable<T> target, final T source, final Vertx vertx, final VertxTestContext testContext, final Consumer<T> expected) {

    target.merge(source, "codePrefix", vertx).onComplete(testContext.succeeding(merged -> testContext.verify(() -> {

      if (expected != null) {

        expected.accept(merged);
      }

      testContext.completeNow();

    })));

  }
}
