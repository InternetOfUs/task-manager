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

package eu.internetofus.wenet_task_manager.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.components.task_manager.TaskTypeTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;

/**
 * Integration test over the {@link TaskTypesRepository}.
 *
 * @see TaskTypesRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TaskTypesRepositoryIT {

	/**
	 * Verify that can not found a task type if it is not defined.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotFoundUndefinedTaskType(Vertx vertx, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).searchTaskType("undefined user identifier", testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can not found a task type object if it is not defined.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#searchTaskTypeObject(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotFoundUndefinedTaskTypeObject(Vertx vertx, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).searchTaskTypeObject("undefined user identifier",
				testContext.failing(failed -> {
					testContext.completeNow();
				}));

	}

	/**
	 * Verify that can found a task type.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTaskType(Vertx vertx, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(storedTaskType -> {

			TaskTypesRepository.createProxy(vertx).searchTaskType(storedTaskType.id,
					testContext.succeeding(foundTaskType -> testContext.verify(() -> {
						assertThat(foundTaskType).isEqualTo(storedTaskType);
						testContext.completeNow();
					})));

		}));

	}

	/**
	 * Verify that can found a task type object.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#searchTaskTypeObject(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTaskTypeObject(Vertx vertx, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new JsonObject(), testContext.succeeding(storedTaskType -> {

			TaskTypesRepository.createProxy(vertx).searchTaskTypeObject(storedTaskType.getString("id"),
					testContext.succeeding(foundTaskType -> testContext.verify(() -> {
						assertThat(foundTaskType).isEqualTo(storedTaskType);
						testContext.completeNow();
					})));

		}));

	}

	/**
	 * Verify that can not store a task type that can not be an object.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#storeTaskType(TaskType, Handler)
	 */
	@Test
	public void shouldNotStoreATaskTypeThatCanNotBeAnObject(Vertx vertx, VertxTestContext testContext) {

		final TaskType taskType = new TaskType() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public JsonObject toJsonObject() {

				return null;
			}
		};
		taskType.id = "undefined user identifier";
		TaskTypesRepository.createProxy(vertx).storeTaskType(taskType, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can store a task type.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#storeTaskType(TaskType, Handler)
	 */
	@Test
	public void shouldStoreTaskType(Vertx vertx, VertxTestContext testContext) {

		final TaskType taskType = new TaskType();
		// taskType._creationTs = 0;
		// taskType._lastUpdateTs = 1;
		// final long now = TimeManager.now();
		TaskTypesRepository.createProxy(vertx).storeTaskType(taskType,
				testContext.succeeding(storedTaskType -> testContext.verify(() -> {

					assertThat(storedTaskType).isNotNull();
					assertThat(storedTaskType.id).isNotEmpty();
					// assertThat(storedTaskType._creationTs).isNotEqualTo(0).isGreaterThanOrEqualTo(now);
					// assertThat(storedTaskType._lastUpdateTs).isNotEqualTo(1).isGreaterThanOrEqualTo(now);
					testContext.completeNow();
				})));

	}

	/**
	 * Verify that can store a task type.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#storeTaskType(TaskType, Handler)
	 */
	@Test
	public void shouldStoreTaskTypeWithAnId(Vertx vertx, VertxTestContext testContext) {

		final String id = UUID.randomUUID().toString();
		final TaskType taskType = new TaskType();
		taskType.id = id;
		// taskType._creationTs = 0;
		// taskType._lastUpdateTs = 1;
		// final long now = TimeManager.now();
		TaskTypesRepository.createProxy(vertx).storeTaskType(taskType,
				testContext.succeeding(storedTaskType -> testContext.verify(() -> {

					assertThat(storedTaskType.id).isEqualTo(id);
					// assertThat(storedTaskType._creationTs).isNotEqualTo(0).isGreaterThanOrEqualTo(now);
					// assertThat(storedTaskType._lastUpdateTs).isNotEqualTo(1).isGreaterThanOrEqualTo(now);
					testContext.completeNow();
				})));

	}

	/**
	 * Verify that can store a task type with an id of an stored taskType.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#storeTaskType(TaskType, Handler)
	 */
	@Test
	public void shouldNotStoreTwoTaskTypeWithTheSameId(Vertx vertx, VertxTestContext testContext) {

		final String id = UUID.randomUUID().toString();
		final TaskType taskType = new TaskType();
		taskType.id = id;
		TaskTypesRepository.createProxy(vertx).storeTaskType(taskType,
				testContext.succeeding(storedTaskType -> testContext.verify(() -> {

					TaskTypesRepository.createProxy(vertx).storeTaskType(taskType,
							testContext.failing(error -> testContext.completeNow()));

				})));

	}

	/**
	 * Verify that can store a task type object.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#storeTaskType(TaskType, Handler)
	 */
	@Test
	public void shouldStoreTaskTypeObject(Vertx vertx, VertxTestContext testContext) {

		// final long now = TimeManager.now();
		TaskTypesRepository.createProxy(vertx).storeTaskType(new JsonObject(),
				testContext.succeeding(storedTaskType -> testContext.verify(() -> {

					assertThat(storedTaskType).isNotNull();
					final String id = storedTaskType.getString("id");
					assertThat(id).isNotEmpty();
					// assertThat(storedTaskType.getLong("_creationTs",
					// 0l)).isNotEqualTo(0).isGreaterThanOrEqualTo(now);
					// assertThat(storedTaskType.getLong("_lastUpdateTs",
					// 1l)).isNotEqualTo(1).isGreaterThanOrEqualTo(now);
					testContext.completeNow();
				})));

	}

	/**
	 * Verify that can not update a task type if it is not defined.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#updateTaskType(TaskType, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateUndefinedTaskType(Vertx vertx, VertxTestContext testContext) {

		final TaskType taskType = new TaskType();
		taskType.id = "undefined user identifier";
		TaskTypesRepository.createProxy(vertx).updateTaskType(taskType, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can not update a task type if it is not defined.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#updateTaskType(JsonObject, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateUndefinedTaskTypeObject(Vertx vertx, VertxTestContext testContext) {

		final JsonObject taskType = new JsonObject().put("id", "undefined user identifier");
		TaskTypesRepository.createProxy(vertx).updateTaskType(taskType, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can not update a task type if it is not defined.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#updateTaskType(TaskType, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateATaskTypeThatCanNotBeAnObject(Vertx vertx, VertxTestContext testContext) {

		final TaskType taskType = new TaskType() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public JsonObject toJsonObject() {

				return null;
			}
		};
		taskType.id = "undefined user identifier";
		TaskTypesRepository.createProxy(vertx).updateTaskType(taskType, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can update a task type.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#updateTaskType(TaskType, io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateTaskType(Vertx vertx, VertxTestContext testContext) {

		final TaskType taskType = new TaskTypeTest().createModelExample(1);
		TaskTypesRepository.createProxy(vertx).storeTaskType(taskType,
				testContext.succeeding(stored -> testContext.verify(() -> {

					// final long now = TimeManager.now();
					final TaskType update = new TaskTypeTest().createModelExample(23);
					update.id = stored.id;
					// update._creationTs = stored._creationTs;
					// update._lastUpdateTs = 1;
					TaskTypesRepository.createProxy(vertx).updateTaskType(update,
							testContext.succeeding(empty -> testContext.verify(() -> {

								TaskTypesRepository.createProxy(vertx).searchTaskType(stored.id,
										testContext.succeeding(foundTaskType -> testContext.verify(() -> {

											assertThat(stored).isNotNull();
											assertThat(foundTaskType.id).isNotEmpty().isEqualTo(stored.id);
											// assertThat(foundTaskType._creationTs).isEqualTo(stored._creationTs);
											// assertThat(foundTaskType._lastUpdateTs).isGreaterThanOrEqualTo(now);
											// update._lastUpdateTs = foundTaskType._lastUpdateTs;
											assertThat(foundTaskType).isEqualTo(update);
											testContext.completeNow();
										})));
							})));

				})));

	}

	/**
	 * Verify that update a defined task type object.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#updateTaskType(JsonObject, io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateTaskTypeObject(Vertx vertx, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new JsonObject().put("name", "Task type name"),
				testContext.succeeding(stored -> testContext.verify(() -> {

					final String id = stored.getString("id");
					final JsonObject update = new JsonObject().put("id", id).put("description", "Task type description");
					TaskTypesRepository.createProxy(vertx).updateTaskType(update,
							testContext.succeeding(empty -> testContext.verify(() -> {

								TaskTypesRepository.createProxy(vertx).searchTaskTypeObject(id,
										testContext.succeeding(foundTaskType -> testContext.verify(() -> {
											// stored.put("_lastUpdateTs", foundTaskType.getLong("_lastUpdateTs"));
											stored.put("description", "Task type description");
											assertThat(foundTaskType).isEqualTo(stored);
											testContext.completeNow();
										})));
							})));

				})));

	}

	/**
	 * Verify that can not delete a task type if it is not defined.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDeleteUndefinedTaskType(Vertx vertx, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).deleteTaskType("undefined user identifier", testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can delete a taskm type.
	 *
	 * @param vertx       event bus to use.
	 * @param testContext context that executes the test.
	 *
	 * @see TaskTypesRepository#updateTaskType(JsonObject, io.vertx.core.Handler)
	 */
	@Test
	public void shouldDeleteTaskType(Vertx vertx, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new JsonObject(), testContext.succeeding(stored -> {

			final String id = stored.getString("id");
			TaskTypesRepository.createProxy(vertx).deleteTaskType(id, testContext.succeeding(success -> {

				TaskTypesRepository.createProxy(vertx).searchTaskTypeObject(id, testContext.failing(search -> {

					testContext.completeNow();

				}));

			}));

		}));

	}

}
