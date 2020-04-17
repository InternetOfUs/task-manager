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

import eu.internetofus.common.TimeManager;
import eu.internetofus.common.api.models.Model;
import eu.internetofus.common.api.models.ModelTestCase;
import eu.internetofus.common.api.models.ValidationsTest;
import eu.internetofus.common.services.WeNetProfileManagerService;
import eu.internetofus.common.services.WeNetProfileManagerServiceOnMemory;
import eu.internetofus.common.services.WeNetTaskManagerService;
import eu.internetofus.common.services.WeNetTaskManagerServiceOnMemory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link Task}.
 *
 * @see Task
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TaskTest extends ModelTestCase<Task> {

	/**
	 * Register the necessary services before to test.
	 *
	 * @param vertx event bus to register the necessary services.
	 */
	@BeforeEach
	public void registerServices(Vertx vertx) {

		WeNetProfileManagerServiceOnMemory.register(vertx);
		WeNetTaskManagerServiceOnMemory.register(vertx);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Task createModelExample(int index) {

		final Task model = new Task();
		model.taskTypeId = "taskTypeId" + index;
		model.requesterId = "requesterId" + index;
		model.appId = "appId" + index;
		model.goal = new TaskGoalTest().createModelExample(index);
		model.startTs = TimeManager.now() + 10000 * index;
		model.endTs = model.startTs + 300000l + index;
		model.deadlineTs = model.deadlineTs + 200000l + index;
		model.norms = new ArrayList<>();
		model.norms.add(new NormTest().createModelExample(index));
		model.attributes = new ArrayList<>();
		model.attributes.add(new TaskAttributeTest().createModelExample(index));
		model._creationTs = 1234567891 + index;
		model._lastUpdateTs = 1234567991 + index * 2;
		return model;
	}

	/**
	 * Check that the
	 * {@link #createModelExample(int, Vertx, VertxTestContext, Handler)} is valid.
	 *
	 * @param index       to verify
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@ParameterizedTest(name = "The model example {0} has to be NOT valid")
	@ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
	public void shouldExampleBeValid(int index, Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(index, vertx, testContext,
				testContext.succeeding(model -> assertIsValid(model, vertx, testContext)));

	}

	/**
	 * Store a profile.
	 *
	 * @param profile      to store.
	 * @param vertx        event bus to use.
	 * @param testContext  test context to use.
	 * @param storeHandler the component that will manage the stored model.
	 */
	public void storeProfile(WeNetUserProfile profile, Vertx vertx, VertxTestContext testContext,
			Handler<AsyncResult<WeNetUserProfile>> storeHandler) {

		WeNetProfileManagerService.createProxy(vertx).createProfile(profile.toJsonObject(),
				testContext.succeeding(created -> {

					final WeNetUserProfile result = Model.fromJsonObject(created, WeNetUserProfile.class);
					storeHandler.handle(Future.succeededFuture(result));

				}));

	}

	/**
	 * Store a task type.
	 *
	 * @param taskType     to store.
	 * @param vertx        event bus to use.
	 * @param testContext  test context to use.
	 * @param storeHandler the component that will manage the stored model.
	 */
	public void storeTaskType(TaskType taskType, Vertx vertx, VertxTestContext testContext,
			Handler<AsyncResult<TaskType>> storeHandler) {

		WeNetTaskManagerService.createProxy(vertx).createTaskType(taskType.toJsonObject(),
				testContext.succeeding(created -> {

					final TaskType result = Model.fromJsonObject(created, TaskType.class);
					storeHandler.handle(Future.succeededFuture(result));

				}));

	}

	/**
	 * Create an example model that has the specified index.
	 *
	 * @param index         to use in the example.
	 * @param vertx         event bus to use.
	 * @param testContext   test context to use.
	 * @param createHandler the component that will manage the created model.
	 */
	public void createModelExample(int index, Vertx vertx, VertxTestContext testContext,
			Handler<AsyncResult<Task>> createHandler) {

		this.storeProfile(new WeNetUserProfile(), vertx, testContext, testContext.succeeding(profile -> {

			this.storeTaskType(new TaskType(), vertx, testContext, testContext.succeeding(taskType -> {

				final Task model = this.createModelExample(index);
				model.requesterId = profile.id;
				model.taskTypeId = taskType.id;
				createHandler.handle(Future.succeededFuture(model));

			}));

		}));

	}

	/**
	 * Check that can not be valid with a too large task type identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithALargeTaskTypeId(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.taskTypeId = ValidationsTest.STRING_256;
		assertIsNotValid(model, "taskTypeId", vertx, testContext);

	}

	/**
	 * Check that can not be valid with an undefined task type identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithAnUndefinedTaskTypeId(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.taskTypeId = "Undefined-task-type-ID";
		assertIsNotValid(model, "taskTypeId", vertx, testContext);

	}

	/**
	 * Check that can not be valid with a too large requester identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithALargeRequesterId(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.requesterId = ValidationsTest.STRING_256;
		assertIsNotValid(model, "requesterId", vertx, testContext);

	}

	/**
	 * Check that can not be valid with an undefined requester identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithAnUndefinedRequesterId(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.requesterId = "Undefined-requester-id";
		assertIsNotValid(model, "requesterId", vertx, testContext);

	}

	/**
	 * Check that can not be valid with a too large app identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithALargeAppId(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.appId = ValidationsTest.STRING_256;
		assertIsNotValid(model, "appId", vertx, testContext);

	}

	/**
	 * Check that can not be valid with an undefined app identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithAnUndefinedAppId(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.appId = "Undefined-app-id";
		assertIsNotValid(model, "appId", vertx, testContext);

	}

	/**
	 * Check that can not be valid with a too soon start time stamp.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithATooSoonStartTimeStampn(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.startTs = TimeManager.now();
		assertIsNotValid(model, "startTs", vertx, testContext);

	}

	/**
	 * Check that can not be valid with a too soon end time stamp.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithATooSoonEndTimeStampn(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.startTs = TimeManager.now() + 10000;
		model.endTs = TimeManager.now();
		assertIsNotValid(model, "endTs", vertx, testContext);

	}

	/**
	 * Check that can not be valid with a too soon deadline time stamp.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithATooSoonDeadlineTimeStampn(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.startTs = TimeManager.now() + 10000;
		model.endTs = model.startTs + 20000;
		model.deadlineTs = model.startTs;
		assertIsNotValid(model, "deadlineTs", vertx, testContext);

	}

	/**
	 * Check that can not be valid with a too late deadline time stamp.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithATooLateDeadlineTimeStampn(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.startTs = TimeManager.now() + 10000;
		model.endTs = model.startTs + 20000;
		model.deadlineTs = model.endTs;
		assertIsNotValid(model, "deadlineTs", vertx, testContext);

	}

	/**
	 * Check that not accept profiles with bad norms.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithABadNorms(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.norms = new ArrayList<>();
		model.norms.add(new Norm());
		model.norms.add(new Norm());
		model.norms.add(new Norm());
		model.norms.get(1).attribute = ValidationsTest.STRING_256;
		assertIsNotValid(model, "norms[1].attribute", vertx, testContext);

	}

	/**
	 * Check that not accept profiles with bad attributes.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithABadAttributes(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.attributes = new ArrayList<>();
		model.attributes.add(new TaskAttribute());
		model.attributes.add(new TaskAttribute());
		model.attributes.add(new TaskAttribute());
		model.attributes.get(1).name = ValidationsTest.STRING_256;
		assertIsNotValid(model, "attributes[1].attribute", vertx, testContext);

	}

}
