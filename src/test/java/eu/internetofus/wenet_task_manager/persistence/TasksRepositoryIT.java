/*
 * -----------------------------------------------------------------------------
 *
 * Copyright 2019 - 2022 UDT-IA, IIIA-CSIC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -----------------------------------------------------------------------------
 */

package eu.internetofus.wenet_task_manager.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.models.HumanDescriptionTest;
import eu.internetofus.common.components.models.Message;
import eu.internetofus.common.components.models.MessageTest;
import eu.internetofus.common.components.models.Task;
import eu.internetofus.common.components.models.TaskTest;
import eu.internetofus.common.components.models.TaskTransaction;
import eu.internetofus.common.components.models.TaskTransactionTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration test over the {@link TasksRepository}.
 *
 * @see TasksRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TasksRepositoryIT {

  /**
   * Verify that can not found a task if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundUndefinedTask(final Vertx vertx, final VertxTestContext testContext) {

    TasksRepository.createProxy(vertx).searchTask("undefined user identifier", testContext.failing(failed -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can not found a task object if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundUndefinedTaskObject(final Vertx vertx, final VertxTestContext testContext) {

    TasksRepository.createProxy(vertx).searchTask("undefined user identifier", testContext.failing(failed -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can found a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldFoundTask(final Vertx vertx, final VertxTestContext testContext) {

    testContext
        .assertComplete(TasksRepository.createProxy(vertx).storeTask(new Task()).compose(storedTask -> TasksRepository
            .createProxy(vertx).searchTask(storedTask.id).onSuccess(foundTask -> testContext.verify(() -> {
              assertThat(foundTask).isEqualTo(storedTask);
              testContext.completeNow();
            }))));

  }

  /**
   * Verify that can found a task object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldFoundTaskObject(final Vertx vertx, final VertxTestContext testContext) {

    TasksRepository.createProxy(vertx).storeTask(new JsonObject(), testContext.succeeding(storedTask -> {

      TasksRepository.createProxy(vertx).searchTask(storedTask.getString("id"),
          testContext.succeeding(foundTask -> testContext.verify(() -> {
            assertThat(foundTask).isEqualTo(storedTask);
            testContext.completeNow();
          })));

    }));

  }

  /**
   * Verify that can not store a task that can not be an object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task)
   */
  @Test
  public void shouldNotStoreATaskThatCanNotBeAnObject(final Vertx vertx, final VertxTestContext testContext) {

    final Task task = new Task() {

      /**
       * {@inheritDoc}
       */
      @Override
      public JsonObject toJsonObject() {

        return null;
      }
    };
    task.id = "undefined user identifier";
    testContext.assertFailure(TasksRepository.createProxy(vertx).storeTask(task))
        .onFailure(failed -> testContext.completeNow());

  }

  /**
   * Verify that can store a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task)
   */
  @Test
  public void shouldStoreTask(final Vertx vertx, final VertxTestContext testContext) {

    final var task = new Task();
    task._creationTs = 0;
    task._lastUpdateTs = 1;

    testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(task))
        .onSuccess(storedTask -> testContext.verify(() -> {

          assertThat(storedTask).isNotNull();
          assertThat(storedTask.id).isNotEmpty();
          assertThat(storedTask._creationTs).isEqualTo(0);
          assertThat(storedTask._lastUpdateTs).isEqualTo(1);
          testContext.completeNow();
        }));

  }

  /**
   * Verify that can store a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task)
   */
  @Test
  public void shouldStoreTaskWithAnId(final Vertx vertx, final VertxTestContext testContext) {

    final var id = UUID.randomUUID().toString();
    final var task = new Task();
    task.id = id;
    task._creationTs = 2;
    task._lastUpdateTs = 3;
    testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(task))
        .onSuccess(storedTask -> testContext.verify(() -> {

          assertThat(storedTask.id).isEqualTo(id);
          assertThat(storedTask._creationTs).isEqualTo(2);
          assertThat(storedTask._lastUpdateTs).isEqualTo(3);
          testContext.completeNow();
        }));

  }

  /**
   * Verify that can store a task with an id of an stored task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task)
   */
  @Test
  public void shouldNotStoreTwoTaskWithTheSameId(final Vertx vertx, final VertxTestContext testContext) {

    final var id = UUID.randomUUID().toString();
    final var task = new Task();
    task.id = id;
    testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(task))
        .onSuccess(storedTask -> testContext.verify(() -> {

          testContext.assertFailure(TasksRepository.createProxy(vertx).storeTask(task))
              .onFailure(error -> testContext.completeNow());

        }));

  }

  /**
   * Verify that can not update a task if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(Task)
   */
  @Test
  public void shouldNotUpdateUndefinedTask(final Vertx vertx, final VertxTestContext testContext) {

    final var task = new Task();
    task.id = "undefined user identifier";
    testContext.assertFailure(TasksRepository.createProxy(vertx).updateTask(task))
        .onFailure(error -> testContext.completeNow());

  }

  /**
   * Verify that can not update a task if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(JsonObject, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateUndefinedTaskObject(final Vertx vertx, final VertxTestContext testContext) {

    final var task = new JsonObject().put("id", "undefined user identifier");
    TasksRepository.createProxy(vertx).updateTask(task, testContext.failing(failed -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can not update a task if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(Task)
   */
  @Test
  public void shouldNotUpdateATaskThatCanNotBeAnObject(final Vertx vertx, final VertxTestContext testContext) {

    final Task task = new Task() {

      /**
       * {@inheritDoc}
       */
      @Override
      public JsonObject toJsonObject() {

        return null;
      }
    };
    task.id = "undefined user identifier";
    testContext.assertFailure(TasksRepository.createProxy(vertx).updateTask(task))
        .onFailure(error -> testContext.completeNow());

  }

  /**
   * Verify that can update a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(Task)
   */
  @Test
  public void shouldUpdateTask(final Vertx vertx, final VertxTestContext testContext) {

    final var task = new Task();
    task.goal = new HumanDescriptionTest().createModelExample(23);
    testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(task)).onSuccess(stored -> {

      final var update = new TaskTest().createModelExample(23);
      update.id = stored.id;
      update._creationTs = 0;
      update._lastUpdateTs = 1;
      testContext
          .assertComplete(TasksRepository.createProxy(vertx).updateTask(update)
              .compose(empty -> TasksRepository.createProxy(vertx).searchTask(stored.id)))
          .onSuccess(foundTask -> testContext.verify(() -> {

            assertThat(stored).isNotNull();
            assertThat(foundTask.id).isNotEmpty().isEqualTo(stored.id);
            assertThat(foundTask._creationTs).isEqualTo(stored._creationTs);
            assertThat(foundTask._lastUpdateTs).isEqualTo(1);
            update._creationTs = stored._creationTs;
            update._lastUpdateTs = foundTask._lastUpdateTs;
            assertThat(foundTask).isEqualTo(update);
            testContext.completeNow();
          }));

    });

  }

  /**
   * Verify that update a defined task object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(JsonObject, io.vertx.core.Handler)
   */
  @Test
  public void shouldUpdateTaskObject(final Vertx vertx, final VertxTestContext testContext) {

    TasksRepository.createProxy(vertx).storeTask(
        new JsonObject().put("goal", new JsonObject().put("name", "Goal name")),
        testContext.succeeding(stored -> testContext.verify(() -> {

          final var id = stored.getString("id");
          final var update = new JsonObject().put("id", id).put("attributes",
              new JsonObject().put("attributeKey", "Attribute value"));
          TasksRepository.createProxy(vertx).updateTask(update,
              testContext.succeeding(empty -> testContext.verify(() -> {

                TasksRepository.createProxy(vertx).searchTask(id,
                    testContext.succeeding(foundTask -> testContext.verify(() -> {
                      stored.put("attributes", new JsonObject().put("attributeKey", "Attribute value"));
                      assertThat(foundTask).isEqualTo(stored);
                      testContext.completeNow();
                    })));
              })));

        })));

  }

  /**
   * Verify that can not delete a task if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDeleteUndefinedTask(final Vertx vertx, final VertxTestContext testContext) {

    testContext.assertFailure(TasksRepository.createProxy(vertx).deleteTask("undefined"))
        .onFailure(error -> testContext.completeNow());
  }

  /**
   * Verify that can delete a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(JsonObject, io.vertx.core.Handler)
   */
  @Test
  public void shouldDeleteTask(final Vertx vertx, final VertxTestContext testContext) {

    testContext.assertComplete(StoreServices.storeTaskExample(1, vertx, testContext)).onSuccess(stored -> {

      testContext.assertComplete(TasksRepository.createProxy(vertx).deleteTask(stored.id)).onSuccess(deleted -> {

        testContext.assertFailure(TasksRepository.createProxy(vertx).searchTask(stored.id))
            .onFailure(error -> testContext.completeNow());

      });

    });

  }

  /**
   * Check that retrieve the expected tasks by goal name.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int)
   */
  @Test
  public void shouldRetrieveTaksByGoalName(final Vertx vertx, final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    final var query = TasksRepository.createTasksPageQuery(null, null, null, name, null, null, null, null, null, null,
        null, null);

    testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10))
        .onSuccess(foundPage -> testContext.verify(() -> {

          assertThat(foundPage).isNotNull();
          assertThat(foundPage.total).isEqualTo(0);
          assertThat(foundPage.offset).isEqualTo(0);
          assertThat(foundPage.tasks).isNull();
          final List<Task> tasks = new ArrayList<>();
          testContext
              .assertComplete(this.storeSomeTasks(vertx, testContext, task -> task.goal.name = name, 10, tasks)
                  .compose(stored -> TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10)))
              .onSuccess(foundPage2 -> testContext.verify(() -> {

                assertThat(foundPage2).isNotNull();
                assertThat(foundPage2.total).isEqualTo(10);
                assertThat(foundPage2.offset).isEqualTo(0);
                assertThat(foundPage2.tasks).isEqualTo(tasks);
                testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5))
                    .onSuccess(foundPage3 -> testContext.verify(() -> {

                      assertThat(foundPage3).isNotNull();
                      assertThat(foundPage3.total).isEqualTo(10);
                      assertThat(foundPage3.offset).isEqualTo(2);
                      assertThat(foundPage3.tasks).isEqualTo(tasks.subList(2, 7));
                      testContext.completeNow();

                    }));

              }));
        }));

  }

  /**
   * Create some {@link TaskTest#createModelExample(int)}.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   * @param change      function to modify the pattern before to store it.
   * @param max         number maximum of tasks to create.
   * @param tasks       list to add the created tasks.
   *
   * @return the future creation result.
   */
  public Future<Void> storeSomeTasks(final Vertx vertx, final VertxTestContext testContext, final Consumer<Task> change,
      final int max, final List<Task> tasks) {

    if (tasks.size() == max) {

      return Future.succeededFuture();

    } else {

      final var task = new TaskTest().createModelExample(tasks.size());
      change.accept(task);
      return testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(task)).compose(stored -> {
        tasks.add(stored);
        return this.storeSomeTasks(vertx, testContext, change, max, tasks);
      });
    }

  }

  /**
   * Check that retrieve the expected tasks by goal description.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int)
   */
  @Test
  public void shouldRetrieveTaksByGoalDescription(final Vertx vertx, final VertxTestContext testContext) {

    final var description = UUID.randomUUID().toString();
    final var query = TasksRepository.createTasksPageQuery(null, null, null, null, description, null, null, null, null,
        null, null, null);

    testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10))
        .onSuccess(foundPage -> testContext.verify(() -> {

          assertThat(foundPage).isNotNull();
          assertThat(foundPage.total).isEqualTo(0);
          assertThat(foundPage.offset).isEqualTo(0);
          assertThat(foundPage.tasks).isNull();
          final List<Task> tasks = new ArrayList<>();
          testContext
              .assertComplete(
                  this.storeSomeTasks(vertx, testContext, task -> task.goal.description = description, 10, tasks)
                      .compose(stored -> TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10)))
              .onSuccess(foundPage2 -> testContext.verify(() -> {

                assertThat(foundPage2).isNotNull();
                assertThat(foundPage2.total).isEqualTo(10);
                assertThat(foundPage2.offset).isEqualTo(0);
                assertThat(foundPage2.tasks).isEqualTo(tasks);
                testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5))
                    .onSuccess(foundPage3 -> testContext.verify(() -> {

                      assertThat(foundPage3).isNotNull();
                      assertThat(foundPage3.total).isEqualTo(10);
                      assertThat(foundPage3.offset).isEqualTo(2);
                      assertThat(foundPage3.tasks).isEqualTo(tasks.subList(2, 7));
                      testContext.completeNow();

                    }));

              }));
        }));

  }

  /**
   * Check that retrieve the expected tasks by requester id.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int)
   */
  @Test
  public void shouldRetrieveTaksByRequesterId(final Vertx vertx, final VertxTestContext testContext) {

    final var requesterId = UUID.randomUUID().toString();
    final var query = TasksRepository.createTasksPageQuery(null, requesterId, null, null, null, null, null, null, null,
        null, null, null);

    testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10))
        .onSuccess(foundPage -> testContext.verify(() -> {

          assertThat(foundPage).isNotNull();
          assertThat(foundPage.total).isEqualTo(0);
          assertThat(foundPage.offset).isEqualTo(0);
          assertThat(foundPage.tasks).isNull();
          final List<Task> tasks = new ArrayList<>();
          testContext
              .assertComplete(this.storeSomeTasks(vertx, testContext, task -> task.requesterId = requesterId, 10, tasks)
                  .compose(stored -> TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10)))
              .onSuccess(foundPage2 -> testContext.verify(() -> {

                assertThat(foundPage2).isNotNull();
                assertThat(foundPage2.total).isEqualTo(10);
                assertThat(foundPage2.offset).isEqualTo(0);
                assertThat(foundPage2.tasks).isEqualTo(tasks);
                testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5))
                    .onSuccess(foundPage3 -> testContext.verify(() -> {

                      assertThat(foundPage3).isNotNull();
                      assertThat(foundPage3.total).isEqualTo(10);
                      assertThat(foundPage3.offset).isEqualTo(2);
                      assertThat(foundPage3.tasks).isEqualTo(tasks.subList(2, 7));
                      testContext.completeNow();

                    }));

              }));
        }));

  }

  /**
   * Check that retrieve the expected tasks by app id.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int)
   */
  @Test
  public void shouldRetrieveTaksByAppId(final Vertx vertx, final VertxTestContext testContext) {

    final var appId = UUID.randomUUID().toString();
    final var query = TasksRepository.createTasksPageQuery(appId, null, null, null, null, null, null, null, null, null,
        null, null);

    testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10))
        .onSuccess(foundPage -> testContext.verify(() -> {

          assertThat(foundPage).isNotNull();
          assertThat(foundPage.total).isEqualTo(0);
          assertThat(foundPage.offset).isEqualTo(0);
          assertThat(foundPage.tasks).isNull();
          final List<Task> tasks = new ArrayList<>();
          testContext
              .assertComplete(this.storeSomeTasks(vertx, testContext, task -> task.appId = appId, 10, tasks)
                  .compose(stored -> TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10)))
              .onSuccess(foundPage2 -> testContext.verify(() -> {

                assertThat(foundPage2).isNotNull();
                assertThat(foundPage2.total).isEqualTo(10);
                assertThat(foundPage2.offset).isEqualTo(0);
                assertThat(foundPage2.tasks).isEqualTo(tasks);
                testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5))
                    .onSuccess(foundPage3 -> testContext.verify(() -> {

                      assertThat(foundPage3).isNotNull();
                      assertThat(foundPage3.total).isEqualTo(10);
                      assertThat(foundPage3.offset).isEqualTo(2);
                      assertThat(foundPage3.tasks).isEqualTo(tasks.subList(2, 7));
                      testContext.completeNow();

                    }));

              }));
        }));

  }

  /**
   * Check that retrieve the expected tasks by task type id.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int)
   */
  @Test
  public void shouldRetrieveTaksByTaskTypeId(final Vertx vertx, final VertxTestContext testContext) {

    final var taskTypeId = UUID.randomUUID().toString();
    final var query = TasksRepository.createTasksPageQuery(null, null, taskTypeId, null, null, null, null, null, null,
        null, null, null);

    testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10))
        .onSuccess(foundPage -> testContext.verify(() -> {

          assertThat(foundPage).isNotNull();
          assertThat(foundPage.total).isEqualTo(0);
          assertThat(foundPage.offset).isEqualTo(0);
          assertThat(foundPage.tasks).isNull();
          final List<Task> tasks = new ArrayList<>();
          testContext
              .assertComplete(this.storeSomeTasks(vertx, testContext, task -> task.taskTypeId = taskTypeId, 10, tasks)
                  .compose(stored -> TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10)))
              .onSuccess(foundPage2 -> testContext.verify(() -> {

                assertThat(foundPage2).isNotNull();
                assertThat(foundPage2.total).isEqualTo(10);
                assertThat(foundPage2.offset).isEqualTo(0);
                assertThat(foundPage2.tasks).isEqualTo(tasks);
                testContext.assertComplete(TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5))
                    .onSuccess(foundPage3 -> testContext.verify(() -> {

                      assertThat(foundPage3).isNotNull();
                      assertThat(foundPage3.total).isEqualTo(10);
                      assertThat(foundPage3.offset).isEqualTo(2);
                      assertThat(foundPage3.tasks).isEqualTo(tasks.subList(2, 7));
                      testContext.completeNow();

                    }));

              }));
        }));

  }

  /**
   * Check can not add a transaction over an undefined task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#addTransactionIntoTask(String, TaskTransaction)
   */
  @Test
  public void shouldNotAddTransactionIntoUndefinedTask(final Vertx vertx, final VertxTestContext testContext) {

    testContext
        .assertFailure(TasksRepository.createProxy(vertx).addTransactionIntoTask("undefined", new TaskTransaction()))
        .onFailure(error -> testContext.completeNow());
  }

  /**
   * Check can not add message over undefined task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#addMessageIntoTransaction(String, String, Message)
   */
  @Test
  public void shouldNotAddMessageIntoTransactionForUndefinedTask(final Vertx vertx,
      final VertxTestContext testContext) {

    testContext
        .assertFailure(
            TasksRepository.createProxy(vertx).addMessageIntoTransaction("undefined", "undefined", new Message()))
        .onFailure(error -> testContext.completeNow());
  }

  /**
   * Check can not add message over undefined transaction.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#addMessageIntoTransaction(String, String, Message)
   */
  @Test
  public void shouldNotAddMessageIntoUndefinedTransaction(final Vertx vertx, final VertxTestContext testContext) {

    testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(new Task())).onSuccess(stored -> {

      testContext
          .assertFailure(
              TasksRepository.createProxy(vertx).addMessageIntoTransaction(stored.id, "undefined", new Message()))
          .onFailure(error -> testContext.completeNow());

    });
  }

  /**
   * Check add transaction into task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#addTransactionIntoTask(String, TaskTransaction)
   */
  @Test
  public void shouldAddTransactionIntoTask(final Vertx vertx, final VertxTestContext testContext) {

    var future = TasksRepository.createProxy(vertx).storeTask(new Task());
    for (var i = 0; i < 10; i++) {

      final var index = i;
      future = future.compose(task -> {

        final var transaction = new TaskTransactionTest().createModelExample(index);
        transaction.id = UUID.randomUUID().toString();
        transaction.taskId = task.id;
        return TasksRepository.createProxy(vertx).addTransactionIntoTask(task.id, transaction).compose(stored -> {

          testContext.verify(() -> {

            assertThat(stored.id).isEqualTo(String.valueOf(index));
            assertThat(stored).isNotEqualTo(transaction);
            transaction._creationTs = stored._creationTs;
            transaction._lastUpdateTs = stored._lastUpdateTs;
            assertThat(stored).isNotEqualTo(transaction);
            transaction.id = stored.id;
            assertThat(stored).isEqualTo(transaction);

          });
          return TasksRepository.createProxy(vertx).searchTask(task.id).compose(foundTask -> {

            testContext.verify(() -> {

              assertThat(foundTask._lastUpdateTs).isEqualTo(stored._lastUpdateTs);
              assertThat(foundTask.transactions).isNotEmpty().contains(stored);

            });

            return Future.succeededFuture(task);

          });

        });
      });

    }

    testContext.assertComplete(future).onSuccess(done -> testContext.completeNow());
  }

  /**
   * Check add message into task transaction.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#addMessageIntoTransaction(String, String, Message)
   */
  @Test
  public void shouldAddMessageIntoTransaction(final Vertx vertx, final VertxTestContext testContext) {

    final var exampleTask = new TaskTest().createModelExample(1);
    final var transactionId = exampleTask.transactions.get(0).id;
    var future = TasksRepository.createProxy(vertx).storeTask(exampleTask);
    for (var i = 0; i < 10; i++) {

      final var index = i;
      future = future.compose(task -> {

        final var message = new MessageTest().createModelExample(index);
        message.appId = task.appId;
        return TasksRepository.createProxy(vertx).addMessageIntoTransaction(task.id, transactionId, message)
            .compose(stored -> {

              testContext.verify(() -> {

                assertThat(stored).isEqualTo(message);

              });
              return TasksRepository.createProxy(vertx).searchTask(task.id).compose(foundTask -> {

                testContext.verify(() -> {

                  assertThat(foundTask._lastUpdateTs).isEqualTo(foundTask.transactions.get(0)._lastUpdateTs)
                      .isNotEqualTo(exampleTask._lastUpdateTs);
                  assertThat(foundTask.transactions.get(0).messages).isNotEmpty().contains(stored);

                });

                return Future.succeededFuture(task);

              });

            });
      });

    }

    testContext.assertComplete(future).onSuccess(done -> testContext.completeNow());
  }

  /**
   * Check add transaction and message into a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#addMessageIntoTransaction(String, String, Message)
   */
  @Test
  public void shouldAddMultipleTransactionsAndMessagesIntoTask(final Vertx vertx, final VertxTestContext testContext) {

    new TaskTest().createModelExample(1, vertx, testContext).onComplete(testContext.succeeding(example -> {

      TasksRepository.createProxy(vertx).storeTask(example).onComplete(testContext.succeeding(task -> {

        final var transaction1 = new TaskTransactionTest().createModelExample(1);
        transaction1.id = null;
        transaction1.messages = null;
        final var transaction2 = new TaskTransactionTest().createModelExample(2);
        transaction2.id = null;
        transaction2.messages = null;
        final var message1 = new MessageTest().createModelExample(3);
        final var message2 = new MessageTest().createModelExample(4);

        final var future = TasksRepository.createProxy(vertx).addTransactionIntoTask(task.id, transaction1)
            .compose(addedTransaction -> {

              transaction1.id = addedTransaction.id;
              transaction1._creationTs = addedTransaction._creationTs;
              transaction1._lastUpdateTs = task._lastUpdateTs = addedTransaction._lastUpdateTs;
              return TasksRepository.createProxy(vertx).addMessageIntoTransaction(task.id, transaction1.id, message1)
                  .compose(addedMessage -> TasksRepository.createProxy(vertx).addMessageIntoTransaction(task.id,
                      transaction1.id, message2));

            }).compose(message -> {

              return TasksRepository.createProxy(vertx).addTransactionIntoTask(task.id, transaction2)
                  .compose(addedTransaction -> {

                    transaction2.id = addedTransaction.id;
                    transaction2._creationTs = addedTransaction._creationTs;
                    transaction2._lastUpdateTs = task._lastUpdateTs = addedTransaction._lastUpdateTs;
                    return TasksRepository.createProxy(vertx)
                        .addMessageIntoTransaction(task.id, transaction2.id, message1)
                        .compose(addedMessage -> TasksRepository.createProxy(vertx).addMessageIntoTransaction(task.id,
                            transaction2.id, message2));

                  });

            }).compose(addedMessage -> TasksRepository.createProxy(vertx).searchTask(task.id));

        testContext.assertComplete(future).onSuccess(updatedTask -> testContext.verify(() -> {

          assertThat(updatedTask._lastUpdateTs).isBetween(task._lastUpdateTs, task._lastUpdateTs + 1);
          task._lastUpdateTs = updatedTask._lastUpdateTs;
          final var updatedTransaction1 = updatedTask.transactions.get(0);
          assertThat(updatedTransaction1._lastUpdateTs).isBetween(transaction1._lastUpdateTs,
              transaction2._lastUpdateTs + 1);
          transaction1._lastUpdateTs = updatedTransaction1._lastUpdateTs;
          assertThat(updatedTask).isNotEqualTo(task);
          task.transactions = new ArrayList<>();
          assertThat(updatedTask).isNotEqualTo(task);
          task.transactions.add(transaction1);
          assertThat(updatedTask).isNotEqualTo(task);
          transaction1.messages = new ArrayList<>();
          assertThat(updatedTask).isNotEqualTo(task);
          transaction1.messages.add(message1);
          assertThat(updatedTask).isNotEqualTo(task);
          transaction1.messages.add(message2);
          final var updatedTransaction2 = updatedTask.transactions.get(1);
          assertThat(updatedTransaction2._lastUpdateTs).isBetween(transaction2._lastUpdateTs,
              transaction2._lastUpdateTs + 2);
          transaction2._lastUpdateTs = updatedTransaction2._lastUpdateTs;
          assertThat(updatedTask).isNotEqualTo(task);
          task.transactions.add(transaction2);
          assertThat(updatedTask).isNotEqualTo(task);
          transaction2.messages = new ArrayList<>();
          assertThat(updatedTask).isNotEqualTo(task);
          transaction2.messages.add(message1);
          assertThat(updatedTask).isNotEqualTo(task);
          transaction2.messages.add(message2);
          assertThat(updatedTask).isEqualTo(task);

          testContext.completeNow();

        }));

      }));

    }));

  }

  /**
   * Check that not update task that can not convert to JSON.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(Task)
   */
  @Test
  public void shouldNotStoreNullTask(final Vertx vertx, final VertxTestContext testContext) {

    final var task = new Task() {

      /**
       * {@inheritDoc}
       *
       * @return {@code null} in any circumstance.
       */
      @Override
      public JsonObject toJsonObjectWithEmptyValues() {

        return null;
      }

    };
    testContext.assertFailure(TasksRepository.createProxy(vertx).updateTask(task))
        .onFailure(error -> testContext.completeNow());
  }

  /**
   * Check that delete all the task with an specified requester.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  @Test
  public void shouldDeleteAllTaskWithRequester(final Vertx vertx, final VertxTestContext testContext) {

    StoreServices.storeProfileExample(43, vertx, testContext).onSuccess(profile -> {
      final List<Task> tasks = new ArrayList<>();
      testContext.assertComplete(this.storeSomeTasks(vertx, testContext, task -> {
        if (tasks.size() % 2 == 0) {

          task.requesterId = profile.id;
        }

      }, 20, tasks)).onSuccess(any -> {

        testContext.assertComplete(TasksRepository.createProxy(vertx).deleteAllTaskWithRequester(profile.id))
            .onSuccess(ids -> testContext.verify(() -> {

              assertThat(ids).isNotNull();
              var expectedSize = 0;
              for (final var task : tasks) {

                if (task.requesterId.equals(profile.id)) {

                  assertThat(ids).contains(task.id);
                  expectedSize++;

                } else {

                  assertThat(ids).doesNotContain(task.id);
                }

              }
              assertThat(ids).hasSize(expectedSize);

              this.assertDeleted(tasks, profile.id, vertx, testContext);

            }));

      });
    });

  }

  /**
   * Check that the tasks has been removed.
   *
   * @param tasks       that has to be removed.
   * @param profileId   identifier of the task that remove it this identifier is
   *                    the requester.
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  private void assertDeleted(final List<Task> tasks, final String profileId, final Vertx vertx,
      final VertxTestContext testContext) {

    if (tasks.isEmpty()) {

      testContext.completeNow();

    } else {

      final var task = tasks.remove(0);
      if (task.requesterId.equals(profileId)) {

        testContext.assertFailure(TasksRepository.createProxy(vertx).searchTask(task.id))
            .onFailure(any -> this.assertDeleted(tasks, profileId, vertx, testContext));

      } else {

        testContext.assertComplete(TasksRepository.createProxy(vertx).searchTask(task.id))
            .onSuccess(any -> this.assertDeleted(tasks, profileId, vertx, testContext));

      }

    }
  }

  /**
   * Check that delete all the transactions with the specified actioneer.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  @Test
  public void shouldDeleteAllTransactionByActioneer(final Vertx vertx, final VertxTestContext testContext) {

    StoreServices.storeProfileExample(43, vertx, testContext).onSuccess(profile -> {
      final List<Task> tasks = new ArrayList<>();
      testContext.assertComplete(this.storeSomeTasks(vertx, testContext, task -> {
        if (tasks.size() % 2 == 0) {

          for (var i = 0; i < 10; i++) {

            final var transaction = new TaskTransactionTest().createModelExample(i);
            if (i % 2 == 0) {

              transaction.actioneerId = profile.id;
            }
            task.transactions.add(transaction);
          }
          Collections.shuffle(task.transactions);
        }

      }, 20, tasks)).onSuccess(any -> {

        testContext.assertComplete(TasksRepository.createProxy(vertx).deleteAllTransactionByActioneer(profile.id))
            .onSuccess(empty -> this.assertDeletedTransactionsBy(profile.id, tasks, vertx, testContext));
      });
    });

  }

  /**
   * Check that the transactions of an actioneer has been removed.
   *
   * @param profileId   identifier of the actionneer.
   * @param tasks       where the transactions has to be removed.
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  private void assertDeletedTransactionsBy(final String profileId, final List<Task> tasks, final Vertx vertx,
      final VertxTestContext testContext) {

    if (tasks.isEmpty()) {

      testContext.completeNow();

    } else {

      final var expected = tasks.remove(0);
      testContext.assertComplete(TasksRepository.createProxy(vertx).searchTask(expected.id)).onSuccess(updated -> {

        final var iter = expected.transactions.iterator();
        while (iter.hasNext()) {

          final var transaction = iter.next();
          if (transaction.actioneerId.equals(profileId)) {

            iter.remove();
          }
        }
        expected._lastUpdateTs = updated._lastUpdateTs;
        testContext.verify(() -> {

          assertThat(updated).isEqualTo(expected);

        });

        this.assertDeletedTransactionsBy(profileId, tasks, vertx, testContext);

      });

    }
  }

  /**
   * Check that delete all the messages with the specified receiver.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  @Test
  public void shouldDeleteAllMessagesWithReceiver(final Vertx vertx, final VertxTestContext testContext) {

    StoreServices.storeProfileExample(43, vertx, testContext).onSuccess(profile -> {
      final List<Task> tasks = new ArrayList<>();
      testContext.assertComplete(this.storeSomeTasks(vertx, testContext, task -> {
        if (tasks.size() % 2 == 0) {

          for (var i = 0; i < 10; i++) {

            final var transaction = new TaskTransactionTest().createModelExample(i);
            for (var j = 0; j < 10; j++) {

              final var message = new MessageTest().createModelExample(j);
              if (j % 2 == 0) {

                message.receiverId = profile.id;
              }
              transaction.messages.add(message);
            }

            Collections.shuffle(transaction.messages);
            task.transactions.add(transaction);
          }
          Collections.shuffle(task.transactions);
        }

      }, 20, tasks)).onSuccess(any -> {

        testContext.assertComplete(TasksRepository.createProxy(vertx).deleteAllMessagesWithReceiver(profile.id))
            .onSuccess(empty -> this.assertDeletedMessagesTo(profile.id, tasks, vertx, testContext));
      });
    });

  }

  /**
   * Check that the messages to a receiver has been removed.
   *
   * @param profileId   identifier of the receiver.
   * @param tasks       where the messages has to be removed.
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  private void assertDeletedMessagesTo(final String profileId, final List<Task> tasks, final Vertx vertx,
      final VertxTestContext testContext) {

    if (tasks.isEmpty()) {

      testContext.completeNow();

    } else {

      final var expected = tasks.remove(0);
      testContext.assertComplete(TasksRepository.createProxy(vertx).searchTask(expected.id)).onSuccess(updated -> {

        for (final var transaction : expected.transactions) {

          final var iter = transaction.messages.iterator();
          while (iter.hasNext()) {

            final var message = iter.next();
            if (message.receiverId.equals(profileId)) {

              iter.remove();
            }
          }
        }
        expected._lastUpdateTs = updated._lastUpdateTs;
        testContext.verify(() -> {

          assertThat(updated).isEqualTo(expected);

        });

        this.assertDeletedMessagesTo(profileId, tasks, vertx, testContext);

      });
    }
  }

}
