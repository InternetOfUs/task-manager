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
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
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
import static eu.internetofus.common.vertx.ext.TestRequest.queryParam;
import static eu.internetofus.common.vertx.ext.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;

import eu.internetofus.common.components.ErrorMessage;
import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.profile_manager.WeNetUserProfile;
import eu.internetofus.common.components.service.App;
import eu.internetofus.common.components.service.WeNetServiceSimulator;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskTest;
import eu.internetofus.common.components.task_manager.TaskTransaction;
import eu.internetofus.common.components.task_manager.TaskTransactionTest;
import eu.internetofus.common.vertx.AbstractModelResourcesIT;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The integration test over the {@link Tasks}.
 *
 * @see Tasks
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TasksIT extends AbstractModelResourcesIT<Task, String> {

  /**
   * {@inheritDoc}
   */
  @Override
  protected String modelPath() {

    return Tasks.PATH;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Task createInvalidModel() {

    return new TaskTest().createModelExample(1);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Future<Task> createValidModelExample(final int index, final Vertx vertx,
      final VertxTestContext testContext) {

    return testContext.assertComplete(new TaskTest().createModelExample(index, vertx, testContext));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Future<Task> storeModel(final Task source, final Vertx vertx, final VertxTestContext testContext) {

    return StoreServices.storeTask(source, vertx, testContext);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void assertThatCreatedEquals(final Task source, final Task target) {

    source.id = target.id;
    source._creationTs = target._creationTs;
    source._lastUpdateTs = target._lastUpdateTs;
    if (source.norms != null && target.norms != null && source.norms.size() == target.norms.size()) {

      final var max = source.norms.size();
      for (var i = 0; i < max; i++) {

        source.norms.get(i).id = target.norms.get(i).id;
      }

    }
    assertThat(source).isEqualTo(target);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String idOf(final Task model) {

    return model.id;

  }

  /**
   * Verify that store an empty task.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see Tasks#createTask(io.vertx.core.json.JsonObject,
   *      io.vertx.ext.web.api.service.ServiceRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreEmptyTask(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final var task = new Task();
    testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      final var error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty();
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

    }).sendJson(task.toJsonObject(), testContext);

  }

  /**
   * Verify that only update the middle name of an user.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see Tasks#retrieveTask(String, io.vertx.ext.web.api.service.ServiceRequest,
   *      io.vertx.core.Handler)
   */
  @Test
  public void shouldMergeOnlyAppIdOnTask(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskExample(2, vertx, testContext).onSuccess(target -> {

      StoreServices.storeAppExample(1, vertx, testContext).onSuccess(app -> {

        final var source = new Task();
        source.appId = app.appId;
        testRequest(client, HttpMethod.PATCH, Tasks.PATH + "/" + target.id).expect(res -> testContext.verify(() -> {

          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final var updated = assertThatBodyIs(Task.class, res);
          assertThat(updated).isNotEqualTo(target).isNotEqualTo(source);
          target._lastUpdateTs = updated._lastUpdateTs;
          target.appId = app.appId;
          assertThat(updated).isEqualTo(target);

        })).sendJson(source.toJsonObject(), testContext);
      });
    });

  }

  /**
   * Verify that can not do a transaction with a bad task transaction JSON object.
   *
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see Tasks#doTaskTransaction(JsonObject,
   *      io.vertx.ext.web.api.service.ServiceRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDoTaskTransactionWithAnonTaskTransactionObject(final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.POST, Tasks.PATH + Tasks.TRANSACTIONS_PATH).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      final var error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty().isEqualTo("bad_task_transaction");
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

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
   *      io.vertx.ext.web.api.service.ServiceRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDoTransactionIfItIsEmpty(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var taskTransaction = new TaskTransaction();
    testRequest(client, HttpMethod.POST, Tasks.PATH + Tasks.TRANSACTIONS_PATH).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      final var error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty().isEqualTo("bad_task_transaction.taskId");
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

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
   *      io.vertx.ext.web.api.service.ServiceRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDoTransactionIfTaskIsNotDefined(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var taskTransaction = new TaskTransactionTest().createModelExample(1);
    testRequest(client, HttpMethod.POST, Tasks.PATH + Tasks.TRANSACTIONS_PATH).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      final var error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty().isEqualTo("bad_task_transaction.taskId");
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

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
  @ValueSource(strings = { "undefined", "goal.name,undefined", "-goalDescription,+", ",-" })
  public void shouldNotGetTasksPageWithBadOrder(final String order, final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("order", order)).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

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
  public void shouldGetEmptyTasksPageWithLargeOffset(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", String.valueOf(Integer.MAX_VALUE)))
        .expect(res -> {

          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final var page = assertThatBodyIs(TasksPage.class, res);
          assertThat(page).isNotNull();
          assertThat(page.offset).isEqualTo(Integer.MAX_VALUE);
          assertThat(page.total).isGreaterThanOrEqualTo(0);
          assertThat(page.tasks).isNull();

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
  @ValueSource(strings = { "goalName", "goal.name", "goalDescription", "goal.description", "updateTs", "update",
      "_updateTs", "lastUpdateTs", "_lastUpdateTs", "creationTs", "creation", "_creationTs", "id", "taskTypeId",
      "requesterId", "appId" })
  public void shouldGetTasksPageOrderByField(final String field, final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH)
        .with(queryParam("order", field), queryParam("appId", UUID.randomUUID().toString())).expect(res -> {

          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final var page = assertThatBodyIs(TasksPage.class, res);
          assertThat(page).isNotNull();
          assertThat(page.total).isEqualTo(0);
          assertThat(page.tasks).isNull();

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
  public void shouldGetEmptyTasksPageWithUndefinedAppId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var appId = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("appId", appId)).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final var page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();

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

    StoreServices.storeApp(new App(), vertx, testContext).onSuccess(app -> {

      StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
        if (index % 2 == 0) {

          task.appId = app.appId;
        }
      }).onComplete(testContext.succeeding(tasks -> {

        testRequest(client, HttpMethod.GET, Tasks.PATH)
            .with(queryParam("appId", app.appId), queryParam("offset", "1"), queryParam("limit", "2")).expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TasksPage.class, res);
              assertThat(page).isNotNull();
              assertThat(page.offset).isEqualTo(1);
              assertThat(page.total).isEqualTo(4);
              assertThat(page.tasks).isNotNull().hasSize(2).contains(tasks.get(2), tasks.get(4));

            }).send(testContext);

      }));

    });

  }

  /**
   * Verify get a page with the tasks that the appId match a regular expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexAppId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeSomeTask(8, vertx, testContext, null).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
      testRequest(client, HttpMethod.GET, Tasks.PATH)
          .with(queryParam("offset", "1"), queryParam("order", "-goalName,+appId,goalDescription"),
              queryParam("appId", "/^" + tasks.get(1).appId + "$|^" + tasks.get(7).appId + "$/"))
          .expect(res -> {

            assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
            final var page = assertThatBodyIs(TasksPage.class, res);
            assertThat(page).isNotNull();
            assertThat(page.offset).isEqualTo(1);
            assertThat(page.total).isEqualTo(2);
            assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(1));

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
  public void shouldGetEmptyTasksPageWithUndefinedRequesterId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var requesterId = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("requesterId", requesterId)).expect(res -> {
      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());

      final var page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();

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
  public void shouldGetSomeTasksByRequesterId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeProfile(new WeNetUserProfile(), vertx, testContext).onSuccess(requester -> {

      StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
        if (index % 2 == 0) {

          task.requesterId = requester.id;
        }
      }).onComplete(testContext.succeeding(tasks -> {

        testRequest(client, HttpMethod.GET, Tasks.PATH)
            .with(queryParam("offset", "3"), queryParam("limit", "1"), queryParam("requesterId", requester.id))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TasksPage.class, res);
              assertThat(page).isNotNull();
              assertThat(page.offset).isEqualTo(3);
              assertThat(page.total).isEqualTo(4);
              assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(6));

            }).send(testContext);

      }));

    });

  }

  /**
   * Verify get a page with the tasks that the requesterId match a regular
   * expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexRequesterId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeSomeTask(8, vertx, testContext, null).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
      testRequest(client, HttpMethod.GET, Tasks.PATH)
          .with(queryParam("offset", "1"), queryParam("order", "-goalName,+requesterId,goalDescription"),
              queryParam("requesterId", "/^" + tasks.get(1).requesterId + "$|^" + tasks.get(7).requesterId + "$/"))
          .expect(res -> {

            assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
            final var page = assertThatBodyIs(TasksPage.class, res);
            assertThat(page).isNotNull();
            assertThat(page.offset).isEqualTo(1);
            assertThat(page.total).isEqualTo(2);
            assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(1));

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
  public void shouldGetEmptyTasksPageWithUndefinedTaskTypeId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var taskTypeId = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("taskTypeId", taskTypeId)).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final var page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();

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
  public void shouldGetSomeTasksByTaskTypeId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(100, vertx, testContext).onSuccess(taskType -> {

      StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
        if (index % 2 == 0) {

          task.taskTypeId = taskType.id;
        }
      }).onComplete(testContext.succeeding(tasks -> {

        testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("taskTypeId", taskType.id)).expect(res -> {

          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final var page = assertThatBodyIs(TasksPage.class, res);
          assertThat(page).isNotNull();
          assertThat(page.offset).isEqualTo(0);
          assertThat(page.total).isEqualTo(4);
          assertThat(page.tasks).isNotNull().hasSize(4).contains(tasks.get(0), tasks.get(2), tasks.get(4),
              tasks.get(6));

        }).send(testContext);

      }));

    });

  }

  /**
   * Verify get a page with the tasks that the taskTypeId match a regular
   * expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexTaskTypeId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeSomeTask(8, vertx, testContext, null).onComplete(testContext.succeeding(tasks -> {

      tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
      testRequest(client, HttpMethod.GET, Tasks.PATH)
          .with(queryParam("offset", "1"), queryParam("order", "-goal.name,+taskTypeId"),
              queryParam("taskTypeId", "/^" + tasks.get(1).taskTypeId + "$|^" + tasks.get(7).taskTypeId + "$/"))
          .expect(res -> {

            assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
            final var page = assertThatBodyIs(TasksPage.class, res);
            assertThat(page).isNotNull();
            assertThat(page.offset).isEqualTo(1);
            assertThat(page.total).isEqualTo(2);
            assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(1));

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
  public void shouldGetEmptyTasksPageWithUndefinedGoalName(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var goalName = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", goalName)).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final var page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();

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
  public void shouldGetSomeTasksByGoalName(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var goalName = UUID.randomUUID().toString();

    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      if (index % 2 == 0) {

        task.goal.name = goalName;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalName", goalName)).expect(res -> {

        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final var page = assertThatBodyIs(TasksPage.class, res);
        assertThat(page).isNotNull();
        assertThat(page.offset).isEqualTo(0);
        assertThat(page.total).isEqualTo(4);
        assertThat(page.tasks).isNotNull().hasSize(4).contains(tasks.get(0), tasks.get(2), tasks.get(4), tasks.get(6));

      }).send(testContext);

    }));

  }

  /**
   * Verify get a page with the tasks that the goalName match a regular
   * expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexGoalName(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var goalName = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> task.goal.name += goalName)
        .onComplete(testContext.succeeding(tasks -> {

          tasks.sort((t1, t2) -> t1.goal.name.compareTo(t2.goal.name));
          testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("offset", "1"), queryParam("limit", "1"),
              queryParam("order", "-goal.name"), queryParam("goalName", "/" + goalName.replaceAll("-", "\\-") + "$/"))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(TasksPage.class, res);
                assertThat(page).isNotNull();
                assertThat(page.offset).isEqualTo(1);
                assertThat(page.total).isEqualTo(8);
                assertThat(page.tasks).isNotNull().hasSize(1).contains(tasks.get(6));

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
  public void shouldGetEmptyTasksPageWithUndefinedGoalDescription(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var goalDescription = UUID.randomUUID().toString();
    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalDescription", goalDescription)).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final var page = assertThatBodyIs(TasksPage.class, res);
      assertThat(page).isNotNull();
      assertThat(page.offset).isEqualTo(0);
      assertThat(page.total).isEqualTo(0);
      assertThat(page.tasks).isNull();

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
  public void shouldGetSomeTasksByGoalDescription(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var goalDescription = UUID.randomUUID().toString();

    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      if (index % 2 == 0) {

        task.goal.description = goalDescription;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("goalDescription", goalDescription))
          .expect(res -> {

            assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
            final var page = assertThatBodyIs(TasksPage.class, res);
            assertThat(page).isNotNull();
            assertThat(page.offset).isEqualTo(0);
            assertThat(page.total).isEqualTo(4);
            assertThat(page.tasks).isNotNull().hasSize(4).contains(tasks.get(0), tasks.get(2), tasks.get(4),
                tasks.get(6));

          }).send(testContext);

    }));

  }

  /**
   * Verify get a page with the tasks that the goalDescription match a regular
   * expression.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetSomeTasksByRegexGoalDescription(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var goalDescription = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> task.goal.description += goalDescription)
        .onComplete(testContext.succeeding(tasks -> {

          tasks.sort((t1, t2) -> t1.goal.description.compareTo(t2.goal.description));
          testRequest(client, HttpMethod.GET, Tasks.PATH)
              .with(queryParam("offset", "2"), queryParam("limit", "2"), queryParam("order", "-goal.description"),
                  queryParam("goalDescription", "/" + goalDescription.replaceAll("-", "\\-") + "$/"))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(TasksPage.class, res);
                assertThat(page).isNotNull();
                assertThat(page.offset).isEqualTo(2);
                assertThat(page.total).isEqualTo(8);
                assertThat(page.tasks).isNotNull().hasSize(2).contains(tasks.get(5), tasks.get(4));

              }).send(testContext);

        }));

  }

  /**
   * Verify get page of task that has a _creationTs on the specified range.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWith_creationTsOnSpecificRange(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, 1000, vertx, testContext, (index, task) -> task.goal.name = name)
        .onComplete(testContext.succeeding(tasks -> {

          testRequest(client, HttpMethod.GET, Tasks.PATH)
              .with(queryParam("goalName", name), queryParam("creationFrom", String.valueOf(tasks.get(2)._creationTs)),
                  queryParam("creationTo", String.valueOf(tasks.get(5)._creationTs)))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(TasksPage.class, res);
                assertThat(page).isNotNull();
                assertThat(page.offset).isEqualTo(0);
                assertThat(page.total).isEqualTo(4);
                assertThat(page.tasks).isNotNull().isEqualTo(tasks.subList(2, 6));

              }).send(testContext);

        }));

  }

  /**
   * Verify get page of task that has a _lastUpdateTs on the specified range.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldGetTasksPageWith_lastUpdateTsOnSpecificRange(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, 1000, vertx, testContext, (index, task) -> task.goal.name = name)
        .onComplete(testContext.succeeding(tasks -> {

          testRequest(client, HttpMethod.GET, Tasks.PATH)
              .with(queryParam("goalName", name), queryParam("updateFrom", String.valueOf(tasks.get(2)._lastUpdateTs)),
                  queryParam("updateTo", String.valueOf(tasks.get(5)._lastUpdateTs)))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(TasksPage.class, res);
                assertThat(page).isNotNull();
                assertThat(page.offset).isEqualTo(0);
                assertThat(page.total).isEqualTo(4);
                assertThat(page.tasks).isNotNull().isEqualTo(tasks.subList(2, 6));

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
  public void shouldGetEmptyTasksPageWithAnytAskOnCloseTsRange(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Tasks.PATH).with(queryParam("closeFrom", "0"), queryParam("closeTo", "1"))
        .expect(res -> {

          assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
          final var page = assertThatBodyIs(TasksPage.class, res);
          assertThat(page).isNotNull();
          assertThat(page.offset).isEqualTo(0);
          assertThat(page.total).isEqualTo(0);
          assertThat(page.tasks).isNull();

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
  public void shouldGetTasksPageWithCloseTsOnSpecificRange(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      task.goal.name = name;
      task.closeTs = task._creationTs + 10000;
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH)
          .with(queryParam("goalName", name), queryParam("closeFrom", String.valueOf(tasks.get(2).closeTs)),
              queryParam("closeTo", String.valueOf(tasks.get(5).closeTs)))
          .expect(res -> {

            assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
            final var page = assertThatBodyIs(TasksPage.class, res);
            assertThat(page).isNotNull();
            assertThat(page.offset).isEqualTo(0);
            assertThat(page.total).isEqualTo(4);
            assertThat(page.tasks).isNotNull().isEqualTo(tasks.subList(2, 6));

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
  public void shouldGetTasksPageWithClosedTasks(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      task.goal.name = name;
      if (index % 2 == 0) {

        task.closeTs = task._creationTs + 10000;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH)
          .with(queryParam("goalName", name), queryParam("hasCloseTs", "true")).expect(res -> {

            assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
            final var page = assertThatBodyIs(TasksPage.class, res);
            assertThat(page).isNotNull();
            assertThat(page.offset).isEqualTo(0);
            assertThat(page.total).isEqualTo(4);
            assertThat(page.tasks).isNotNull().contains(tasks.get(0), tasks.get(2), tasks.get(4), tasks.get(6));

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
  public void shouldGetTasksPageWithNotClosedTasks(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    StoreServices.storeSomeTask(8, vertx, testContext, (index, task) -> {
      task.goal.name = name;
      if (index % 2 == 0) {

        task.closeTs = task._creationTs + 10000;
      }
    }).onComplete(testContext.succeeding(tasks -> {

      testRequest(client, HttpMethod.GET, Tasks.PATH)
          .with(queryParam("goalName", name), queryParam("hasCloseTs", "false")).expect(res -> {

            assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
            final var page = assertThatBodyIs(TasksPage.class, res);
            assertThat(page).isNotNull();
            assertThat(page.offset).isEqualTo(0);
            assertThat(page.total).isEqualTo(4);
            assertThat(page.tasks).isNotNull().contains(tasks.get(1), tasks.get(3), tasks.get(5), tasks.get(7));

          }).send(testContext);

    }));

  }

  /**
   * Should create task with the same community defined for the application.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldCreateTaskWithTheCameCommunityOfTheApp(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskExample(1, vertx, testContext).onSuccess(task -> {

      final var communityId = task.communityId;
      task.id = null;
      task.communityId = null;
      testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

        assertThat(res.statusCode()).isEqualTo(Status.CREATED.getStatusCode());
        final var task2 = assertThatBodyIs(Task.class, res);
        assertThat(task2.communityId).isEqualTo(communityId);

      }).sendJson(task.toJsonObject(), testContext);

    });
  }

  /**
   * Should create task creating a community for the APP.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldNotCreateTaskBecauseNoUserOnTheApp(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeApp(new App(), vertx, testContext).onSuccess(app -> {

      StoreServices.storeTaskExample(1, vertx, testContext).onSuccess(task -> {

        task.id = null;
        task.appId = app.appId;
        task.communityId = null;
        testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

          assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        }).sendJson(task.toJsonObject(), testContext);

      });
    });
  }

  /**
   * Should create task creating a community for the APP.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldCreateTaskCreatingCommunityForApp(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeApp(new App(), vertx, testContext).onSuccess(app -> {

      StoreServices.storeTaskExample(1, vertx, testContext).onSuccess(task -> {

        WeNetServiceSimulator.createProxy(vertx).addUsers(app.appId, new JsonArray().add(task.requesterId),
            testContext.succeeding(users -> {

              final var communityId = task.communityId;
              task.id = null;
              task.appId = app.appId;
              task.communityId = null;
              testRequest(client, HttpMethod.POST, Tasks.PATH).expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.CREATED.getStatusCode());
                final var task2 = assertThatBodyIs(Task.class, res);
                assertThat(task2.communityId).isNotEqualTo(communityId);

              }).sendJson(task.toJsonObject(), testContext);

            }));
      });

    });
  }

}
