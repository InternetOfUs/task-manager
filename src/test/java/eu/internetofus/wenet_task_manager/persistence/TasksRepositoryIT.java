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

package eu.internetofus.wenet_task_manager.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.HumanDescriptionTest;
import eu.internetofus.common.components.service.Message;
import eu.internetofus.common.components.service.MessageTest;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskTest;
import eu.internetofus.common.components.task_manager.TaskTransaction;
import eu.internetofus.common.components.task_manager.TaskTransactionTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
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
   * @see TasksRepository#updateTask(Task, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateUndefinedTask(final Vertx vertx, final VertxTestContext testContext) {

    final var task = new Task();
    task.id = "undefined user identifier";
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
   * @see TasksRepository#updateTask(Task, io.vertx.core.Handler)
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
    TasksRepository.createProxy(vertx).updateTask(task, testContext.failing(failed -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can update a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#updateTask(Task, io.vertx.core.Handler)
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
      TasksRepository.createProxy(vertx).updateTask(update, testContext.succeeding(empty -> {

        testContext.assertComplete(TasksRepository.createProxy(vertx).searchTask(stored.id))
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

    TasksRepository.createProxy(vertx).deleteTask("undefined user identifier", testContext.failing(failed -> {
      testContext.completeNow();
    }));

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

    TasksRepository.createProxy(vertx).storeTask(new JsonObject(), testContext.succeeding(stored -> {

      final var id = stored.getString("id");
      TasksRepository.createProxy(vertx).deleteTask(id, testContext.succeeding(success -> {

        TasksRepository.createProxy(vertx).searchTask(id, testContext.failing(search -> {

          testContext.completeNow();

        }));

      }));

    }));

  }

  /**
   * Check that retrieve the expected tasks by goal name.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int,
   *      Handler)
   */
  @Test
  public void shouldRetrieveTaksByGoalName(final Vertx vertx, final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    final var query = TasksRepository.creteTasksPageQuery(null, null, null, name, null, null, null, null, null, null,
        null, null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);
          assertThat(search.tasks).isNull();
          final List<Task> tasks = new ArrayList<>();
          this.storeSomeTasks(vertx, testContext, task -> task.goal.name = name, 10, tasks).onSuccess(empty -> {

            TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
                testContext.succeeding(search2 -> testContext.verify(() -> {

                  assertThat(search2).isNotNull();
                  assertThat(search2.total).isEqualTo(10);
                  assertThat(search2.offset).isEqualTo(0);
                  assertThat(search2.tasks).isEqualTo(tasks);
                  TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5,
                      testContext.succeeding(search3 -> testContext.verify(() -> {

                        assertThat(search3).isNotNull();
                        assertThat(search3.total).isEqualTo(10);
                        assertThat(search3.offset).isEqualTo(2);
                        assertThat(search3.tasks).isEqualTo(tasks.subList(2, 7));
                        testContext.completeNow();

                      })));

                })));
          });
        })));

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
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int,
   *      Handler)
   */
  @Test
  public void shouldRetrieveTaksByGoalDescription(final Vertx vertx, final VertxTestContext testContext) {

    final var description = UUID.randomUUID().toString();
    final var query = TasksRepository.creteTasksPageQuery(null, null, null, null, description, null, null, null, null,
        null, null, null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);
          final List<Task> tasks = new ArrayList<>();
          this.storeSomeTasks(vertx, testContext, task -> task.goal.description = description, 10, tasks)
              .onSuccess(empty -> {

                TasksRepository.createProxy(vertx).retrieveTasksPage(query, new JsonObject(), 0, 10,
                    testContext.succeeding(search2 -> testContext.verify(() -> {

                      assertThat(search2).isNotNull();
                      assertThat(search2.total).isEqualTo(10);
                      assertThat(search2.offset).isEqualTo(0);
                      assertThat(search2.tasks).isEqualTo(tasks);
                      TasksRepository.createProxy(vertx).retrieveTasksPage(query, new JsonObject(), 2, 5,
                          testContext.succeeding(search3 -> testContext.verify(() -> {

                            assertThat(search3).isNotNull();
                            assertThat(search3.total).isEqualTo(10);
                            assertThat(search3.offset).isEqualTo(2);
                            assertThat(search3.tasks).isEqualTo(tasks.subList(2, 7));
                            testContext.completeNow();

                          })));

                    })));
              });
        })));

  }

  /**
   * Check that retrieve the expected tasks by requester id.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int,
   *      Handler)
   */
  @Test
  public void shouldRetrieveTaksByRequesterId(final Vertx vertx, final VertxTestContext testContext) {

    final var requesterId = UUID.randomUUID().toString();
    final var query = TasksRepository.creteTasksPageQuery(null, requesterId, null, null, null, null, null, null, null,
        null, null, null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);
          final List<Task> tasks = new ArrayList<>();
          this.storeSomeTasks(vertx, testContext, task -> task.requesterId = requesterId, 10, tasks)
              .onSuccess(empty -> {

                TasksRepository.createProxy(vertx).retrieveTasksPage(query, new JsonObject(), 0, 10,
                    testContext.succeeding(search2 -> testContext.verify(() -> {

                      assertThat(search2).isNotNull();
                      assertThat(search2.total).isEqualTo(10);
                      assertThat(search2.offset).isEqualTo(0);
                      assertThat(search2.tasks).isEqualTo(tasks);
                      TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5,
                          testContext.succeeding(search3 -> testContext.verify(() -> {

                            assertThat(search3).isNotNull();
                            assertThat(search3.total).isEqualTo(10);
                            assertThat(search3.offset).isEqualTo(2);
                            assertThat(search3.tasks).isEqualTo(tasks.subList(2, 7));
                            testContext.completeNow();

                          })));

                    })));
              });
        })));

  }

  /**
   * Check that retrieve the expected tasks by app id.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int,
   *      Handler)
   */
  @Test
  public void shouldRetrieveTaksByAppId(final Vertx vertx, final VertxTestContext testContext) {

    final var appId = UUID.randomUUID().toString();
    final var query = TasksRepository.creteTasksPageQuery(appId, null, null, null, null, null, null, null, null, null,
        null, null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);
          final List<Task> tasks = new ArrayList<>();
          this.storeSomeTasks(vertx, testContext, task -> task.appId = appId, 10, tasks).onSuccess(empty -> {

            TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
                testContext.succeeding(search2 -> testContext.verify(() -> {

                  assertThat(search2).isNotNull();
                  assertThat(search2.total).isEqualTo(10);
                  assertThat(search2.offset).isEqualTo(0);
                  assertThat(search2.tasks).isEqualTo(tasks);
                  this.storeSomeTasks(vertx, testContext, task -> {
                  }, 10, tasks).onSuccess(empty2 -> {
                    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5,
                        testContext.succeeding(search3 -> testContext.verify(() -> {

                          assertThat(search3).isNotNull();
                          assertThat(search3.total).isEqualTo(10);
                          assertThat(search3.offset).isEqualTo(2);
                          assertThat(search3.tasks).isEqualTo(tasks.subList(2, 7));
                          testContext.completeNow();

                        })));
                  });
                })));
          });
        })));

  }

  /**
   * Check that retrieve the expected tasks by task type id.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int,
   *      Handler)
   */
  @Test
  public void shouldRetrieveTaksByTaskTypeId(final Vertx vertx, final VertxTestContext testContext) {

    final var taskTypeId = UUID.randomUUID().toString();
    final var query = TasksRepository.creteTasksPageQuery(null, null, taskTypeId, null, null, null, null, null, null,
        null, null, null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);
          final List<Task> tasks = new ArrayList<>();
          this.storeSomeTasks(vertx, testContext, task -> task.taskTypeId = taskTypeId, 10, tasks).onSuccess(empty -> {

            TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
                testContext.succeeding(search2 -> testContext.verify(() -> {

                  assertThat(search2).isNotNull();
                  assertThat(search2.total).isEqualTo(10);
                  assertThat(search2.offset).isEqualTo(0);
                  assertThat(search2.tasks).isEqualTo(tasks);
                  this.storeSomeTasks(vertx, testContext, task -> {
                  }, 10, tasks).onSuccess(empty2 -> {
                    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5,
                        testContext.succeeding(search3 -> testContext.verify(() -> {

                          assertThat(search3).isNotNull();
                          assertThat(search3.total).isEqualTo(10);
                          assertThat(search3.offset).isEqualTo(2);
                          assertThat(search3.tasks).isEqualTo(tasks.subList(2, 7));
                          testContext.completeNow();

                        })));
                  });
                })));
          });
        })));

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

        var transaction = new TaskTransactionTest().createModelExample(index);
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

    var exampleTask = new TaskTest().createModelExample(1);
    var transactionId = exampleTask.transactions.get(0).id;
    var future = TasksRepository.createProxy(vertx).storeTask(exampleTask);
    for (var i = 0; i < 10; i++) {

      final var index = i;
      future = future.compose(task -> {

        var message = new MessageTest().createModelExample(index);
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
  public void shouldAddTransactionAndMessageIntoTask(final Vertx vertx, final VertxTestContext testContext) {

    var task = new TaskTest().createModelExample(1);
    task.id = null;
    var transaction = new TaskTransactionTest().createModelExample(2);
    transaction.id = null;
    var message = new MessageTest().createModelExample(3);
    var future = TasksRepository.createProxy(vertx).storeTask(task).compose(createdTask -> {

      task.id = createdTask.id;
      task._creationTs = createdTask._creationTs;
      task._lastUpdateTs = createdTask._lastUpdateTs;
      transaction.taskId = task.id;
      return TasksRepository.createProxy(vertx).addTransactionIntoTask(task.id, transaction);

    }).compose(addedTransaction -> {

      transaction.id = addedTransaction.id;
      transaction._creationTs = addedTransaction._creationTs;
      transaction._lastUpdateTs = task._lastUpdateTs = addedTransaction._lastUpdateTs;
      return TasksRepository.createProxy(vertx).addMessageIntoTransaction(task.id, transaction.id, message);

    }).compose(addedMessage -> TasksRepository.createProxy(vertx).searchTask(task.id));

    testContext.assertComplete(future).onSuccess(updatedTask -> testContext.verify(() -> {

      assertThat(updatedTask._lastUpdateTs).isBetween(task._lastUpdateTs, task._lastUpdateTs + 1);
      task._lastUpdateTs = updatedTask._lastUpdateTs;
      var updatedTransaction = updatedTask.transactions.get(1);
      assertThat(updatedTransaction._lastUpdateTs).isBetween(transaction._lastUpdateTs, transaction._lastUpdateTs + 1);
      transaction._lastUpdateTs = updatedTransaction._lastUpdateTs;
      assertThat(updatedTask).isNotEqualTo(task);
      task.transactions.add(transaction);
      assertThat(updatedTask).isNotEqualTo(task);
      transaction.messages.add(message);
      assertThat(updatedTask).isEqualTo(task);

      testContext.completeNow();

    }));
  }
}
