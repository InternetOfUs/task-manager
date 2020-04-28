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

import static eu.internetofus.common.api.models.ValidationsTest.assertIsNotValid;
import static eu.internetofus.common.api.models.ValidationsTest.assertIsValid;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.internetofus.common.api.models.ModelTestCase;
import eu.internetofus.common.api.models.ValidationsTest;
import eu.internetofus.common.services.ServiceApiSimulatorServiceOnMemory;
import eu.internetofus.common.services.WeNetProfileManagerServiceOnMemory;
import eu.internetofus.common.services.WeNetTaskManagerServiceOnMemory;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link TaskTransaction}.
 *
 * @see TaskTransaction
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TaskTransactionTest extends ModelTestCase<TaskTransaction> {

	/**
	 * Register the necessary services before to test.
	 *
	 * @param vertx event bus to register the necessary services.
	 */
	@BeforeEach
	public void registerServices(Vertx vertx) {

		WeNetProfileManagerServiceOnMemory.register(vertx);
		WeNetTaskManagerServiceOnMemory.register(vertx);
		ServiceApiSimulatorServiceOnMemory.register(vertx);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskTransaction createModelExample(int index) {

		assert index >= 0;
		final TaskTransaction model = new TaskTransaction();
		model.taskId = "taskId" + index;
		model.typeId = "typeId" + index;
		model.attributes = new ArrayList<>();
		model.attributes.add(new TaskAttributeTest().createModelExample(index));
		return model;

	}

	/**
	 * Check that the {@link #createModelExample(int)} is valid.
	 *
	 * @param index       to verify.
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@ParameterizedTest(name = "The model example {0} has to be valid")
	@ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
	public void shouldExampleBeValid(int index, Vertx vertx, VertxTestContext testContext) {

		final TaskTransaction model = this.createModelExample(index);
		assertIsValid(model, vertx, testContext);

	}

	/**
	 * An empty transaction not be valid.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldEmptyTransactionBeNotValid(Vertx vertx, VertxTestContext testContext) {

		assertIsNotValid(new TaskTransaction(), "taskId", vertx, testContext);
	}

	/**
	 * A task transaction without task identifier cannot be valid.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldTaskTransactionBeNotValidWithoutTaskId(Vertx vertx, VertxTestContext testContext) {

		final TaskTransaction model = this.createModelExample(1);
		model.taskId = null;
		assertIsNotValid(model, "taskId", vertx, testContext);
	}

	/**
	 * A task transaction without task type identifier cannot be valid.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldTaskTransactionBeNotValidWithoutTaskTypeId(Vertx vertx, VertxTestContext testContext) {

		final TaskTransaction model = this.createModelExample(1);
		model.typeId = null;
		assertIsNotValid(model, "typeId", vertx, testContext);
	}

	/**
	 * A task transaction with bad attribute cannot be valid.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldTaskTransactionBeNotValidWithBadAttribute(Vertx vertx, VertxTestContext testContext) {

		final TaskTransaction model = this.createModelExample(1);
		model.attributes.get(0).name = ValidationsTest.STRING_256;
		assertIsNotValid(model, "attributes[0].name", vertx, testContext);
	}

}