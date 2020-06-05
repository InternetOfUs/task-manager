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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.common.TimeManager;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskGoalTest;
import eu.internetofus.common.components.task_manager.TaskTest;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;

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
   * @see TasksRepository#searchTaskObject(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundUndefinedTaskObject(final Vertx vertx, final VertxTestContext testContext) {

    TasksRepository.createProxy(vertx).searchTaskObject("undefined user identifier", testContext.failing(failed -> {
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

      TasksRepository.createProxy(vertx).searchTask(storedTask.id, testContext.succeeding(foundTask -> testContext.verify(() -> {
        assertThat(foundTask).isEqualTo(storedTask);
        testContext.completeNow();
      })));

    }));

  }

  /**
   * Verify that can found a task object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTaskObject(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldFoundTaskObject(final Vertx vertx, final VertxTestContext testContext) {

    TasksRepository.createProxy(vertx).storeTask(new JsonObject(), testContext.succeeding(storedTask -> {

      TasksRepository.createProxy(vertx).searchTaskObject(storedTask.getString("id"), testContext.succeeding(foundTask -> testContext.verify(() -> {
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

    final Task task = new Task();
    task._creationTs = 0;
    task._lastUpdateTs = 1;
    final long now = TimeManager.now();
    TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(storedTask -> testContext.verify(() -> {

      assertThat(storedTask).isNotNull();
      assertThat(storedTask.id).isNotEmpty();
      assertThat(storedTask._creationTs).isNotEqualTo(0).isGreaterThanOrEqualTo(now);
      assertThat(storedTask._lastUpdateTs).isNotEqualTo(1).isGreaterThanOrEqualTo(now);
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

    final String id = UUID.randomUUID().toString();
    final Task task = new Task();
    task.id = id;
    task._creationTs = 0;
    task._lastUpdateTs = 1;
    final long now = TimeManager.now();
    TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(storedTask -> testContext.verify(() -> {

      assertThat(storedTask.id).isEqualTo(id);
      assertThat(storedTask._creationTs).isNotEqualTo(0).isGreaterThanOrEqualTo(now);
      assertThat(storedTask._lastUpdateTs).isNotEqualTo(1).isGreaterThanOrEqualTo(now);
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

    final String id = UUID.randomUUID().toString();
    final Task task = new Task();
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

    final long now = TimeManager.now();
    TasksRepository.createProxy(vertx).storeTask(new JsonObject(), testContext.succeeding(storedTask -> testContext.verify(() -> {

      assertThat(storedTask).isNotNull();
      final String id = storedTask.getString("id");
      assertThat(id).isNotEmpty();
      assertThat(storedTask.getLong("_creationTs", 0l)).isNotEqualTo(0).isGreaterThanOrEqualTo(now);
      assertThat(storedTask.getLong("_lastUpdateTs", 1l)).isNotEqualTo(1).isGreaterThanOrEqualTo(now);
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

    final Task task = new Task();
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

    final JsonObject task = new JsonObject().put("id", "undefined user identifier");
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

    final Task task = new Task();
    task.goal = new TaskGoalTest().createModelExample(23);
    TasksRepository.createProxy(vertx).storeTask(task, testContext.succeeding(stored -> {

      final long now = TimeManager.now();
      final Task update = new TaskTest().createModelExample(23);
      update.id = stored.id;
      update._creationTs = stored._creationTs;
      update._lastUpdateTs = 1;
      TasksRepository.createProxy(vertx).updateTask(update, testContext.succeeding(empty -> {

        TasksRepository.createProxy(vertx).searchTask(stored.id, testContext.succeeding(foundTask -> testContext.verify(() -> {

          assertThat(stored).isNotNull();
          assertThat(foundTask.id).isNotEmpty().isEqualTo(stored.id);
          assertThat(foundTask._creationTs).isEqualTo(stored._creationTs);
          assertThat(foundTask._lastUpdateTs).isGreaterThanOrEqualTo(now);
          update._lastUpdateTs = foundTask._lastUpdateTs;
          assertThat(foundTask).isEqualTo(update);
          testContext.completeNow();
        })));
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

    TasksRepository.createProxy(vertx).storeTask(new JsonObject().put("nationality", "Italian"), testContext.succeeding(stored -> testContext.verify(() -> {

      final String id = stored.getString("id");
      final JsonObject update = new JsonObject().put("id", id).put("occupation", "Unemployed");
      TasksRepository.createProxy(vertx).updateTask(update, testContext.succeeding(empty -> testContext.verify(() -> {

        TasksRepository.createProxy(vertx).searchTaskObject(id, testContext.succeeding(foundTask -> testContext.verify(() -> {
          stored.put("_lastUpdateTs", foundTask.getLong("_lastUpdateTs"));
          stored.put("occupation", "Unemployed");
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

      final String id = stored.getString("id");
      TasksRepository.createProxy(vertx).deleteTask(id, testContext.succeeding(success -> {

        TasksRepository.createProxy(vertx).searchTaskObject(id, testContext.failing(search -> {

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
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaksByGoalName(final Vertx vertx, final VertxTestContext testContext) {

    final String name = UUID.randomUUID().toString();
    final JsonObject query = TasksRepository.creteTasksPageQuery(null, null, null,  name , null, null, null, null, null, null, null,null,null,null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search -> testContext.verify(() -> {

      assertThat(search).isNotNull();
      assertThat(search.total).isEqualTo(0);
      assertThat(search.offset).isEqualTo(0);
      assertThat(search.tasks).isNull();
      final List<Task> tasks = new ArrayList<>();
      this.storeSomeTasks(vertx, testContext, task -> task.goal.name = name, 10, tasks, testContext.succeeding(empty -> {

        TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search2 -> testContext.verify(() -> {

          assertThat(search2).isNotNull();
          assertThat(search2.total).isEqualTo(10);
          assertThat(search2.offset).isEqualTo(0);
          assertThat(search2.tasks).isEqualTo(tasks);
          TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5, testContext.succeeding(search3 -> testContext.verify(() -> {

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
   *
   * Create some {@link TaskTest#createModelExample(int)}.
   *
   * @param vertx           event bus to use.
   * @param testContext     context that executes the test.
   * @param change          function to modify the pattern before to store it.
   * @param max             number maximum of tasks to create.
   * @param tasks           list to add the created tasks.
   * @param creationHandler that manage the creation.
   */
  public void storeSomeTasks(final Vertx vertx, final VertxTestContext testContext, final Consumer<Task> change, final int max, final List<Task> tasks, final Handler<AsyncResult<Void>> creationHandler) {

    if (tasks.size() == max) {

      creationHandler.handle(Future.succeededFuture());

    } else {

      final Task task = new TaskTest().createModelExample(tasks.size());
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
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaksByGoalDescription(final Vertx vertx, final VertxTestContext testContext) {

    final String description = UUID.randomUUID().toString();
    final JsonObject query = TasksRepository.creteTasksPageQuery(null, null, null, null,  description , null, null, null, null, null, null,null,null,null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search -> testContext.verify(() -> {

      assertThat(search).isNotNull();
      assertThat(search.total).isEqualTo(0);
      assertThat(search.offset).isEqualTo(0);
      final List<Task> tasks = new ArrayList<>();
      this.storeSomeTasks(vertx, testContext, task -> task.goal.description = description, 10, tasks, testContext.succeeding(empty -> {

        TasksRepository.createProxy(vertx).retrieveTasksPage(query, new JsonObject(), 0, 10, testContext.succeeding(search2 -> testContext.verify(() -> {

          assertThat(search2).isNotNull();
          assertThat(search2.total).isEqualTo(10);
          assertThat(search2.offset).isEqualTo(0);
          assertThat(search2.tasks).isEqualTo(tasks);
          TasksRepository.createProxy(vertx).retrieveTasksPage(query, new JsonObject(), 2, 5, testContext.succeeding(search3 -> testContext.verify(() -> {

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
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaksByRequesterId(final Vertx vertx, final VertxTestContext testContext) {

    final String requesterId = UUID.randomUUID().toString();
    final JsonObject query = TasksRepository.creteTasksPageQuery(null,  requesterId , null, null, null, null, null, null, null, null, null,null,null,null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search -> testContext.verify(() -> {

      assertThat(search).isNotNull();
      assertThat(search.total).isEqualTo(0);
      assertThat(search.offset).isEqualTo(0);
      final List<Task> tasks = new ArrayList<>();
      this.storeSomeTasks(vertx, testContext, task -> task.requesterId = requesterId, 10, tasks, testContext.succeeding(empty -> {

        TasksRepository.createProxy(vertx).retrieveTasksPage(query, new JsonObject(), 0, 10, testContext.succeeding(search2 -> testContext.verify(() -> {

          assertThat(search2).isNotNull();
          assertThat(search2.total).isEqualTo(10);
          assertThat(search2.offset).isEqualTo(0);
          assertThat(search2.tasks).isEqualTo(tasks);
          TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5, testContext.succeeding(search3 -> testContext.verify(() -> {

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
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaksByAppId(final Vertx vertx, final VertxTestContext testContext) {

    final String appId = UUID.randomUUID().toString();
    final JsonObject query = TasksRepository.creteTasksPageQuery(appId , null, null, null, null, null, null, null, null, null, null,null,null,null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search -> testContext.verify(() -> {

      assertThat(search).isNotNull();
      assertThat(search.total).isEqualTo(0);
      assertThat(search.offset).isEqualTo(0);
      final List<Task> tasks = new ArrayList<>();
      this.storeSomeTasks(vertx, testContext, task -> task.appId = appId, 10, tasks, testContext.succeeding(empty -> {

        TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search2 -> testContext.verify(() -> {

          assertThat(search2).isNotNull();
          assertThat(search2.total).isEqualTo(10);
          assertThat(search2.offset).isEqualTo(0);
          assertThat(search2.tasks).isEqualTo(tasks);
          this.storeSomeTasks(vertx, testContext, task -> {
          }, 10, tasks, testContext.succeeding(empty2 -> {
            TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5, testContext.succeeding(search3 -> testContext.verify(() -> {

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
   * @see TasksRepository#retrieveTasksPage(JsonObject, JsonObject, int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaksByTaskTypeId(final Vertx vertx, final VertxTestContext testContext) {

    final String taskTypeId = UUID.randomUUID().toString();
    final JsonObject query = TasksRepository.creteTasksPageQuery(null, null, taskTypeId, null, null, null, null, null, null, null, null,null,null,null);

    TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search -> testContext.verify(() -> {

      assertThat(search).isNotNull();
      assertThat(search.total).isEqualTo(0);
      assertThat(search.offset).isEqualTo(0);
      final List<Task> tasks = new ArrayList<>();
      this.storeSomeTasks(vertx, testContext, task -> task.taskTypeId = taskTypeId, 10, tasks, testContext.succeeding(empty -> {

        TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 0, 10, testContext.succeeding(search2 -> testContext.verify(() -> {

          assertThat(search2).isNotNull();
          assertThat(search2.total).isEqualTo(10);
          assertThat(search2.offset).isEqualTo(0);
          assertThat(search2.tasks).isEqualTo(tasks);
          this.storeSomeTasks(vertx, testContext, task -> {
          }, 10, tasks, testContext.succeeding(empty2 -> {
            TasksRepository.createProxy(vertx).retrieveTasksPage(query, null, 2, 5, testContext.succeeding(search3 -> testContext.verify(() -> {

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
