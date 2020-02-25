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
import eu.internetofus.wenet_task_manager.ValidationsTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import eu.internetofus.wenet_task_manager.services.WeNetProfileManagerService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
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
	 * {@inheritDoc}
	 */
	@Override
	public Task createModelExample(int index) {

		final Task model = new Task();
		model.taskId = null;
		model.creationTs = System.currentTimeMillis();
		model.deadlineTs = model.creationTs + index * 30000;
		model.startTs = model.creationTs + index * 31000;
		model.endTs = model.creationTs + index * 300000;

		model.state = TaskState.values()[index % (TaskState.values().length - 1)];
		model.norms = new ArrayList<>();
		model.norms.add(new NormTest().createModelExample(index));

		return model;

	}

	/**
	 * Create an example model that has the specified index.
	 *
	 * @param index          to use in the example.
	 * @param profileService service to manage user profiles.
	 *
	 * @return the example.
	 */
	public Future<Task> createModelExample(int index, WeNetProfileManagerService profileService) {

		final Promise<Task> promise = Promise.promise();
		final Future<Task> future = promise.future();

		profileService.createProfile(new JsonObject(), create -> {

			if (create.failed()) {

				promise.fail(create.cause());

			} else {

				final JsonObject profile = create.result();
				final Task model = this.createModelExample(index);
				model.requesterUserId = profile.getString("id");
				promise.complete(model);

			}

		});

		return future;

	}

	/**
	 * Check that an empty model is valid.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldEmptyModelBeValid(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task model = new Task();
		testContext.assertComplete(model.validate("codePrefix", profileService))
				.setHandler(result -> testContext.completeNow());

	}

	/**
	 * Check that the {@link #createModelExample(int)} is valid.
	 *
	 * @param index          to verify
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@ParameterizedTest(name = "The model example {0} has to be valid")
	@ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
	public void shouldExampleBeValid(int index, WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task model = this.createModelExample(index);
		testContext.assertComplete(model.validate("codePrefix", profileService))
				.setHandler(result -> testContext.completeNow());

	}

	/**
	 * Check that the {@link #createModelExample(int,WeNetProfileManagerService)} is
	 * valid.
	 *
	 * @param index          to verify
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@ParameterizedTest(name = "The model example {0} has to be valid")
	@ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
	public void shouldExampleFromRepositoryBeValid(int index, WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		testContext.assertComplete(this.createModelExample(index, profileService)).setHandler(created -> {

			final Task model = created.result();
			testContext.assertComplete(model.validate("codePrefix", profileService))
					.setHandler(result -> testContext.completeNow());

		});

	}

	/**
	 * Check that a model with all the values is valid.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldFullModelBeValid(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task model = new Task();
		model.taskId = " ";

		testContext.assertComplete(model.validate("codePrefix", profileService)).setHandler(result -> {

			final Task expected = new Task();
			expected.taskId = model.taskId;

			assertThat(model).isEqualTo(expected);
			testContext.completeNow();
		});
	}

	/**
	 * Check that the validation of a model fails.
	 *
	 * @param model          to validate.
	 * @param suffix         to the error code.
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 */
	public void assertFailValidate(Task model, String suffix, WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		testContext.assertFailure(model.validate("codePrefix", profileService))
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
	 * Check that the model with id is not valid.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotBeValidWithAnId(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task model = new Task();
		model.taskId = "has_id";
		this.assertFailValidate(model, "taskId", profileService, testContext);

	}

	/**
	 * Check that the model is not valid if the start time is before the created
	 * time.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotBeValidWithAStartTimeBeforeCreationTime(WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		final Task model = new Task();
		model.creationTs = 2;
		model.startTs = 1;
		this.assertFailValidate(model, "startTs", profileService, testContext);

	}

	/**
	 * Check that the model is not valid if the end time is before the start time.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotBeValidWithAEndTimeBeforeStartTs(WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		final Task model = new Task();
		model.creationTs = 2;
		model.startTs = 3;
		model.endTs = 1;
		this.assertFailValidate(model, "endTs", profileService, testContext);

	}

	/**
	 * Check that the model is not valid if the deadline time is before the start
	 * time.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotBeValidWithADeadlineTimeAfterStartTs(WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		final Task model = new Task();
		model.creationTs = 2;
		model.startTs = 3;
		model.deadlineTs = 4;
		model.endTs = 5;
		this.assertFailValidate(model, "deadlineTs", profileService, testContext);

	}

	/**
	 * Check that a model can not be merged.
	 *
	 * @param model          to validate.
	 * @param suffix         to the error code.
	 * @param profileService to manage profiles.
	 * @param source         to merge.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	public void assertFailMerge(Task model, String suffix, WeNetProfileManagerService profileService, Task source,
			VertxTestContext testContext) {

		testContext.assertFailure(model.merge(source, "codePrefix", profileService))
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
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldMergeEmptyModels(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task target = new Task();
		final Task source = new Task();
		testContext.assertComplete(target.merge(source, "codePrefix", profileService))
				.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

					assertThat(merged).isEqualTo(target);
					testContext.completeNow();
				})));

	}

	/**
	 * Check merge basic tasks.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldMergeBasicModels(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task target = new Task();
		target.taskId = "1";

		final Task source = new Task();
		source.taskId = "4";

		testContext.assertComplete(target.merge(source, "codePrefix", profileService))
				.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
					testContext.completeNow();
				})));

	}

	/**
	 * Check merge example tasks.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldMergeExampleModels(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task target = this.createModelExample(1);
		target.taskId = "1";
		final Task source = this.createModelExample(2);
		source.taskId = "2";
		testContext.assertComplete(target.merge(source, "codePrefix", profileService))
				.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

					source.taskId = target.taskId;
					assertThat(merged).isNotEqualTo(target).isEqualTo(source);
					testContext.completeNow();
				})));

	}

	/**
	 * Check merge stored tasks.
	 *
	 * import static org.assertj.core.api.Assertions.assertThat;
	 *
	 * @param profileService to manage profiles.
	 * @param repository     to manage the tasks.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldMergeStoredModels(TasksRepository repository, WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		testContext.assertComplete(this.createModelExample(1, profileService)).setHandler(targetToStore -> {

			testContext.assertComplete(this.createModelExample(2, profileService)).setHandler(sourceToStore -> {

				repository.storeTask(targetToStore.result(), testContext.succeeding(target -> {

					repository.storeTask(sourceToStore.result(), testContext.succeeding(source -> {

						testContext.assertComplete(target.merge(source, "codePrefix", profileService))
								.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

									source.taskId = target.taskId;
									source.creationTs = target.creationTs;
									assertThat(merged).isNotEqualTo(target).isEqualTo(source);
									testContext.completeNow();
								})));

					}));

				}));

			});

		});

	}

	/**
	 * Check that not accept tasks with bad norms.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#validate(String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotBeValidWithABadNorms(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task model = new Task();
		model.norms = new ArrayList<>();
		model.norms.add(new Norm());
		model.norms.add(new Norm());
		model.norms.add(new Norm());
		model.norms.get(1).attribute = ValidationsTest.STRING_256;
		this.assertFailValidate(model, "norms[1].attribute", profileService, testContext);

	}

	/**
	 * Check that not merge tasks with bad norms.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotBeMergeWithABadNorms(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task source = new Task();
		source.norms = new ArrayList<>();
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.get(1).attribute = ValidationsTest.STRING_256;
		this.assertFailMerge(new Task(), "norms[1].attribute", profileService, source, testContext);

	}

	/**
	 * Check that not merge tasks with duplicated social practice identifiers.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotMergeWithADuplicatedNormIds(WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		final Task source = new Task();
		source.norms = new ArrayList<>();
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.get(1).id = "1";
		source.norms.get(2).id = "1";
		final Task target = new Task();
		target.norms = new ArrayList<>();
		target.norms.add(new Norm());
		target.norms.get(0).id = "1";
		this.assertFailMerge(target, "norms[2].id", profileService, source, testContext);

	}

	/**
	 * Check that not merge profiles with not defined social practice id.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldNotMergeWithNotDefinecNormId(WeNetProfileManagerService profileService,
			VertxTestContext testContext) {

		final Task source = new Task();
		source.norms = new ArrayList<>();
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.get(1).id = "1";
		this.assertFailMerge(new Task(), "norms[1].id", profileService, source, testContext);

	}

	/**
	 * Check merge social practices profiles.
	 *
	 * @param profileService to manage profiles.
	 * @param testContext    context to test.
	 *
	 * @see Task#merge(Task, String, WeNetProfileManagerService)
	 */
	@Test
	public void shouldMergeWithNorms(WeNetProfileManagerService profileService, VertxTestContext testContext) {

		final Task target = new Task();
		target.norms = new ArrayList<>();
		target.norms.add(new Norm());
		target.norms.get(0).id = "1";
		final Task source = new Task();
		source.norms = new ArrayList<>();
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.add(new Norm());
		source.norms.get(1).id = "1";
		testContext.assertComplete(target.merge(source, "codePrefix", profileService))
				.setHandler(testContext.succeeding(merged -> testContext.verify(() -> {

					assertThat(merged.norms).isNotEqualTo(target.norms).isEqualTo(source.norms);
					assertThat(merged.norms.get(0).id).isNotEmpty();
					assertThat(merged.norms.get(1).id).isEqualTo("1");
					assertThat(merged.norms.get(2).id).isNotEmpty();
					testContext.completeNow();
				})));

	}

}
