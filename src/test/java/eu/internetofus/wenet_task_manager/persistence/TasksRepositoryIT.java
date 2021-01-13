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
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.AsyncResult;
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

    TasksRepository.createProxy(vertx).storeTask(new Task(), testContext.succeeding(storedTask -> {

      testContext.assertComplete(TasksRepository.createProxy(vertx).searchTask(storedTask.id))
          .onSuccess(foundTask -> testContext.verify(() -> {
            assertThat(foundTask).isEqualTo(storedTask);
            testContext.completeNow();
          }));
    }));

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
   * @see TasksRepository#storeTask(Task, Handler)
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
    TasksRepository.createProxy(vertx).storeTask(task, testContext.failing(failed -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can store a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task, Handler)
   */
  @Test
  public void shouldStoreTask(final Vertx vertx, final VertxTestContext testContext) {

    final var task = new Task();
    task._creationTs = 0;
    task._lastUpdateTs = 1;

    TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(storedTask -> testContext.verify(() -> {

      assertThat(storedTask).isNotNull();
      assertThat(storedTask.id).isNotEmpty();
      assertThat(storedTask._creationTs).isEqualTo(0);
      assertThat(storedTask._lastUpdateTs).isEqualTo(1);
      testContext.completeNow();
    })));

  }

  /**
   * Verify that can store a task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task, Handler)
   */
  @Test
  public void shouldStoreTaskWithAnId(final Vertx vertx, final VertxTestContext testContext) {

    final var id = UUID.randomUUID().toString();
    final var task = new Task();
    task.id = id;
    task._creationTs = 2;
    task._lastUpdateTs = 3;
    TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(storedTask -> testContext.verify(() -> {

      assertThat(storedTask.id).isEqualTo(id);
      assertThat(storedTask._creationTs).isEqualTo(2);
      assertThat(storedTask._lastUpdateTs).isEqualTo(3);
      testContext.completeNow();
    })));

  }

  /**
   * Verify that can store a task with an id of an stored task.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task, Handler)
   */
  @Test
  public void shouldNotStoreTwoTaskWithTheSameId(final Vertx vertx, final VertxTestContext testContext) {

    final var id = UUID.randomUUID().toString();
    final var task = new Task();
    task.id = id;
    TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(storedTask -> testContext.verify(() -> {

      TasksRepository.createProxy(vertx).storeTask(task, testContext.failing(error -> testContext.completeNow()));

    })));

  }

  /**
   * Verify that can store a task object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task, Handler)
   */
  @Test
  public void shouldStoreTaskObject(final Vertx vertx, final VertxTestContext testContext) {

    TasksRepository.createProxy(vertx).storeTask(new JsonObject(),
        testContext.succeeding(storedTask -> testContext.verify(() -> {

          assertThat(storedTask).isNotNull();
          final var id = storedTask.getString("id");
          assertThat(id).isNotEmpty();
          assertThat(storedTask.containsKey("_creationTs")).isFalse();
          assertThat(storedTask.containsKey("_lastUpdateTs")).isFalse();
          testContext.completeNow();
        })));

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
    TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(stored -> {

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

    }));

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
          this.storeSomeTasks(vertx, testContext, task -> task.goal.name = name, 10, tasks,
              testContext.succeeding(empty -> {

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
              }));
        })));

  }

  /**
   * Create some {@link TaskTest#createModelExample(int)}.
   *
   * @param vertx           event bus to use.
   * @param testContext     context that executes the test.
   * @param change          function to modify the pattern before to store it.
   * @param max             number maximum of tasks to create.
   * @param tasks           list to add the created tasks.
   * @param creationHandler that manage the creation.
   */
  public void storeSomeTasks(final Vertx vertx, final VertxTestContext testContext, final Consumer<Task> change,
      final int max, final List<Task> tasks, final Handler<AsyncResult<Void>> creationHandler) {

    if (tasks.size() == max) {

      creationHandler.handle(Future.succeededFuture());

    } else {

      final var task = new TaskTest().createModelExample(tasks.size());
      change.accept(task);
      TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(stored -> {

        tasks.add(stored);
        this.storeSomeTasks(vertx, testContext, change, max, tasks, creationHandler);
      }));
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
          this.storeSomeTasks(vertx, testContext, task -> task.goal.description = description, 10, tasks,
              testContext.succeeding(empty -> {

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
              }));
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
          this.storeSomeTasks(vertx, testContext, task -> task.requesterId = requesterId, 10, tasks,
              testContext.succeeding(empty -> {

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
              }));
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
          this.storeSomeTasks(vertx, testContext, task -> task.appId = appId, 10, tasks,
              testContext.succeeding(empty -> {

                TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
                    testContext.succeeding(search2 -> testContext.verify(() -> {

                      assertThat(search2).isNotNull();
                      assertThat(search2.total).isEqualTo(10);
                      assertThat(search2.offset).isEqualTo(0);
                      assertThat(search2.tasks).isEqualTo(tasks);
                      this.storeSomeTasks(vertx, testContext, task -> {
                      }, 10, tasks, testContext.succeeding(empty2 -> {
                        TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5,
                            testContext.succeeding(search3 -> testContext.verify(() -> {

                              assertThat(search3).isNotNull();
                              assertThat(search3.total).isEqualTo(10);
                              assertThat(search3.offset).isEqualTo(2);
                              assertThat(search3.tasks).isEqualTo(tasks.subList(2, 7));
                              testContext.completeNow();

                            })));
                      }));
                    })));
              }));
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
          this.storeSomeTasks(vertx, testContext, task -> task.taskTypeId = taskTypeId, 10, tasks,
              testContext.succeeding(empty -> {

                TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10,
                    testContext.succeeding(search2 -> testContext.verify(() -> {

                      assertThat(search2).isNotNull();
                      assertThat(search2.total).isEqualTo(10);
                      assertThat(search2.offset).isEqualTo(0);
                      assertThat(search2.tasks).isEqualTo(tasks);
                      this.storeSomeTasks(vertx, testContext, task -> {
                      }, 10, tasks, testContext.succeeding(empty2 -> {
                        TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5,
                            testContext.succeeding(search3 -> testContext.verify(() -> {

                              assertThat(search3).isNotNull();
                              assertThat(search3.total).isEqualTo(10);
                              assertThat(search3.offset).isEqualTo(2);
                              assertThat(search3.tasks).isEqualTo(tasks.subList(2, 7));
                              testContext.completeNow();

                            })));
                      }));
                    })));
              }));
        })));

  }

}
