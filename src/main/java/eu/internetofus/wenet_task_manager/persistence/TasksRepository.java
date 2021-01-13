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

import eu.internetofus.common.components.Model;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.vertx.QueryBuilder;
import eu.internetofus.wenet_task_manager.api.tasks.TasksPage;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * The service to manage the {@link Task} on the database.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ProxyGen
public interface TasksRepository {

  /**
   * The address of this service.
   */
  String ADDRESS = "wenet_task_manager.persistence.tasks";

  /**
   * Create a proxy of the {@link TasksRepository}.
   *
   * @param vertx where the service has to be used.
   *
   * @return the task.
   */
  static TasksRepository createProxy(final Vertx vertx) {

    return new TasksRepositoryVertxEBProxy(vertx, TasksRepository.ADDRESS);
  }

  /**
   * Register this service.
   *
   * @param vertx   that contains the event bus to use.
   * @param pool    to create the database connections.
   * @param version of the schemas.
   *
   * @return the future that inform when the repository will be registered or not.
   */
  static Future<Void> register(final Vertx vertx, final MongoClient pool, final String version) {

    final var repository = new TasksRepositoryImpl(pool, version);
    new ServiceBinder(vertx).setAddress(TasksRepository.ADDRESS).register(TasksRepository.class, repository);
    return repository.migrateDocumentsToCurrentVersions();

  }

  /**
   * Search for the task with the specified identifier.
   *
   * @param id identifier of the task to search.
   *
   * @return tyeh future found task.
   */
  @GenIgnore
  default Future<Task> searchTask(final String id) {

    Promise<JsonObject> promise = Promise.promise();
    this.searchTask(id, promise);
    return Model.fromFutureJsonObject(promise.future(), Task.class);

  }

  /**
   * Search for the task with the specified identifier.
   *
   * @param id            identifier of the task to search.
   * @param searchHandler handler to manage the search.
   */
  void searchTask(String id, Handler<AsyncResult<JsonObject>> searchHandler);

  /**
   * Store a task.
   *
   * @param task         to store.
   * @param storeHandler handler to manage the store.
   */
  @GenIgnore
  default void storeTask(final Task task, final Handler<AsyncResult<Task>> storeHandler) {

    final var object = task.toJsonObject();
    if (object == null) {

      storeHandler.handle(Future.failedFuture("The task can not converted to JSON."));

    } else {

      this.storeTask(object, stored -> {
        if (stored.failed()) {

          storeHandler.handle(Future.failedFuture(stored.cause()));

        } else {

          final var value = stored.result();
          final var storedTask = Model.fromJsonObject(value, Task.class);
          if (storedTask == null) {

            storeHandler.handle(Future.failedFuture("The stored task is not valid."));

          } else {

            storeHandler.handle(Future.succeededFuture(storedTask));
          }

        }
      });
    }
  }

  /**
   * Store a task.
   *
   * @param task         to store.
   * @param storeHandler handler to manage the search.
   */
  void storeTask(JsonObject task, Handler<AsyncResult<JsonObject>> storeHandler);

  /**
   * Update a task.
   *
   * @param task          to update.
   * @param updateHandler handler to manage the update.
   */
  @GenIgnore
  default void updateTask(final Task task, final Handler<AsyncResult<Void>> updateHandler) {

    final var object = task.toJsonObjectWithEmptyValues();
    if (object == null) {

      updateHandler.handle(Future.failedFuture("The task can not converted to JSON."));

    } else {

      this.updateTask(object, updateHandler);
    }

  }

  /**
   * Update a task.
   *
   * @param task          to update.
   * @param updateHandler handler to manage the update result.
   */
  void updateTask(JsonObject task, Handler<AsyncResult<Void>> updateHandler);

  /**
   * Delete a task.
   *
   * @param id            identifier of the task to delete.
   * @param deleteHandler handler to manage the delete result.
   */
  void deleteTask(String id, Handler<AsyncResult<Void>> deleteHandler);

  /**
   * Create the query to ask about some tasks.
   *
   * @param appId           the pattern to match the {@link Task#appId}.
   * @param requesterId     the pattern to match the {@link Task#requesterId}.
   * @param taskTypeId      the pattern to match the {@link Task#taskTypeId}.
   * @param goalName        the pattern to match the {@link Task#goal} name.
   * @param goalDescription the pattern to match the {@link Task#goal}
   *                        description.
   * @param creationFrom    the minimum time stamp, inclusive, for the
   *                        {@link Task#_creationTs}.
   * @param creationTo      the maximum time stamp, inclusive, for the
   *                        {@link Task#_creationTs}.
   * @param updateFrom      the minimum time stamp, inclusive, for the
   *                        {@link Task#_lastUpdateTs}.
   * @param updateTo        the maximum time stamp, inclusive, for the
   *                        {@link Task#_lastUpdateTs}.
   * @param hasCloseTs      is {@code true} is the {@link Task#closeTs} has to be
   *                        defined.
   * @param closeFrom       the minimum time stamp, inclusive, for the
   *                        {@link Task#closeTs}.
   * @param closeTo         the maximum time stamp, inclusive, for the
   *                        {@link Task#closeTs}.
   *
   * @return the query that you have to use to obtains some tasks.
   */
  static JsonObject creteTasksPageQuery(final String appId, final String requesterId, final String taskTypeId,
      final String goalName, final String goalDescription, final Number creationFrom, final Number creationTo,
      final Number updateFrom, final Number updateTo, final Boolean hasCloseTs, final Number closeFrom,
      final Number closeTo) {

    return new QueryBuilder().withEqOrRegex("appId", appId).withEqOrRegex("requesterId", requesterId)
        .withEqOrRegex("taskTypeId", taskTypeId).withEqOrRegex("goal.name", goalName)
        .withEqOrRegex("goal.description", goalDescription).withRange("_creationTs", creationFrom, creationTo)
        .withRange("_lastUpdateTs", updateFrom, updateTo).withExist("closeTs", hasCloseTs)
        .withRange("closeTs", closeFrom, closeTo).build();

  }

  /**
   * Obtain the tasks that satisfies a query.
   *
   * @param query         that define the tasks to add into the page.
   * @param order         in witch has to return the tasks.
   * @param offset        index of the first task to return.
   * @param limit         number maximum of tasks to return.
   * @param searchHandler handler to manage the search.
   */
  @GenIgnore
  default void retrieveTasksPage(final JsonObject query, final JsonObject order, final int offset, final int limit,
      final Handler<AsyncResult<TasksPage>> searchHandler) {

    this.retrieveTasksPageObject(query, order, offset, limit, search -> {

      if (search.failed()) {

        searchHandler.handle(Future.failedFuture(search.cause()));

      } else {

        final var value = search.result();
        final var page = Model.fromJsonObject(value, TasksPage.class);
        if (page == null) {

          searchHandler.handle(Future.failedFuture("The stored task page is not valid."));

        } else {

          searchHandler.handle(Future.succeededFuture(page));
        }
      }
    });
  }

  /**
   * Search for the task with the specified identifier.
   *
   * @param query         that define the tasks to add into the page.
   * @param order         in witch has to return the tasks.
   * @param offset        index of the first task to return.
   * @param limit         number maximum of tasks to return.
   * @param searchHandler handler to manage the search.
   */
  void retrieveTasksPageObject(JsonObject query, JsonObject order, int offset, int limit,
      Handler<AsyncResult<JsonObject>> searchHandler);

}
