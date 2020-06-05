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

import static eu.internetofus.common.vertx.HttpResponses.assertThatBodyIs;
import static io.vertx.junit5.web.TestRequest.queryParam;
import static io.vertx.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.internetofus.common.components.ErrorMessage;
import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.ValidationsTest;
import eu.internetofus.common.components.profile_manager.WeNetUserProfile;
import eu.internetofus.common.components.service.App;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskGoalTest;
import eu.internetofus.common.components.task_manager.TaskTest;
import eu.internetofus.common.components.task_manager.TaskTransaction;
import eu.internetofus.common.components.task_manager.TaskTransactionTest;
import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.components.task_manager.TaskTypeTest;
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
   * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundTaskWithAnUndefinedTaskId(final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldFoundTask(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#createTask(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreANonTaskObject(final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#createTask(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreTaskWithExistingId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#createTask(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldStoreTaskExample(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
        TasksRepository.createProxy(vertx).searchTask(stored.id, testContext.succeeding(foundTask -> testContext.verify(() -> {

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
   * @see Tasks#createTask(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreEmptyTask(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskThatIsNotDefined(final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskWithANotTaskObject(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskBecauseNotChangesHasDone(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#updateTask(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskBecauseBadSource(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldUpdateExampleTaskWithAnotherExample(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDeleteTaskWithAnUndefinedTaskId(final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldDeleteTask(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldUpdateOnlyAppIdOnTask(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundTaskTypeWithAnUndefinedTaskTypeId(final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/undefined-task-type-identifier").expect(res -> {

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
   * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldFoundTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    TaskTypesRepository.createProxy(vertx).storeTaskType(new TaskType(), testContext.succeeding(taskType -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + taskType.id).expect(res -> testContext.verify(() -> {

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
   * @see Tasks#createTaskType(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreANonTaskTypeObject(final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#createTaskType(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreTaskTypeWithExistingId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#createTaskType(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldStoreTaskTypeExample(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
      TaskTypesRepository.createProxy(vertx).searchTaskType(stored.id, testContext.succeeding(foundTaskType -> testContext.verify(() -> {

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
   * @see Tasks#createTaskType(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreEmptyTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#createTaskType(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldStoreTaskTypeWithOnlyID(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
      TaskTypesRepository.createProxy(vertx).searchTaskType(stored.id, testContext.succeeding(foundTaskType -> testContext.verify(() -> {

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
   * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskTypeThatIsNotDefined(final WebClient client, final VertxTestContext testContext) {

    final TaskType taskType = new TaskType();
    taskType.name = "Task type name";
    testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/undefined-task-type-identifier").expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
      final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty();
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
      testContext.completeNow();

    }).sendJson(taskType.toJsonObject(), testContext);
  }

  /**
   * Verify that return error when try to update with a model that is not a taskType.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskTypeWithANotTaskTypeObject(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskTypeBecauseNotChangesHasDone(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#updateTaskType(String, io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskTypeBecauseBadSource(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldUpdateExampleTaskTypeWithAnotherExample(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

      final TaskType source = new TaskTypeTest().createModelExample(2);

      source.id = UUID.randomUUID().toString();
      testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

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
   * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDeleteTaskTypeWithAnUndefinedTaskTypeId(final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.DELETE, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/undefined-task-type-identifier").expect(res -> {

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
   * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldDeleteTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final TaskTypesRepository repository = TaskTypesRepository.createProxy(vertx);
    repository.storeTaskType(new TaskType(), testContext.succeeding(storedTaskType -> {

      testRequest(client, HttpMethod.DELETE, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + storedTaskType.id).expect(res -> testContext.verify(() -> {

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
   * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldUpdateOnlyNameOnTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

      final TaskType source = new TaskType();
      source.name = "NEW task type name";
      testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

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
   * @see Tasks#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateBecasueNotChangedOnTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

      final TaskType source = new TaskType();
      testRequest(client, HttpMethod.PUT, Tasks.PATH + "/" + Tasks.TYPES_PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

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
   * @see Tasks#doTaskTransaction(JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDoTaskTransactionWithAnonTaskTransactionObject(final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#doTaskTransaction(JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDoTransactionIfItIsEmpty(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

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
   * @see Tasks#doTaskTransaction(JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDoTransactionIfTaskIsNotDefined(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final TaskTransaction taskTransaction = new TaskTransactionTest().createModelExample(1);
    testRequest(client, HttpMethod.POST, Tasks.PATH + Tasks.TRANSACTIONS_PATH).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      final ErrorMessage error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty().isEqualTo("bad_task_transaction.taskId");
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);
      testContext.completeNow();

    }).sendJson(taskTransaction.toJsonObject(), testContext);

  }

  /**
   * Verify can not get tasks page if try to order with an undefined field.
   *
   * @param order       that is wrong.
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @ParameterizedTest(name = "Should get page order by {0}")
  @ValueSource(strings = { "undefined", "goal.name,", "-goalDescription,+", ",", "" })
  public void shouldNotGetTasksPageWithBadOrder(final String order, final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("order", order)).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      testContext.completeNow();

    }).send(testContext);

  }

  /**
   * Verify get empty tasks page with large offset.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithLargeOffset(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", String.valueOf(Integer.MAX_VALUE))).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(Integer.MAX_VALUE);
      assertThat(page.total).isGreaterThanOrEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify can get tasks page order by the specified field.
   *
   * @param field       that has to used to sort.
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @ParameterizedTest(name = "Should get page order by {0}")
  @ValueSource(strings = { "goalName", "goal.name", "goalDescription", "goal.description", "start", "end", "deadline", "close", "id", "taskTypeId", "requesterId", "appId", "startTs", "endTs", "deadlineTs", "closeTs" })
  public void shouldGetTasksPageOrderByField(final String field, final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("order", field), queryParam("appId", UUID.randomUUID().toString())).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);

  }

  /**
   * Verify get empty tasks page if the appId is not defined.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithUndefinedAppId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String appId = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("appId", appId)).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get a page with the tasks with an specific appId.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByAppId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeApp(new App(), vertx, testContext, testContext.succeeding(app -> {

      StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
        if (index % 2 == 0) {

          task.appId = app.appId;
        }
      }).onComplete(testContext.succeeding(tasks -> {

        testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("appId", app.appId), queryParam("offset", "1"), queryParam("limit", "2")).expect(res -> {
          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final TasksPage page = assertThatBodyIs(TasksPage.class, res);
          assertThat(page).isNotNull();
          assertThat(page.offset).isEqualTo(1);
          assertThat(page.total).isEqualTo(4);
          assertThat(page.tasks).isNotNull().hasSize(2).contains(tasks.get(2), tasks.get(4));
          testContext.completeNow();

        }).send(testContext);

      }));

    }));

  }

  /**
   * Verify get a page with the tasks that the appId match a regular expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexAppId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeSomeTask(8, vertx, testContext, null).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", "1"), queryParam("order", "-goalName,+appId,goalDescription"), queryParam("appId", "/^" + tasks.get(1).appId + "$|^" + tasks.get(7).appId + "$/"))
      .expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(1);
        assertThat(page.total).isEqualTo(2);
        assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(1));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if the requesterId is not defined.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithUndefinedRequesterId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String requesterId = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("requesterId", requesterId)).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get a page with the tasks with an specific requesterId.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRequesterId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeProfile(new WeNetUserProfile(), vertx, testContext, testContext.succeeding(requester -> {

      StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
        if (index % 2 == 0) {

          task.requesterId = requester.id;
        }
      }).onComplete(testContext.succeeding(tasks -> {

        testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", "3"), queryParam("limit", "1"), queryParam("requesterId", requester.id)).expect(res -> {
          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final TasksPage page = assertThatBodyIs(TasksPage.class, res);
          assertThat(page).isNotNull();
          assertThat(page.offset).isEqualTo(3);
          assertThat(page.total).isEqualTo(4);
          assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(6));
          testContext.completeNow();

        }).send(testContext);

      }));

    }));

  }

  /**
   * Verify get a page with the tasks that the requesterId match a regular expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexRequesterId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeSomeTask(8, vertx, testContext, null).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
      testRequest(client, HttpMethod.GET, Tasks.PATH)
      .with(queryParam("offset", "1"), queryParam("order", "-goalName,+requesterId,goalDescription"), queryParam("requesterId", "/^" + tasks.get(1).requesterId + "$|^" + tasks.get(7).requesterId + "$/")).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(1);
        assertThat(page.total).isEqualTo(2);
        assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(1));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if the taskTypeId is not defined.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithUndefinedTaskTypeId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String taskTypeId = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("taskTypeId", taskTypeId)).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get a page with the tasks with an specific taskTypeId.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByTaskTypeId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(100, vertx, testContext, testContext.succeeding(taskType -> {

      StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
        if (index % 2 == 0) {

          task.taskTypeId = taskType.id;
        }
      }).onComplete(testContext.succeeding(tasks -> {

        testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("taskTypeId", taskType.id)).expect(res -> {
          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final TasksPage page = assertThatBodyIs(TasksPage.class, res);
          assertThat(page).isNotNull();
          assertThat(page.offset).isEqualTo(0);
          assertThat(page.total).isEqualTo(4);
          assertThat(page.tasks).isNotNull().hasSize(4).contains(tasks.get(0), tasks.get(2), tasks.get(4), tasks.get(6));
          testContext.completeNow();

        }).send(testContext);

      }));

    }));

  }

  /**
   * Verify get a page with the tasks that the taskTypeId match a regular expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexTaskTypeId(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeSomeTask(8, vertx, testContext, null).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", "1"), queryParam("order", "-goal.name,+taskTypeId"), queryParam("taskTypeId", "/^" + tasks.get(1).taskTypeId + "$|^" + tasks.get(7).taskTypeId + "$/"))
      .expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(1);
        assertThat(page.total).isEqualTo(2);
        assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(1));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if the goalName is not defined.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithUndefinedGoalName(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String goalName = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", goalName)).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get a page with the tasks with an specific goalName.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByGoalName(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String goalName = UUID.randomUUID().toString();

    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      if (index % 2 == 0) {

        task.goal.name = goalName;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", goalName)).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().hasSize(4).contains(tasks.get(0), tasks.get(2), tasks.get(4), tasks.get(6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get a page with the tasks that the goalName match a regular expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexGoalName(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String goalName = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> task.goal.name += goalName).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", "1"), queryParam("limit", "1"), queryParam("order", "-goal.name"), queryParam("goalName", "/" + goalName.replaceAll("-", "\\-") + "$/")).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(1);
        assertThat(page.total).isEqualTo(8);
        assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if the goalDescription is not defined.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithUndefinedGoalDescription(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String goalDescription = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalDescription", goalDescription)).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get a page with the tasks with an specific goalDescription.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByGoalDescription(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String goalDescription = UUID.randomUUID().toString();

    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      if (index % 2 == 0) {

        task.goal.description = goalDescription;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalDescription", goalDescription)).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().hasSize(4).contains(tasks.get(0), tasks.get(2), tasks.get(4), tasks.get(6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get a page with the tasks that the goalDescription match a regular expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexGoalDescription(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String goalDescription = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> task.goal.description += goalDescription).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.description.compareTo(t2.goal.description));
      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", "2"), queryParam("limit", "2"), queryParam("order", "-goal.description"), queryParam("goalDescription", "/" + goalDescription.replaceAll("-", "\\-") + "$/"))
      .expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(2);
        assertThat(page.total).isEqualTo(8);
        assertThat(page.tasks).isNotNull().hasSize(2).contains(tasks.get(5), tasks.get(4));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if any task are in the range on the startTs.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithAnytAskOnStartTsRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("startFrom", "0"), queryParam("startTo", "1")).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get page of task that has a startTs on the specified range.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWithStartTsOnSpecificRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> task.goal.name = name).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", name), queryParam("startFrom", String.valueOf(tasks.get(2).startTs)), queryParam("startTo", String.valueOf(tasks.get(5).startTs))).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().isEqualTo(tasks.subList(2, 6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if any task are in the range on the endTs.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithAnytAskOnEndTsRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("endFrom", "0"), queryParam("endTo", "1")).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get page of task that has a endTs on the specified range.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWithEndTsOnSpecificRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> task.goal.name = name).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", name), queryParam("endFrom", String.valueOf(tasks.get(2).endTs)), queryParam("endTo", String.valueOf(tasks.get(5).endTs))).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().isEqualTo(tasks.subList(2, 6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if any task are in the range on the deadlineTs.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithAnytAskOnDeadlineTsRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("deadlineFrom", "0"), queryParam("deadlineTo", "1")).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get page of task that has a deadlineTs on the specified range.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWithDeadlineTsOnSpecificRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> task.goal.name = name).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", name), queryParam("deadlineFrom", String.valueOf(tasks.get(2).deadlineTs)), queryParam("deadlineTo", String.valueOf(tasks.get(5).deadlineTs))).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().isEqualTo(tasks.subList(2, 6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get empty tasks page if any task are in the range on the closeTs.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetEmptyTasksPageWithAnytAskOnCloseTsRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("closeFrom", "0"), queryParam("closeTo", "1")).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final TasksPage page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();
      testContext.completeNow();

    }).send(testContext);
  }

  /**
   * Verify get page of task that has a closeTs on the specified range.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWithCloseTsOnSpecificRange(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      task.goal.name = name;
      task.closeTs = task.endTs + 10000;
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", name), queryParam("closeFrom", String.valueOf(tasks.get(2).closeTs)), queryParam("closeTo", String.valueOf(tasks.get(5).closeTs))).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().isEqualTo(tasks.subList(2, 6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get page of task that has been closed.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWithClosedTasks(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      task.goal.name = name;
      if (index % 2 == 0) {

        task.closeTs = task.endTs + 10000;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", name), queryParam("hasCloseTs", "true")).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().contains(tasks.get(0),tasks.get(2),tasks.get(4),tasks.get(6));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

  /**
   * Verify get page of task that has not been closed.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWithNotClosedTasks(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final String name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      task.goal.name = name;
      if (index % 2 == 0) {

        task.closeTs = task.endTs + 10000;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", name), queryParam("hasCloseTs", "false")).expect(res -> {
        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final TasksPage page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().contains(tasks.get(1),tasks.get(3),tasks.get(5),tasks.get(7));
        testContext.completeNow();

      }).send(testContext);

    }));

  }

}
