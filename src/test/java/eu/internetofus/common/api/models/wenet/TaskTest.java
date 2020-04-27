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

import static eu.internetofus.common.api.models.MergesTest.assertCanMerge;
import static eu.internetofus.common.api.models.MergesTest.assertCannotMerge;
import static eu.internetofus.common.api.models.ValidationsTest.assertIsNotValid;
import static eu.internetofus.common.api.models.ValidationsTest.assertIsValid;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.internetofus.common.TimeManager;
import eu.internetofus.common.api.models.ModelTestCase;
import eu.internetofus.common.api.models.ValidationsTest;
import eu.internetofus.common.services.ServiceApiSimulatorServiceOnMemory;
import eu.internetofus.common.services.WeNetProfileManagerServiceOnMemory;
import eu.internetofus.common.services.WeNetTaskManagerService;
import eu.internetofus.common.services.WeNetTaskManagerServiceOnMemory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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
		ServiceApiSimulatorServiceOnMemory.register(vertx);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Task createModelExample(int index) {

		assert index >= 0;
		final Task model = new Task();
		model.taskTypeId = "taskTypeId" + index;
		model.requesterId = "requesterId" + index;
		model.appId = "appId" + index;
		model.goal = new TaskGoalTest().createModelExample(index);
		model.startTs = TimeManager.now() + 10000 + 10000 * index;
		model.endTs = model.startTs + 300000l + index;
		model.deadlineTs = model.startTs + 200000l + index;
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
	@ParameterizedTest(name = "The model example {0} has to be valid")
	@ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
	public void shouldExampleBeValid(int index, Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(index, vertx, testContext,
				testContext.succeeding(model -> assertIsValid(model, vertx, testContext)));

	}

	/**
	 * Check that an empty task is valid.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldEmptyTaskBeValid(Vertx vertx, VertxTestContext testContext) {

		assertIsValid(new Task(), vertx, testContext);

	}

	/**
	 * Check that a task with only an identifier is valid.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext test context to use.
	 *
	 * @see Task#validate(String, Vertx)
	 */
	@Test
	public void shouldTaskWithOnlyIdBeValid(Vertx vertx, VertxTestContext testContext) {

		final Task model = new Task();
		model.id = UUID.randomUUID().toString();
		assertIsValid(model, vertx, testContext);

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

		StoreServices.storeProfile(new WeNetUserProfile(), vertx, testContext, testContext.succeeding(profile -> {

			StoreServices.storeTaskType(new TaskType(), vertx, testContext, testContext.succeeding(taskType -> {

				StoreServices.storeApp(new App(), vertx, testContext, testContext.succeeding(app -> {

					final Task model = this.createModelExample(index);
					model.requesterId = profile.id;
					model.taskTypeId = taskType.id;
					model.appId = app.appId;
					createHandler.handle(Future.succeededFuture(model));

				}));
			}));
		}));

	}

	/**
	 * Check that the model with id is not valid.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#validate(String, Vertx)
	 */
	@Test
	public void shouldNotBeValidWithAnExistingId(Vertx vertx, VertxTestContext testContext) {

		WeNetTaskManagerService.createProxy(vertx).createTask(new JsonObject(), testContext.succeeding(created -> {

			final Task model = new Task();
			model.id = created.getString("id");
			assertIsNotValid(model, "id", vertx, testContext);

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
		model.norms.add(new NormTest().createModelExample(1));
		model.norms.add(new NormTest().createModelExample(2));
		model.norms.add(new NormTest().createModelExample(3));
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
		model.attributes.add(new TaskAttributeTest().createModelExample(1));
		model.attributes.add(new TaskAttributeTest().createModelExample(2));
		model.attributes.add(new TaskAttributeTest().createModelExample(3));
		model.attributes.get(1).name = ValidationsTest.STRING_256;
		assertIsNotValid(model, "attributes[1].name", vertx, testContext);

	}

	/**
	 * Check merge stored profiles.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldMergeStoredModels(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				this.createModelExample(2, vertx, testContext, testContext.succeeding(sourceToStore -> {

					StoreServices.storeTask(sourceToStore, vertx, testContext, testContext.succeeding(source -> {

						assertCanMerge(target, source, vertx, testContext, merged -> {

							source.id = target.id;
							source._creationTs = target._creationTs;
							source._lastUpdateTs = target._lastUpdateTs;
							assertThat(merged).isNotEqualTo(target).isEqualTo(source);

						});
					}));
				}));

			}));
		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#taskTypeId}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeTaskTypeId(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				StoreServices.storeTaskType(new TaskType(), vertx, testContext, testContext.succeeding(taskType -> {

					final Task source = new Task();
					source.taskTypeId = taskType.id;
					assertCanMerge(target, source, vertx, testContext, merged -> {

						assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
						target.taskTypeId = taskType.id;
						assertThat(merged).isEqualTo(target).isNotEqualTo(source);
					});

				}));
			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#taskTypeId} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadTaskTypeId(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.taskTypeId = "undefined-task-type-identifier";
				assertCannotMerge(target, source, "taskTypeId", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#requesterId}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeRequesterId(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				StoreServices.storeProfile(new WeNetUserProfile(), vertx, testContext, testContext.succeeding(requester -> {

					final Task source = new Task();
					source.requesterId = requester.id;
					assertCanMerge(target, source, vertx, testContext, merged -> {

						assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
						target.requesterId = requester.id;
						assertThat(merged).isEqualTo(target).isNotEqualTo(source);
					});

				}));
			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#requesterId} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadRequesterId(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.requesterId = "undefined-requester-identifier";
				assertCannotMerge(target, source, "requesterId", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#appId}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeAppId(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				StoreServices.storeApp(new App(), vertx, testContext, testContext.succeeding(app -> {

					final Task source = new Task();
					source.appId = app.appId;
					assertCanMerge(target, source, vertx, testContext, merged -> {

						assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
						target.appId = app.appId;
						assertThat(merged).isEqualTo(target).isNotEqualTo(source);
					});

				}));
			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#appId} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadAppId(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.appId = "undefined-application-identifier";
				assertCannotMerge(target, source, "appId", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#goal}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeGoal(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.goal = new TaskGoalTest().createModelExample(2);
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.goal = new TaskGoalTest().createModelExample(2);
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#goal} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadGoal(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.goal = new TaskGoal();
				source.goal.name = ValidationsTest.STRING_256;
				assertCannotMerge(target, source, "goal.name", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#startTs}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeStartTs(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.startTs = target.startTs + 1;
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.startTs++;
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#startTs} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadStartTs(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.startTs = -1l;
				assertCannotMerge(target, source, "startTs", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#endTs}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeEndTs(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.endTs = target.endTs + 1;
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.endTs++;
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#endTs} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadEndTs(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.endTs = -1l;
				assertCannotMerge(target, source, "endTs", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#deadlineTs}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeDeadlineTs(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.deadlineTs = target.deadlineTs + 1;
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.deadlineTs++;
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#deadlineTs} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadDeadlineTs(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.deadlineTs = -1l;
				assertCannotMerge(target, source, "deadlineTs", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#norms}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeNewNorms(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.norms = new ArrayList<>();
				source.norms.add(new NormTest().createModelExample(2));
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.norms.clear();
					target.norms.add(new NormTest().createModelExample(2));
					target.norms.get(0).id = merged.norms.get(0).id;
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#norms}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeAddingNewNorm(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.norms = new ArrayList<>();
				source.norms.add(new NormTest().createModelExample(2));
				source.norms.addAll(target.norms);
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.norms.add(0, new NormTest().createModelExample(2));
					target.norms.get(0).id = merged.norms.get(0).id;
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#norms}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeDeleteNorm(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			targetToStore.norms.get(0).id = UUID.randomUUID().toString();
			final Norm normToMantain = new NormTest().createModelExample(2);
			normToMantain.id = UUID.randomUUID().toString();
			targetToStore.norms.add(normToMantain);

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.norms = new ArrayList<>();
				source.norms.add(new Norm());
				source.norms.get(0).id = normToMantain.id;
				source.norms.get(0).attribute = "New attribute";
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.norms.clear();
					normToMantain.attribute = "New attribute";
					target.norms.add(normToMantain);
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);

				});

			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#norms} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadNorms(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.norms = new ArrayList<>();
				source.norms.add(new Norm());
				source.norms.get(0).attribute = ValidationsTest.STRING_256;
				assertCannotMerge(target, source, "norms[0].attribute", vertx, testContext);
			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#attributes}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeNewAttributes(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.attributes = new ArrayList<>();
				source.attributes.add(new TaskAttributeTest().createModelExample(2));
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.attributes.clear();
					target.attributes.add(new TaskAttributeTest().createModelExample(2));
					target.attributes.get(0).name = merged.attributes.get(0).name;
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#attributes}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeAddingNewAttribute(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.attributes = new ArrayList<>();
				source.attributes.add(new TaskAttributeTest().createModelExample(2));
				source.attributes.addAll(target.attributes);
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.attributes.add(0, new TaskAttributeTest().createModelExample(2));
					target.attributes.get(0).name = merged.attributes.get(0).name;
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);
				});

			}));

		}));

	}

	/**
	 * Check that merge when only is modified the {@link Task#attributes}.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldOnlyMergeDeleteAttribute(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			targetToStore.attributes.get(0).name = UUID.randomUUID().toString();
			final TaskAttribute attributeToMantain = new TaskAttributeTest().createModelExample(2);
			attributeToMantain.name = UUID.randomUUID().toString();
			targetToStore.attributes.add(attributeToMantain);

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.attributes = new ArrayList<>();
				source.attributes.add(new TaskAttribute());
				source.attributes.get(0).name = attributeToMantain.name;
				source.attributes.get(0).value = "TaskAttribute";
				assertCanMerge(target, source, vertx, testContext, merged -> {

					assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
					target.attributes.clear();
					attributeToMantain.value = "TaskAttribute";
					target.attributes.add(attributeToMantain);
					assertThat(merged).isEqualTo(target).isNotEqualTo(source);

				});

			}));

		}));

	}

	/**
	 * Check that can not merge when the {@link Task#attributes} has a bas value.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context to test.
	 *
	 * @see WeNetUserProfile#merge(WeNetUserProfile, String, Vertx)
	 */
	@Test
	public void shouldNotMergeWithBadAttributes(Vertx vertx, VertxTestContext testContext) {

		this.createModelExample(1, vertx, testContext, testContext.succeeding(targetToStore -> {

			StoreServices.storeTask(targetToStore, vertx, testContext, testContext.succeeding(target -> {

				final Task source = new Task();
				source.attributes = new ArrayList<>();
				source.attributes.add(new TaskAttribute());
				source.attributes.get(0).name = ValidationsTest.STRING_256;
				assertCannotMerge(target, source, "attributes[0].name", vertx, testContext);
			}));

		}));

	}

}
