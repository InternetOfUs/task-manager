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

package eu.internetofus.wenet_task_manager.api.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.internetofus.wenet_task_manager.ModelTestCase;
import eu.internetofus.wenet_task_manager.ValidationErrorException;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link Task}.
 *
 * @see Task
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TaskTest extends ModelTestCase<Task> {

	/**
	 * Create an basic model that has the specified index.
	 *
	 * @param index to use in the example.
	 *
	 * @return the basic example.
	 */
	public Task createBasicExample(int index) {

		final Task model = new Task();
		model.taskId = null;
		model.creationTs = System.currentTimeMillis() - 1000000;
		model.startTs = model.creationTs + index * 100;
		model.endTs = model.startTs + index * 30000;
		model.deadlineTs = model.startTs + index * 15000;
		model.state = TaskState.Assigned;

		return model;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Task createModelExample(int index) {

		final Task model = this.createBasicExample(index);
		model.norms = new ArrayList<>();
		model.norms.add(new NormTest().createModelExample(index));

		return model;

	}

	/**
	 * Create an example model that has the specified index.
	 *
	 * @param index      to use in the example.
	 * @param repository to use to create the model.
	 *
	 * @return the example.
	 */
	public Future<Task> createModelExample(int index, TasksRepository repository) {

		final Promise<Task> promise = Promise.promise();
		final Future<Task> future = promise.future();
		final Task model = this.createModelExample(index);

		promise.complete(model);
		return future;

	}

	/**
	 * Check that an empty model is valid.
	 *
	 * @param repository  to create tasks to use.
	 * @param testContext context to test.
	 *
	 * @see Task#validate(String,
	 *      eu.internetofus.wenet_task_manager.persistence.TasksRepository)
	 */
	@Test
	public void shouldEmptyModelBeValid(TasksRepository repository, VertxTestContext testContext) {

		final Task model = new Task();
		testContext.assertComplete(model.validate("codePrefix", repository))
				.setHandler(result -> testContext.completeNow());

	}

	/**
	 * Check that the {@link #createModelExample(int)} is valid.
	 *
	 * @param index       to verify
	 * @param repository  to create tasks to use.
	 * @param testContext context to test.
	 *
	 * @see Task#validate(String,
	 *      eu.internetofus.wenet_task_manager.persistence.TasksRepository)
	 */
	@ParameterizedTest(name = "The model example {0} has to be valid")
	@ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
	public void shouldExampleBeValid(int index, TasksRepository repository, VertxTestContext testContext) {

		final Task model = this.createModelExample(index);
		testContext.assertComplete(model.validate("codePrefix", repository))
				.setHandler(result -> testContext.completeNow());

	}

	/**
	 * Check that the {@link #createModelExample(int,TasksRepository)} is valid.
	 *
	 * @param index       to verify
	 * @param repository  to create tasks to use.
	 * @param testContext context to test.
	 *
	 * @see Task#validate(String,
	 *      eu.internetofus.wenet_task_manager.persistence.TasksRepository)
	 */
	@ParameterizedTest(name = "The model example {0} has to be valid")
	@ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
	public void shouldExampleFromRepositoryBeValid(int index, TasksRepository repository, VertxTestContext testContext) {

		this.createModelExample(index, repository).onComplete(created -> {

			if (created.failed()) {

				testContext.failNow(created.cause());

			} else {

				final Task model = created.result();
				testContext.assertComplete(model.validate("codePrefix", repository))
						.setHandler(result -> testContext.completeNow());
			}

		});

	}

	/**
	 * Check that a model with all the values is valid.
	 *
	 * @param repository  to use.
	 * @param testContext context to test.
	 *
	 * @see Task#validate(String, TasksRepository)
	 */
	@Test
	public void shouldFullModelBeValid(TasksRepository repository, VertxTestContext testContext) {

		final Task model = new Task();
		model.taskId = " ";

		testContext.assertComplete(model.validate("codePrefix", repository)).setHandler(result -> {

			final Task expected = new Task();
			expected.taskId = model.taskId;

			assertThat(model).isEqualTo(expected);
			testContext.completeNow();
		});
	}

	/**
	 * Check that the validation of a model fails.
	 *
	 * @param model       to validate.
	 * @param suffix      to the error code.
	 * @param repository  to use.
	 * @param testContext context to test.
	 */
	public void assertFailValidate(Task model, String suffix, TasksRepository repository, VertxTestContext testContext) {

		testContext.assertFailure(model.validate("codePrefix", repository)).setHandler(result -> testContext.verify(() -> {

			final Throwable cause = result.cause();
			assertThat(cause).isInstanceOf(ValidationErrorException.class);
			String expectedCode = "codePrefix";
			if (suffix != null && suffix.length() > 0) {

				expectedCode += "." + suffix;
			}
			assertThat(((ValidationErrorException) cause).getCode()).isEqualTo(expectedCode);
			testContext.completeNow();
		}));

	}

	/**
	 * Check that the model with id is not valid.
	 *
	 * @param repository  to use.
	 * @param testContext context to test.
	 *
	 * @see Task#validate(String, TasksRepository)
	 */
	@Test
	public void shouldNotBeValidWithAnId(TasksRepository repository, VertxTestContext testContext) {

		final Task model = new Task();
		model.taskId = "has_id";
		this.assertFailValidate(model, "id", repository, testContext);

	}

	/**
	 * Check that a model can not be merged.
	 *
	 * @param model       to validate.
	 * @param suffix      to the error code.
	 * @param repository  to use.
	 * @param source      to merge.
	 * @param testContext context to test.
	 *
	 * @see Task#merge(Task, String, TasksRepository)
	 */
	public void assertFailMerge(Task model, String suffix, TasksRepository repository, Task source,
			VertxTestContext testContext) {

		testContext.assertFailure(model.merge(source, "codePrefix", repository))
				.setHandler(result -> testContext.verify(() -> {

					final Throwable cause = result.cause();
					assertThat(cause).isInstanceOf(ValidationErrorException.class);
					String expectedCode = "codePrefix";
					if (suffix != null && suffix.length() > 0) {

						expectedCode += "." + suffix;
					}
					assertThat(((ValidationErrorException) cause).getCode()).isEqualTo(expectedCode);
					testContext.completeNow();
				}));

	}

	/**
	 * Check merge empty tasks.
	 *
	 * @param repository  to use.
	 * @param testContext context to test.
	 *
	 * @see Task#merge(Task, String, TasksRepository)
	 */
	@Test
	public void shouldMergeEmptyModels(TasksRepository repository, VertxTestContext testContext) {

		final Task target = new Task();
		final Task source = new Task();
		testContext.assertComplete(target.merge(source, "codePrefix", repository))
				.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

					assertThat(merged).isEqualTo(target);
					testContext.completeNow();
				})));

	}

	/**
	 * Check merge basic tasks.
	 *
	 * @param repository  to use.
	 * @param testContext context to test.
	 *
	 * @see Task#merge(Task, String, TasksRepository)
	 */
	@Test
	public void shouldMergeBasicModels(TasksRepository repository, VertxTestContext testContext) {

		final Task target = new Task();
		target.taskId = "1";

		final Task source = new Task();
		source.taskId = "4";

		testContext.assertComplete(target.merge(source, "codePrefix", repository))
				.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
					testContext.completeNow();
				})));

	}

	/**
	 * Check merge example tasks.
	 *
	 * @param repository  to use.
	 * @param testContext context to test.
	 *
	 * @see Task#merge(Task, String, TasksRepository)
	 */
	@Test
	public void shouldMergeExampleModels(TasksRepository repository, VertxTestContext testContext) {

		final Task target = this.createModelExample(1);
		target.taskId = "1";
		final Task source = this.createModelExample(2);
		source.taskId = "2";
		testContext.assertComplete(target.merge(source, "codePrefix", repository))
				.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

					source.taskId = target.taskId;
					assertThat(merged).isNotEqualTo(target).isEqualTo(source);
					testContext.completeNow();
				})));

	}

	/**
	 * Check merge stored tasks.
	 *
	 * @param repository  to use.
	 * @param testContext context to test.
	 *
	 * @see Task#merge(Task, String, TasksRepository)
	 */
	@Test
	public void shouldMergeStoredModels(TasksRepository repository, VertxTestContext testContext) {

		testContext.assertComplete(this.createModelExample(1, repository)).setHandler(targetToStore -> {

			testContext.assertComplete(this.createModelExample(2, repository)).setHandler(sourceToStore -> {

				repository.storeTask(targetToStore.result(), testContext.succeeding(target -> {

					repository.storeTask(sourceToStore.result(), testContext.succeeding(source -> {

						testContext.assertComplete(target.merge(source, "codePrefix", repository))
								.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

									source.taskId = target.taskId;
									assertThat(merged).isNotEqualTo(target).isEqualTo(source);
									testContext.completeNow();
								})));

					}));

				}));

			});

		});

	}

}
