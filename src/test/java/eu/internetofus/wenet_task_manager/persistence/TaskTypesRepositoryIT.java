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

import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.components.task_manager.TaskTypeTest;
import eu.internetofus.common.components.task_manager.WeNetTaskManager;
import eu.internetofus.common.vertx.ModelsPageContext;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration test over the {@link TaskTypesRepository}.
 *
 * @see TaskTypesRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TaskTypesRepositoryIT {

  /**
   * Verify that can not found a taskType if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundUndefinedTaskType(final Vertx vertx, final VertxTestContext testContext) {

    testContext.assertFailure(TaskTypesRepository.createProxy(vertx).searchTaskType("undefined taskType identifier"))
        .onFailure(failed -> testContext.completeNow());

  }

  /**
   * Verify that can not found a taskType object if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundUndefinedTaskTypeObject(final Vertx vertx, final VertxTestContext testContext) {

    TaskTypesRepository.createProxy(vertx).searchTaskType("undefined taskType identifier",
        testContext.failing(failed -> {
          testContext.completeNow();
        }));

  }

  /**
   * Verify that can found a taskType.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldFoundTaskType(final Vertx vertx, final VertxTestContext testContext) {

    final var repository = TaskTypesRepository.createProxy(vertx);
    testContext.assertComplete(repository.storeTaskType(new TaskType()).onSuccess(storedTaskType -> {

      testContext.assertComplete(repository.searchTaskType(storedTaskType.id))
          .onSuccess(foundTaskType -> testContext.verify(() -> {
            assertThat(foundTaskType).isEqualTo(storedTaskType);
            testContext.completeNow();
          }));

    }));

  }

  /**
   * Verify that can found a taskType object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldFoundTaskTypeObject(final Vertx vertx, final VertxTestContext testContext) {

    final var repository = TaskTypesRepository.createProxy(vertx);
    repository.storeTaskType(new JsonObject(), testContext.succeeding(storedTaskType -> {

      repository.searchTaskType(storedTaskType.getString("id"),
          testContext.succeeding(foundTaskType -> testContext.verify(() -> {
            assertThat(foundTaskType).isEqualTo(storedTaskType);
            testContext.completeNow();
          })));

    }));

  }

  /**
   * Verify that can not store a taskType that can not be an object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#storeTaskType(TaskType)
   */
  @Test
  public void shouldNotStoreATaskTypeThatCanNotBeAnObject(final Vertx vertx, final VertxTestContext testContext) {

    final TaskType taskType = new TaskType() {

      /**
       * {@inheritDoc}
       */
      @Override
      public JsonObject toJsonObject() {

        return null;
      }
    };
    taskType.id = "undefined taskType identifier";
    testContext.assertFailure(TaskTypesRepository.createProxy(vertx).storeTaskType(taskType))
        .onFailure(failed -> testContext.completeNow());

  }

  /**
   * Verify that can store a taskType.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#storeTaskType(TaskType)
   */
  @Test
  public void shouldStoreTaskType(final Vertx vertx, final VertxTestContext testContext) {

    final var taskType = new TaskType();
    taskType._creationTs = 0;
    taskType._lastUpdateTs = 1;
    testContext.assertComplete(TaskTypesRepository.createProxy(vertx).storeTaskType(taskType))
        .onSuccess(storedTaskType -> testContext.verify(() -> {

          assertThat(storedTaskType).isNotNull();
          assertThat(storedTaskType.id).isNotEmpty();
          assertThat(storedTaskType._creationTs).isEqualTo(0);
          assertThat(storedTaskType._lastUpdateTs).isEqualTo(1);
          testContext.completeNow();
        }));

  }

  /**
   * Verify that can store a taskType.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#storeTaskType(TaskType)
   */
  @Test
  public void shouldStoreTaskTypeWithAnId(final Vertx vertx, final VertxTestContext testContext) {

    final var id = UUID.randomUUID().toString();
    final var taskType = new TaskType();
    taskType.id = id;
    taskType._creationTs = 1;
    taskType._lastUpdateTs = 2;
    testContext.assertComplete(TaskTypesRepository.createProxy(vertx).storeTaskType(taskType))
        .onSuccess(storedTaskType -> testContext.verify(() -> {

          assertThat(storedTaskType.id).isEqualTo(id);
          assertThat(storedTaskType._creationTs).isEqualTo(1);
          assertThat(storedTaskType._lastUpdateTs).isEqualTo(2);
          testContext.completeNow();
        }));

  }

  /**
   * Verify that can store a taskType with an id of an stored taskType.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#storeTaskType(TaskType)
   */
  @Test
  public void shouldNotStoreTwoTaskTypeWithTheSameId(final Vertx vertx, final VertxTestContext testContext) {

    final var id = UUID.randomUUID().toString();
    final var taskType = new TaskType();
    taskType.id = id;
    final var repository = TaskTypesRepository.createProxy(vertx);
    testContext.assertComplete(repository.storeTaskType(taskType)).onSuccess(storedTaskType -> testContext
        .assertFailure(repository.storeTaskType(taskType)).onFailure(error -> testContext.completeNow()));

  }

  /**
   * Verify that can store a taskType object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#storeTaskType(JsonObject, Handler)
   */
  @Test
  public void shouldStoreTaskTypeObject(final Vertx vertx, final VertxTestContext testContext) {

    TaskTypesRepository.createProxy(vertx).storeTaskType(new JsonObject(),
        testContext.succeeding(storedTaskType -> testContext.verify(() -> {

          assertThat(storedTaskType).isNotNull();
          final var id = storedTaskType.getString("id");
          assertThat(id).isNotEmpty();
          assertThat(storedTaskType.containsKey("_creationTs")).isFalse();
          assertThat(storedTaskType.containsKey("_lastUpdateTs")).isFalse();
          testContext.completeNow();
        })));

  }

  /**
   * Verify that can not update a taskType if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#updateTaskType(TaskType)
   */
  @Test
  public void shouldNotUpdateUndefinedTaskType(final Vertx vertx, final VertxTestContext testContext) {

    final var taskType = new TaskType();
    taskType.id = "undefined taskType identifier";
    testContext.assertFailure(TaskTypesRepository.createProxy(vertx).updateTaskType(taskType))
        .onFailure(failed -> testContext.completeNow());

  }

  /**
   * Verify that can not update a taskType if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#updateTaskType(JsonObject, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateUndefinedTaskTypeObject(final Vertx vertx, final VertxTestContext testContext) {

    final var taskType = new JsonObject().put("id", "undefined taskType identifier");
    TaskTypesRepository.createProxy(vertx).updateTaskType(taskType, testContext.failing(failed -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can not update a taskType if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#updateTaskType(TaskType)
   */
  @Test
  public void shouldNotUpdateATaskTypeThatCanNotBeAnObject(final Vertx vertx, final VertxTestContext testContext) {

    final TaskType taskType = new TaskType() {

      /**
       * {@inheritDoc}
       */
      @Override
      public JsonObject toJsonObject() {

        return null;
      }
    };
    taskType.id = "undefined taskType identifier";
    testContext.assertFailure(TaskTypesRepository.createProxy(vertx).updateTaskType(taskType))
        .onFailure(failed -> testContext.completeNow());

  }

  /**
   * Verify that can update a taskType.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#updateTaskType(TaskType)
   */
  @Test
  public void shouldUpdateTaskType(final Vertx vertx, final VertxTestContext testContext) {

    final var taskType = new TaskType();
    taskType.name = "NEW NAME";
    final var repository = TaskTypesRepository.createProxy(vertx);
    testContext.assertComplete(repository.storeTaskType(taskType)).onSuccess(stored -> {

      final var update = new TaskTypeTest().createModelExample(23);
      update.id = stored.id;
      update._creationTs = stored._creationTs;
      update._lastUpdateTs = 1;

      testContext
          .assertComplete(repository.updateTaskType(update).compose(empty -> repository.searchTaskType(stored.id)))
          .onSuccess(foundTaskType -> testContext.verify(() -> {

            assertThat(stored).isNotNull();
            assertThat(foundTaskType.id).isNotEmpty().isEqualTo(stored.id);
            assertThat(foundTaskType._creationTs).isEqualTo(stored._creationTs);
            assertThat(foundTaskType._lastUpdateTs).isEqualTo(1);
            update._lastUpdateTs = foundTaskType._lastUpdateTs;
            assertThat(foundTaskType).isEqualTo(update);
            testContext.completeNow();

          }));

    });
  }

  /**
   * Verify that update a defined taskType object.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#updateTaskType(JsonObject, io.vertx.core.Handler)
   */
  @Test
  public void shouldUpdateTaskTypeObject(final Vertx vertx, final VertxTestContext testContext) {

    final var repository = TaskTypesRepository.createProxy(vertx);
    final var createTs = 123;
    final var updateTs = 456;
    repository.storeTaskType(
        new JsonObject().put("name", "TaskType Name").put("_creationTs", createTs).put("_lastUpdateTs", updateTs),
        testContext.succeeding(stored -> testContext.verify(() -> {

          final var id = stored.getString("id");
          final var update = new JsonObject().put("id", id).put("description", "TaskType Description")
              .put("_creationTs", createTs + 12345).put("_lastUpdateTs", updateTs + 12345);
          repository.updateTaskType(update, testContext.succeeding(empty -> testContext.verify(() -> {

            repository.searchTaskType(id, testContext.succeeding(foundTaskType -> testContext.verify(() -> {
              stored.put("_lastUpdateTs", updateTs + 12345);
              stored.put("description", "TaskType Description");
              assertThat(foundTaskType).isEqualTo(stored);
              testContext.completeNow();
            })));
          })));

        })));

  }

  /**
   * Verify that can not delete a taskType if it is not defined.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotDeleteUndefinedTaskType(final Vertx vertx, final VertxTestContext testContext) {

    TaskTypesRepository.createProxy(vertx).deleteTaskType("undefined taskType identifier",
        testContext.failing(failed -> {
          testContext.completeNow();
        }));

  }

  /**
   * Verify that can delete a taskType.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#updateTaskType(JsonObject, io.vertx.core.Handler)
   */
  @Test
  public void shouldDeleteTaskType(final Vertx vertx, final VertxTestContext testContext) {

    final var repository = TaskTypesRepository.createProxy(vertx);
    repository.storeTaskType(new JsonObject(), testContext.succeeding(stored -> {

      final var id = stored.getString("id");
      repository.deleteTaskType(id, testContext.succeeding(success -> {

        repository.searchTaskType(id, testContext.failing(search -> {

          testContext.completeNow();

        }));

      }));

    }));

  }

  /**
   * Create some {@link TaskTypeTest#createModelExample(int)}.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   * @param change      function to modify the pattern before to store it.
   * @param max         number maximum of taskTypes to create.
   *
   * @return the future created task typea.
   */
  public static Future<List<TaskType>> assertStoreSomeTaskTypes(final Vertx vertx, final VertxTestContext testContext,
      final BiConsumer<Integer, TaskType> change, final int max) {

    Future<List<TaskType>> future = Future.succeededFuture(new ArrayList<TaskType>());
    for (var i = 0; i < max; i++) {

      final var taskType = new TaskTypeTest().createModelExample(i);
      change.accept(i, taskType);
      taskType.id = null;
      future = future.compose(taskTypes -> {

        return TaskTypesRepository.createProxy(vertx).storeTaskType(taskType).compose(stored -> {

          taskTypes.add(stored);
          return Future.succeededFuture(taskTypes);
        });
      });

    }

    return testContext.assertComplete(future);

  }

  /**
   * Check that retrieve the expected tasks by goal name.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPageObject(JsonObject, JsonObject,
   *      int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaskTypesByName(final Vertx vertx, final VertxTestContext testContext) {

    final var name = UUID.randomUUID().toString();
    final ModelsPageContext context = new ModelsPageContext();
    context.query = TaskTypesRepository.createTaskTypesPageQuery("/.*" + name + ".*/", null, null);
    context.limit = 10;
    TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(context,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);

          assertStoreSomeTaskTypes(vertx, testContext, (index, taskType) -> taskType.name = name + "_" + index, 10)
              .onSuccess(taskTypes -> {

                context.sort = new JsonObject().put("name", -1);
                TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(context,
                    testContext.succeeding(search2 -> testContext.verify(() -> {

                      assertThat(search2).isNotNull();
                      assertThat(search2.total).isEqualTo(10);
                      assertThat(search2.offset).isEqualTo(0);
                      Collections.reverse(taskTypes);
                      assertThat(search2.taskTypes).isEqualTo(taskTypes);
                      context.sort = new JsonObject().put("name", 1);

                      context.offset = 2;
                      TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(context,
                          testContext.succeeding(search3 -> testContext.verify(() -> {

                            Collections.reverse(taskTypes);
                            assertThat(search3).isNotNull();
                            assertThat(search3.total).isEqualTo(10);
                            assertThat(search3.offset).isEqualTo(2);
                            assertThat(search3.taskTypes).isEqualTo(taskTypes.subList(2, 10));
                            testContext.completeNow();

                          })));

                    })));
              });
        })));

  }

  /**
   * Check that retrieve the expected tasks by goal description.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPageObject(JsonObject, JsonObject,
   *      int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaskTypesByDescription(final Vertx vertx, final VertxTestContext testContext) {

    final var description = UUID.randomUUID().toString();
    final var query = TaskTypesRepository.createTaskTypesPageQuery(null, description, null);
    TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(query, null, 0, 10,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);

          assertStoreSomeTaskTypes(vertx, testContext, (index, taskType) -> taskType.description = description, 10)
              .onSuccess(taskTypes -> {

                TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(query, new JsonObject(), 0, 10,
                    testContext.succeeding(search2 -> testContext.verify(() -> {

                      assertThat(search2).isNotNull();
                      assertThat(search2.total).isEqualTo(10);
                      assertThat(search2.offset).isEqualTo(0);
                      assertThat(search2.taskTypes).isEqualTo(taskTypes);
                      TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(query, new JsonObject(), 2, 5,
                          testContext.succeeding(search3 -> testContext.verify(() -> {

                            assertThat(search3).isNotNull();
                            assertThat(search3.total).isEqualTo(10);
                            assertThat(search3.offset).isEqualTo(2);
                            assertThat(search3.taskTypes).isEqualTo(taskTypes.subList(2, 7));
                            testContext.completeNow();

                          })));

                    })));
              });
        })));

  }

  /**
   * Check that retrieve the expected tasks by goal keywords.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPageObject(JsonObject, JsonObject,
   *      int, int, Handler)
   */
  @Test
  public void shouldRetrieveTaskTypesByKeywords(final Vertx vertx, final VertxTestContext testContext) {

    final var keywords = new ArrayList<String>();
    final var keyword = UUID.randomUUID().toString();
    keywords.add(keyword);
    final var query = TaskTypesRepository.createTaskTypesPageQuery(null, null, keywords);
    TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(query, null, 0, 10,
        testContext.succeeding(search -> testContext.verify(() -> {

          assertThat(search).isNotNull();
          assertThat(search.total).isEqualTo(0);
          assertThat(search.offset).isEqualTo(0);

          assertStoreSomeTaskTypes(vertx, testContext, (index, taskType) -> taskType.keywords.add(keyword), 10)
              .onSuccess(taskTypes -> {

                TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(query, new JsonObject(), 0, 10,
                    testContext.succeeding(search2 -> testContext.verify(() -> {

                      assertThat(search2).isNotNull();
                      assertThat(search2.total).isEqualTo(10);
                      assertThat(search2.offset).isEqualTo(0);
                      assertThat(search2.taskTypes).isEqualTo(taskTypes);
                      TaskTypesRepository.createProxy(vertx).retrieveTaskTypesPage(query, new JsonObject(), 2, 5,
                          testContext.succeeding(search3 -> testContext.verify(() -> {

                            assertThat(search3).isNotNull();
                            assertThat(search3.total).isEqualTo(10);
                            assertThat(search3.offset).isEqualTo(2);
                            assertThat(search3.taskTypes).isEqualTo(taskTypes.subList(2, 7));
                            testContext.completeNow();

                          })));

                    })));
              });
        })));

  }

  /**
   * Check that the hardcoded eat task type is stored.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  @Test
  public void shouldFoundHardcodedEatTaskType(final Vertx vertx, final VertxTestContext testContext) {

    testContext
        .assertComplete(
            WeNetTaskManager.createProxy(vertx).retrieveTaskType(WeNetTaskManager.HARDCODED_DINNER_TASK_TYPE_ID))
        .onSuccess(found -> testContext.verify(() -> {

          assertThat(found.norms).isNullOrEmpty();
          testContext.completeNow();

        }));
  }

  /**
   * Check that the question and answer task type is stored.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  @Test
  public void shouldFoundQuestionAndAnswerTaskType(final Vertx vertx, final VertxTestContext testContext) {

    testContext
        .assertComplete(
            WeNetTaskManager.createProxy(vertx).retrieveTaskType(WeNetTaskManager.QUESTION_AND_ANSWER_TASK_TYPE_ID))
        .onSuccess(found -> testContext.verify(() -> {

          assertThat(found.norms).isNullOrEmpty();
          testContext.completeNow();

        }));
  }

  /**
   * Check that the echo task type is stored.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   */
  @Test
  public void shouldFoundEchoTaskType(final Vertx vertx, final VertxTestContext testContext) {

    testContext.assertComplete(WeNetTaskManager.createProxy(vertx).retrieveTaskType(WeNetTaskManager.ECHO_TASK_TYPE_ID))
        .onSuccess(found -> testContext.verify(() -> {

          assertThat(found.norms).isNotEmpty();
          testContext.completeNow();

        }));
  }

}
