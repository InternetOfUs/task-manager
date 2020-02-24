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

import static eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension.Asserts.assertThatBodyIs;
import static io.vertx.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.api.ErrorMessage;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import eu.internetofus.wenet_task_manager.services.WeNetProfileManagerService;
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
	 * @param repository  to access the tasks.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTask(TasksRepository repository, WebClient client, VertxTestContext testContext) {

		repository.storeTask(new TaskTest().createModelExample(1), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.GET, Tasks.PATH + "/" + task.taskId).expect(res -> testContext.verify(() -> {

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
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreBadTask(WebClient client, VertxTestContext testContext) {

		final Task task = new Task();
		task.taskId = UUID.randomUUID().toString();
		testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
			assertThat(error.code).isNotEmpty().isEqualTo("bad_task.taskId");
			assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
			testContext.completeNow();

		}).sendJson(task.toJsonObject(), testContext);
	}

	/**
	 * Verify that store a task.
	 *
	 * @param repository     that manage the tasks.
	 * @param profileManager service to create user profiles.
	 * @param client         to connect to the server.
	 * @param testContext    context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTask(TasksRepository repository, WeNetProfileManagerService profileManager, WebClient client,
			VertxTestContext testContext) {

		testContext.assertComplete(new TaskTest().createModelExample(1, profileManager)).setHandler(created -> {

			final Task task = created.result();
			testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
				final Task stored = assertThatBodyIs(Task.class, res);
				assertThat(stored).isNotNull().isNotEqualTo(task);
				task.taskId = stored.taskId;
				task.creationTs = stored.creationTs;
				task.norms.get(0).id = stored.norms.get(0).id;
				assertThat(stored).isEqualTo(task);
				repository.searchTask(stored.taskId, testContext.succeeding(foundTask -> testContext.verify(() -> {

					assertThat(foundTask).isEqualTo(stored);
					testContext.completeNow();

				})));

			}).sendJson(task.toJsonObject(), testContext);

		});
	}

	/**
	 * Verify that store an empty task.
	 *
	 * @param repository  that manage the tasks.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreEmptyTask(TasksRepository repository, WebClient client, VertxTestContext testContext) {

		final Task task = new Task();
		testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final Task stored = assertThatBodyIs(Task.class, res);
			assertThat(stored).isNotNull().isNotEqualTo(task);
			task.taskId = stored.taskId;
			task.creationTs = stored.creationTs;
			assertThat(stored).isEqualTo(task);
			repository.searchTask(stored.taskId, testContext.succeeding(foundTask -> testContext.verify(() -> {

				assertThat(foundTask).isEqualTo(stored);
				testContext.completeNow();

			})));

		}).sendJson(task.toJsonObject(), testContext);

	}

	/**
	 * Verify that store a simple task.
	 *
	 * @param repository  that manage the tasks.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#createTask(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreSimpleTask(TasksRepository repository, WebClient client, VertxTestContext testContext) {

		final Task task = new TaskTest().createModelExample(1);
		testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final Task stored = assertThatBodyIs(Task.class, res);
			assertThat(stored).isNotNull().isNotEqualTo(task);
			task.taskId = stored.taskId;
			assertThat(stored).isNotNull().isNotEqualTo(task);
			task.creationTs = stored.creationTs;
			task.norms.get(0).id = stored.norms.get(0).id;
			assertThat(stored).isEqualTo(task);
			repository.searchTask(stored.taskId, testContext.succeeding(foundTask -> testContext.verify(() -> {

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

		final Task task = new TaskTest().createModelExample(1);
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
	 * @param repository  that manage the tasks.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskWithANotTaskObject(TasksRepository repository, WebClient client,
			VertxTestContext testContext) {

		repository.storeTask(new TaskTest().createModelExample(1), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + task.taskId).expect(res -> {

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
	 * @param repository  that manage the tasks.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskBecauseNotChangesHasDone(TasksRepository repository, WebClient client,
			VertxTestContext testContext) {

		repository.storeTask(new TaskTest().createModelExample(1), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + task.taskId).expect(res -> {

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
	 * @param repository  that manage the tasks.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskBecauseBadSource(TasksRepository repository, WebClient client,
			VertxTestContext testContext) {

		repository.storeTask(new TaskTest().createModelExample(1), testContext.succeeding(task -> {

			testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + task.taskId).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
				final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
				assertThat(error.code).isNotEmpty().endsWith(".requesterUserId");
				assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
				testContext.completeNow();

			}).sendJson(new JsonObject().put("requesterUserId", UUID.randomUUID().toString()), testContext);
		}));

	}

	/**
	 * Verify that can update a complex task with another.
	 *
	 * @param repository     that manage the tasks.
	 * @param profileManager service to create user profiles.
	 * @param client         to connect to the server.
	 * @param testContext    context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateTask(TasksRepository repository, WeNetProfileManagerService profileManager, WebClient client,
			VertxTestContext testContext) {

		testContext.assertComplete(new TaskTest().createModelExample(23, profileManager)).setHandler(createdTask -> {

			final Task created = createdTask.result();
			testContext.assertComplete(created.validate("codePrefix", profileManager)).setHandler(validation -> {

				repository.storeTask(created, testContext.succeeding(storedTask -> {

					final Task newTask = new TaskTest().createModelExample(2);
					newTask.taskId = UUID.randomUUID().toString();
					testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + storedTask.taskId)
							.expect(res -> testContext.verify(() -> {

								assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
								final Task updated = assertThatBodyIs(Task.class, res);
								assertThat(updated).isNotEqualTo(storedTask).isNotEqualTo(newTask);
								newTask.taskId = storedTask.taskId;
								newTask.creationTs = storedTask.creationTs;
								newTask.requesterUserId = storedTask.requesterUserId;
								newTask.norms.get(0).id = updated.norms.get(0).id;
								assertThat(updated).isEqualTo(newTask);
								testContext.completeNow();

							})).sendJson(newTask.toJsonObject(), testContext);
				}));
			});
		});

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
	 * @param repository  to access the tasks.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest,
	 *      io.vertx.core.Handler)
	 */
	@Test
	public void shouldDeleteTask(TasksRepository repository, WebClient client, VertxTestContext testContext) {

		repository.storeTask(new Task(), testContext.succeeding(storedTask -> {

			testRequest(client, HttpMethod.DELETE, Tasks.PATH + "/" + storedTask.taskId)
					.expect(res -> testContext.verify(() -> {

						assertThat(res.statusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());
						testContext.completeNow();

					})).send(testContext);

		}));

	}

}
