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

import static eu.internetofus.common.api.HttpResponses.assertThatBodyIs;
import static io.vertx.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.common.api.models.ErrorMessage;
import eu.internetofus.common.api.models.ValidationsTest;
import eu.internetofus.common.api.models.wenet.StoreServices;
import eu.internetofus.common.api.models.wenet.Task;
import eu.internetofus.common.api.models.wenet.TaskGoalTest;
import eu.internetofus.common.api.models.wenet.TaskTest;
import eu.internetofus.common.api.models.wenet.TaskTransaction;
import eu.internetofus.common.api.models.wenet.TaskTransactionTest;
import eu.internetofus.common.api.models.wenet.TaskType;
import eu.internetofus.common.api.models.wenet.TaskTypeTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

/**
 * The integration test over the {@link Tasks}.
 *
 * @see Tasks
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TasksIT {

	/**
	 * Verify that return error when search an undefined task.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotFoundTaskWithAnUndefinedTaskId(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.GET, Tasks.PATH + "/undefined-task-identifier").expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty();
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).send(testContext);
	}

	/**
	 * Verify that return a defined task.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTask(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TasksRepository.createProxy(vertx).storeTask(new Task(), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.GET, Tasks.PATH + "/" + task.id).expect(res -> testContext.verify(() -> {

				assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
				final Task found = assertThatBodyIs(Task.class, res);
				assertThat(found).isEqualTo(task);
				testContext.completeNow();

			})).send(testContext);

		}));

	}

	/**
	 * Verify that can not store a bad task.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreANonTaskObject(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty().isEqualTo("bad_task");
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(new JsonObject().put("udefinedKey", "value"), testContext);
	}

	/**
	 * Verify that can not store a bad task.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreTaskWithExistingId(Vertx vertx, WebClient client, VertxTestContext testContext) {

		StoreServices.storeTaskExample(1, vertx, testContext, testContext.succeeding(created -> {

			testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty().isEqualTo("bad_task.id");
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(created.toJsonObject(), testContext);

		}));
	}

	/**
	 * Verify that store a task.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTaskExample(Vertx vertx, WebClient client, VertxTestContext testContext) {

		new TaskTest().createModelExample(1, vertx, testContext, testContext.succeeding(task -> {
			testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
				final Task stored = assertThatBodyIs(Task.class, res);
				assertThat(stored).isNotNull().isNotEqualTo(task);
				task.id = stored.id;
				assertThat(stored).isNotEqualTo(task);
				task._creationTs = stored._creationTs;
				task._lastUpdateTs = stored._lastUpdateTs;
				assertThat(stored).isNotEqualTo(task);
				task.norms.get(0).id = stored.norms.get(0).id;
				assertThat(stored).isEqualTo(task);
				TasksRepository.createProxy(vertx).searchTask(stored.id,
						testContext.succeeding(foundTask -> testContext.verify(() -> {

							assertThat(foundTask).isEqualTo(stored);
							testContext.completeNow();

						})));

			}).sendJson(task.toJsonObject(), testContext);

		}));

	}

	/**
	 * Verify that store an empty task.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreEmptyTask(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final Task task = new Task();
		testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty();
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(task.toJsonObject(), testContext);

	}

	/**
	 * Verify that return error when try to update an undefined task.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskThatIsNotDefined(WebClient client, VertxTestContext testContext) {

		final Task task = new Task();
		task.goal = new TaskGoalTest().createModelExample(23);
		testRequest(client, HttpMethod.PUT, Tasks.PATH + "/undefined-task-identifier").expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty();
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(task.toJsonObject(), testContext);
	}

	/**
	 * Verify that return error when try to update with a model that is not a task.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskWithANotTaskObject(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TasksRepository.createProxy(vertx).storeTask(new Task(), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + task.id).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty();
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(new JsonObject().put("udefinedKey", "value"), testContext);
		}));
	}

	/**
	 * Verify that not update a task if any change is done.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskBecauseNotChangesHasDone(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TasksRepository.createProxy(vertx).storeTask(new Task(), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + task.id).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty();
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(new JsonObject(), testContext);
		}));

	}

	/**
	 * Verify that not update a task because the source is not valid.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskBecauseBadSource(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TasksRepository.createProxy(vertx).storeTask(new Task(), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + task.id).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty().endsWith(".taskTypeId");
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(new TaskTest().createModelExample(1).toJsonObject(), testContext);
		}));

	}

	/**
	 * Verify that can update a task with another.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateExampleTaskWithAnotherExample(Vertx vertx, WebClient client, VertxTestContext testContext) {

		StoreServices.storeTaskExample(1, vertx, testContext, testContext.succeeding(target -> {

			new TaskTest().createModelExample(2, vertx, testContext, testContext.succeeding(source -> {

				source.id = UUID.randomUUID().toString();
				testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

					assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
					final Task updated = assertThatBodyIs(Task.class, res);
					assertThat(updated).isNotEqualTo(source).isNotEqualTo(target);
					source.id = updated.id;
					source._creationTs = target._creationTs;
					source._lastUpdateTs = updated._lastUpdateTs;
					source.norms.get(0).id = updated.norms.get(0).id;
					assertThat(updated).isEqualTo(source);
					testContext.completeNow();

				})).sendJson(source.toJsonObject(), testContext);
			}));

		}));

	}

	/**
	 * Verify that return error when delete an undefined task.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDeleteTaskWithAnUndefinedTaskId(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.DELETE, Tasks.PATH + "/undefined-task-identifier").expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty();
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).send(testContext);
	}

	/**
	 * Verify that can delete a task.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldDeleteTask(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TasksRepository repository = TasksRepository.createProxy(vertx);
		repository.storeTask(new Task(), testContext.succeeding(storedTask -> {

			testRequest(client, HttpMethod.DELETE, Tasks.PATH + "/" + storedTask.id).expect(res -> testContext.verify(() -> {

				assertThat(res.statusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());
				repository.searchTask(storedTask.id, testContext.failing(error -> testContext.completeNow()));

			})).send(testContext);

		}));

	}

	/**
	 * Verify that only update the middle name of an user.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateOnlyAppIdOnTask(Vertx vertx, WebClient client, VertxTestContext testContext) {

		StoreServices.storeTaskExample(2, vertx, testContext, testContext.succeeding(target -> {

			StoreServices.storeAppExample(1, vertx, testContext, testContext.succeeding(app -> {

				final Task source = new Task();
				source.appId = app.appId;
				testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

					assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
					final Task updated = assertThatBodyIs(Task.class, res);
					assertThat(updated).isNotEqualTo(target).isNotEqualTo(source);
					target._lastUpdateTs = updated._lastUpdateTs;
					target.appId = app.appId;
					assertThat(updated).isEqualTo(target);
					testContext.completeNow();

				})).sendJson(source.toJsonObject(), testContext);
			}));
		}));

	}

	/**
	 * Verify that return error when search an undefined taskType.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotFoundTaskTypeWithAnUndefinedTaskTypeId(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.GET, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/undefined-task-type-identifier")
				.expect(res -> {

					assertThat(res.statusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
					final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
					assertThat(error.code).isNotEmpty();
					assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
					testContext.completeNow();

				}).send(testContext);
	}

	/**
	 * Verify that return a defined taskType.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			testRequest(client, HttpMethod.GET, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + taskType.id)
					.expect(res -> testContext.verify(() -> {

						assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
						final TaskType found = assertThatBodyIs(TaskType.class, res);
						assertThat(found).isEqualTo(taskType);
						testContext.completeNow();

					})).send(testContext);

		}));

	}

	/**
	 * Verify that can not store a bad taskType.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreANonTaskTypeObject(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.POST, Tasks.PATH + "/" + Tasks.TYPES_PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty().isEqualTo("bad_task_type");
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(new JsonObject().put("udefinedKey", "value"), testContext);
	}

	/**
	 * Verify that can not store a bad taskType.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreTaskTypeWithExistingId(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(created -> {

			final TaskType taskType = new TaskTypeTest().createModelExample(1);
			taskType.id = created.id;
			testRequest(client, HttpMethod.POST, Tasks.PATH + "/" + Tasks.TYPES_PATH).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty().isEqualTo("bad_task_type.id");
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(taskType.toJsonObject(), testContext);

		}));
	}

	/**
	 * Verify that store a taskType.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTaskTypeExample(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskTypeTest().createModelExample(1);
		testRequest(client, HttpMethod.POST, Tasks.PATH + "/" + Tasks.TYPES_PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final TaskType stored = assertThatBodyIs(TaskType.class, res);
			assertThat(stored).isNotNull().isNotEqualTo(taskType);
			taskType.id = stored.id;
			assertThat(stored).isNotEqualTo(taskType);
			// taskType._creationTs = stored._creationTs;
			// taskType._lastUpdateTs = stored._lastUpdateTs;
			assertThat(stored).isNotEqualTo(taskType);
			taskType.norms.get(0).id = stored.norms.get(0).id;
			assertThat(stored).isEqualTo(taskType);
			TaskTypesRepository.createProxy(vertx).searchTaskType(stored.id,
					testContext.succeeding(foundTaskType -> testContext.verify(() -> {

						assertThat(foundTaskType).isEqualTo(stored);
						testContext.completeNow();

					})));

		}).sendJson(taskType.toJsonObject(), testContext);

	}

	/**
	 * Verify that not store an empty task type.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreEmptyTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskType();
		testRequest(client, HttpMethod.POST, Tasks.PATH + "/" + Tasks.TYPES_PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			testContext.completeNow();

		}).sendJson(taskType.toJsonObject(), testContext);

	}

	/**
	 * Verify that store a taskType with an identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTaskTypeWithOnlyID(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskTypeTest().createModelExample(1);
		// taskType._creationTs = 0;
		// taskType._lastUpdateTs = 0;
		taskType.id = UUID.randomUUID().toString();
		testRequest(client, HttpMethod.POST, Tasks.PATH + "/" + Tasks.TYPES_PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final TaskType stored = assertThatBodyIs(TaskType.class, res);
			// assertThat(stored).isNotNull().isNotEqualTo(taskType);
			// assertThat(stored).isNotNull().isNotEqualTo(taskType);
			// taskType._creationTs = stored._creationTs;
			// taskType._lastUpdateTs = stored._lastUpdateTs;
			taskType.norms.get(0).id = stored.norms.get(0).id;
			assertThat(stored).isEqualTo(taskType);
			TaskTypesRepository.createProxy(vertx).searchTaskType(stored.id,
					testContext.succeeding(foundTaskType -> testContext.verify(() -> {

						assertThat(foundTaskType).isEqualTo(stored);
						testContext.completeNow();

					})));

		}).sendJson(taskType.toJsonObject(), testContext);

	}

	/**
	 * Verify that return error when try to update an undefined taskType.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeThatIsNotDefined(WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskType();
		taskType.name = "Task type name";
		testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/undefined-task-type-identifier")
				.expect(res -> {

					assertThat(res.statusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
					final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
					assertThat(error.code).isNotEmpty();
					assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
					testContext.completeNow();

				}).sendJson(taskType.toJsonObject(), testContext);
	}

	/**
	 * Verify that return error when try to update with a model that is not a
	 * taskType.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeWithANotTaskTypeObject(Vertx vertx, WebClient client,
			VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + taskType.id).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty();
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(new JsonObject().put("udefinedKey", "value"), testContext);
		}));
	}

	/**
	 * Verify that not update a taskType if any change is done.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeBecauseNotChangesHasDone(Vertx vertx, WebClient client,
			VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + taskType.id).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty();
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(new JsonObject(), testContext);
		}));

	}

	/**
	 * Verify that not update a taskType because the source is not valid.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeBecauseBadSource(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			final TaskType badTaskType = new TaskType();
			badTaskType.name = ValidationsTest.STRING_256;
			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + taskType.id).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty().endsWith(".name");
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(badTaskType.toJsonObject(), testContext);
		}));

	}

	/**
	 * Verify that can update a taskType with another.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateExampleTaskTypeWithAnotherExample(Vertx vertx, WebClient client,
			VertxTestContext testContext) {

		StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

			final TaskType source = new TaskTypeTest().createModelExample(2);

			source.id = UUID.randomUUID().toString();
			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + target.id)
					.expect(res -> testContext.verify(() -> {

						assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
						final TaskType updated = assertThatBodyIs(TaskType.class, res);
						assertThat(updated).isNotEqualTo(source).isNotEqualTo(target);
						source.id = updated.id;
						// source._creationTs = target._creationTs;
						// source._lastUpdateTs = updated._lastUpdateTs;
						source.norms.get(0).id = updated.norms.get(0).id;
						assertThat(updated).isEqualTo(source);
						testContext.completeNow();

					})).sendJson(source.toJsonObject(), testContext);

		}));

	}

	/**
	 * Verify that return error when delete an undefined taskType.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDeleteTaskTypeWithAnUndefinedTaskTypeId(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.DELETE, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/undefined-task-type-identifier")
				.expect(res -> {

					assertThat(res.statusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
					final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
					assertThat(error.code).isNotEmpty();
					assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
					testContext.completeNow();

				}).send(testContext);
	}

	/**
	 * Verify that can delete a taskType.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldDeleteTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskTypesRepository repository = TaskTypesRepository.createProxy(vertx);
		repository.storeTaskType(new TaskType(), testContext.succeeding(storedTaskType -> {

			testRequest(client, HttpMethod.DELETE, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + storedTaskType.id)
					.expect(res -> testContext.verify(() -> {

						assertThat(res.statusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());
						repository.searchTaskType(storedTaskType.id, testContext.failing(error -> testContext.completeNow()));

					})).send(testContext);

		}));

	}

	/**
	 * Verify that only update the task type name.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateOnlyNameOnTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

			final TaskType source = new TaskType();
			source.name = "NEW task type name";
			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + target.id)
					.expect(res -> testContext.verify(() -> {

						assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
						final TaskType updated = assertThatBodyIs(TaskType.class, res);
						assertThat(updated).isNotEqualTo(target).isNotEqualTo(source);
						// target._lastUpdateTs = updated._lastUpdateTs;
						target.name = "NEW task type name";
						assertThat(updated).isEqualTo(target);
						testContext.completeNow();

					})).sendJson(source.toJsonObject(), testContext);
		}));

	}

	/**
	 * Verify that not update the task type because it not produce any change.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateBecasueNotChangedOnTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

			final TaskType source = new TaskType();
			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + target.id)
					.expect(res -> testContext.verify(() -> {

						assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
						final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
						assertThat(error.code).isNotEmpty().isEqualTo("task_type_to_update_equal_to_original");
						assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
						testContext.completeNow();

					})).sendJson(source.toJsonObject(), testContext);
		}));

	}

	/**
	 * Verify that can not do a transaction with a bad task transaction JSON object.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#doTaskTransaction(JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDoTaskTransactionWithAnonTaskTransactionObject(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.POST, Tasks.PATH + Tasks.TRANSACTIONS_PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty().isEqualTo("bad_task_transaction");
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(new JsonObject().put("udefinedKey", "value"), testContext);
	}

	/**
	 * Verify that can not do a transaction with an empty transaction.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#doTaskTransaction(JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDoTransactionIfItIsEmpty(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskTransaction taskTransaction = new TaskTransaction();
		testRequest(client, HttpMethod.POST, Tasks.PATH + Tasks.TRANSACTIONS_PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty().isEqualTo("bad_task_transaction.taskId");
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(taskTransaction.toJsonObject(), testContext);

	}

	/**
	 * Verify that can not do a transaction with over an undefined task.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#doTaskTransaction(JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDoTransactionIfTaskIsNotDefined(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskTransaction taskTransaction = new TaskTransactionTest().createModelExample(1);
		testRequest(client, HttpMethod.POST, Tasks.PATH + Tasks.TRANSACTIONS_PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty().isEqualTo("bad_task_transaction.taskId");
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(taskTransaction.toJsonObject(), testContext);

	}

}
