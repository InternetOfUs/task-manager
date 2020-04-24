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
import eu.internetofus.common.api.models.wenet.StoreServices;
import eu.internetofus.common.api.models.wenet.Task;
import eu.internetofus.common.api.models.wenet.TaskGoalTest;
import eu.internetofus.common.api.models.wenet.TaskTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
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

		TasksRepository.createProxy(vertx).storeTask(new Task(), testContext.succeeding(created -> {

			final Task task = new Task();
			task.id = created.id;
			testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty().isEqualTo("bad_task.id");
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(task.toJsonObject(), testContext);

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
	public void shouldStoreEmptyTask(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final Task task = new Task();
		task._creationTs = 0;
		task._lastUpdateTs = 1;
		testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final Task stored = assertThatBodyIs(Task.class, res);
			assertThat(stored).isNotNull().isNotEqualTo(task);
			task.id = stored.id;
			assertThat(stored).isNotNull().isNotEqualTo(task);
			task._creationTs = stored._creationTs;
			task._lastUpdateTs = stored._lastUpdateTs;
			assertThat(stored).isEqualTo(task);
			TasksRepository.createProxy(vertx).searchTask(stored.id,
					testContext.succeeding(foundTask -> testContext.verify(() -> {

						assertThat(foundTask).isEqualTo(stored);
						testContext.completeNow();

					})));

		}).sendJson(task.toJsonObject(), testContext);

	}

	/**
	 * Verify that store a task with an identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTaskWithOnlyID(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final Task task = new Task();
		task._creationTs = 0;
		task._lastUpdateTs = 0;
		task.id = UUID.randomUUID().toString();
		testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final Task stored = assertThatBodyIs(Task.class, res);
			assertThat(stored).isNotNull().isNotEqualTo(task);
			task.id = stored.id;
			assertThat(stored).isNotNull().isNotEqualTo(task);
			task._creationTs = stored._creationTs;
			task._lastUpdateTs = stored._lastUpdateTs;
			assertThat(stored).isEqualTo(task);
			TasksRepository.createProxy(vertx).searchTask(stored.id,
					testContext.succeeding(foundTask -> testContext.verify(() -> {

						assertThat(foundTask).isEqualTo(stored);
						testContext.completeNow();

					})));

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

		TasksRepository.createProxy(vertx).storeTask(new Task(), testContext.succeeding(target -> {

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

}
