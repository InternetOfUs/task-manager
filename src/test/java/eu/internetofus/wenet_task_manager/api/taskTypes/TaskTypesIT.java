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

package eu.internetofus.wenet_task_manager.api.taskTypes;

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
import eu.internetofus.common.api.models.wenet.TaskType;
import eu.internetofus.common.api.models.wenet.TaskTypeTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

/**
 * The integration test over the {@link TaskTypes}.
 *
 * @see TaskTypes
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TaskTypesIT {

	/**
	 * Verify that return error when search an undefined taskType.
	 *
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see TaskTypes#retrieveTaskType(String,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotFoundTaskTypeWithAnUndefinedTaskTypeId(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.GET, TaskTypes.PATH + "/undefined-task-type-identifier").expect(res -> {

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
	 * @see TaskTypes#retrieveTaskType(String,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			testRequest(client, HttpMethod.GET, TaskTypes.PATH + "/" + taskType.id).expect(res -> testContext.verify(() -> {

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
	 * @see TaskTypes#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreANonTaskTypeObject(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.POST, TaskTypes.PATH).expect(res -> {

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
	 * @see TaskTypes#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreTaskTypeWithExistingId(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(created -> {

			final TaskType taskType = new TaskType();
			taskType.id = created.id;
			testRequest(client, HttpMethod.POST, TaskTypes.PATH).expect(res -> {

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
	 * @see TaskTypes#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTaskTypeExample(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskTypeTest().createModelExample(1);
		testRequest(client, HttpMethod.POST, TaskTypes.PATH).expect(res -> {

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
	 * Verify that store an empty taskType.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see TaskTypes#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreEmptyTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskType();
		// taskType._creationTs = 0;
		// taskType._lastUpdateTs = 1;
		testRequest(client, HttpMethod.POST, TaskTypes.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final TaskType stored = assertThatBodyIs(TaskType.class, res);
			assertThat(stored).isNotNull().isNotEqualTo(taskType);
			taskType.id = stored.id;
			// assertThat(stored).isNotNull().isNotEqualTo(taskType);
			// taskType._creationTs = stored._creationTs;
			// taskType._lastUpdateTs = stored._lastUpdateTs;
			assertThat(stored).isEqualTo(taskType);
			TaskTypesRepository.createProxy(vertx).searchTaskType(stored.id,
					testContext.succeeding(foundTaskType -> testContext.verify(() -> {

						assertThat(foundTaskType).isEqualTo(stored);
						testContext.completeNow();

					})));

		}).sendJson(taskType.toJsonObject(), testContext);

	}

	/**
	 * Verify that store a taskType with an identifier.
	 *
	 * @param vertx       event bus to use.
	 * @param client      to connect to the server.
	 * @param testContext context to test.
	 *
	 * @see TaskTypes#createTaskType(io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTaskTypeWithOnlyID(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskType();
		// taskType._creationTs = 0;
		// taskType._lastUpdateTs = 0;
		taskType.id = UUID.randomUUID().toString();
		testRequest(client, HttpMethod.POST, TaskTypes.PATH).expect(res -> {

			assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
			final TaskType stored = assertThatBodyIs(TaskType.class, res);
			// assertThat(stored).isNotNull().isNotEqualTo(taskType);
			// assertThat(stored).isNotNull().isNotEqualTo(taskType);
			// taskType._creationTs = stored._creationTs;
			// taskType._lastUpdateTs = stored._lastUpdateTs;
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
	 * @see TaskTypes#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeThatIsNotDefined(WebClient client, VertxTestContext testContext) {

		final TaskType taskType = new TaskType();
		taskType.name = "Task type name";
		testRequest(client, HttpMethod.PUT, TaskTypes.PATH + "/undefined-task-type-identifier").expect(res -> {

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
	 * @see TaskTypes#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeWithANotTaskTypeObject(Vertx vertx, WebClient client,
			VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			testRequest(client, HttpMethod.PUT, TaskTypes.PATH + "/" + taskType.id).expect(res -> {

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
	 * @see TaskTypes#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeBecauseNotChangesHasDone(Vertx vertx, WebClient client,
			VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			testRequest(client, HttpMethod.PUT, TaskTypes.PATH + "/" + taskType.id).expect(res -> {

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
	 * @see TaskTypes#updateTaskType(String, io.vertx.core.json.JsonObject,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateTaskTypeBecauseBadSource(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

			final TaskType badTaskType = new TaskType();
			badTaskType.name = ValidationsTest.STRING_256;
			testRequest(client, HttpMethod.PUT, TaskTypes.PATH + "/" + taskType.id).expect(res -> {

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
	 * @see TaskTypes#retrieveTaskType(String,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateExampleTaskTypeWithAnotherExample(Vertx vertx, WebClient client,
			VertxTestContext testContext) {

		StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

			final TaskType source = new TaskTypeTest().createModelExample(2);

			source.id = UUID.randomUUID().toString();
			testRequest(client, HttpMethod.PUT, TaskTypes.PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

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
	 * @see TaskTypes#retrieveTaskType(String,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDeleteTaskTypeWithAnUndefinedTaskTypeId(WebClient client, VertxTestContext testContext) {

		testRequest(client, HttpMethod.DELETE, TaskTypes.PATH + "/undefined-task-type-identifier").expect(res -> {

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
	 * @see TaskTypes#retrieveTaskType(String,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldDeleteTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final TaskTypesRepository repository = TaskTypesRepository.createProxy(vertx);
		repository.storeTaskType(new TaskType(), testContext.succeeding(storedTaskType -> {

			testRequest(client, HttpMethod.DELETE, TaskTypes.PATH + "/" + storedTaskType.id)
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
	 * @see TaskTypes#retrieveTaskType(String,
	 *      io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateOnlyNameOnTaskType(Vertx vertx, WebClient client, VertxTestContext testContext) {

		TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(target -> {

			final TaskType source = new TaskType();
			source.name = "NEW task type name";
			testRequest(client, HttpMethod.PUT, TaskTypes.PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

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

}
