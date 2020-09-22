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

import java.util.List;

import eu.internetofus.common.components.Model;
import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.vertx.QueryBuilder;
import eu.internetofus.wenet_task_manager.api.tasks.TaskTypesPage;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * The service to manage the {@link TaskType} on the database.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ProxyGen
public interface TaskTypesRepository {

  /**
   * The address of this service.
   */
  String ADDRESS = "wenet_task_manager.persistence.taskTypes";

  /**
   * Create a proxy of the {@link TaskTypesRepository}.
   *
   * @param vertx where the service has to be used.
   *
   * @return the taskType.
   */
  static TaskTypesRepository createProxy(final Vertx vertx) {

    return new TaskTypesRepositoryVertxEBProxy(vertx, TaskTypesRepository.ADDRESS);
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

    final var repository = new TaskTypesRepositoryImpl(pool, version);
    new ServiceBinder(vertx).setAddress(TaskTypesRepository.ADDRESS).register(TaskTypesRepository.class, repository);
    return repository.migrateDocumentsToCurrentVersions();

  }

  /**
   * Search for the task type with the specified identifier.
   *
   * @param id            identifier of the task type to search.
   * @param searchHandler handler to manage the search.
   */
  @GenIgnore
  default void searchTaskType(final String id, final Handler<AsyncResult<TaskType>> searchHandler) {

    this.searchTaskTypeObject(id, search -> {

      if (search.failed()) {

        searchHandler.handle(Future.failedFuture(search.cause()));

      } else {

        final var value = search.result();
        final var taskType = Model.fromJsonObject(value, TaskType.class);
        if (taskType == null) {

          searchHandler.handle(Future.failedFuture("The stored taskType is not valid."));

        } else {

          searchHandler.handle(Future.succeededFuture(taskType));
        }
      }
    });
  }

  /**
   * Search for the task type with the specified identifier.
   *
   * @param id            identifier of the task type to search.
   * @param searchHandler handler to manage the search.
   */
  void searchTaskTypeObject(String id, Handler<AsyncResult<JsonObject>> searchHandler);

  /**
   * Store a task type.
   *
   * @param taskType     to store.
   * @param storeHandler handler to manage the store.
   */
  @GenIgnore
  default void storeTaskType(final TaskType taskType, final Handler<AsyncResult<TaskType>> storeHandler) {

    final var object = taskType.toJsonObject();
    if (object == null) {

      storeHandler.handle(Future.failedFuture("The taskType can not converted to JSON."));

    } else {

      this.storeTaskType(object, stored -> {
        if (stored.failed()) {

          storeHandler.handle(Future.failedFuture(stored.cause()));

        } else {

          final var value = stored.result();
          final var storedTaskType = Model.fromJsonObject(value, TaskType.class);
          if (storedTaskType == null) {

            storeHandler.handle(Future.failedFuture("The stored taskType is not valid."));

          } else {

            storeHandler.handle(Future.succeededFuture(storedTaskType));
          }

        }
      });
    }
  }

  /**
   * Store a task type.
   *
   * @param taskType     to store.
   * @param storeHandler handler to manage the search.
   */
  void storeTaskType(JsonObject taskType, Handler<AsyncResult<JsonObject>> storeHandler);

  /**
   * Update a task type.
   *
   * @param taskType      to update.
   * @param updateHandler handler to manage the update.
   */
  @GenIgnore
  default void updateTaskType(final TaskType taskType, final Handler<AsyncResult<Void>> updateHandler) {

    final var object = taskType.toJsonObject();
    if (object == null) {

      updateHandler.handle(Future.failedFuture("The taskType can not converted to JSON."));

    } else {

      this.updateTaskType(object, updateHandler);
    }

  }

  /**
   * Update a task type.
   *
   * @param taskType      to update.
   * @param updateHandler handler to manage the update result.
   */
  void updateTaskType(JsonObject taskType, Handler<AsyncResult<Void>> updateHandler);

  /**
   * Delete a task type.
   *
   * @param id            identifier of the task type to delete.
   * @param deleteHandler handler to manage the delete result.
   */
  void deleteTaskType(String id, Handler<AsyncResult<Void>> deleteHandler);

  /**
   * Create the query to askType about some taskTypes.
   *
   * @param name        the pattern to match the {@link TaskType#name}.
   * @param description the pattern to match the {@link TaskType#description}.
   * @param keywords    the pattern to match the {@link TaskType#keywords}.
   *
   * @return the query that you have to use to obtains some taskTypes.
   */
  static JsonObject creteTaskTypesPageQuery(final String name, final String description, final List<String> keywords) {

    return new QueryBuilder().withEqOrRegex("name", name).withEqOrRegex("description", description).withEqOrRegex("keywords", keywords).build();

  }

  /**
   * Obtain the taskTypes that satisfies a query.
   *
   * @param query         that define the taskTypes to add into the page.
   * @param order         in witch has to return the taskTypes.
   * @param offset        index of the first taskType to return.
   * @param limit         number maximum of taskTypes to return.
   * @param searchHandler handler to manage the search.
   */
  @GenIgnore
  default void retrieveTaskTypesPage(final JsonObject query, final JsonObject order, final int offset, final int limit, final Handler<AsyncResult<TaskTypesPage>> searchHandler) {

    this.retrieveTaskTypesPageObject(query, order, offset, limit, search -> {

      if (search.failed()) {

        searchHandler.handle(Future.failedFuture(search.cause()));

      } else {

        final var value = search.result();
        final var page = Model.fromJsonObject(value, TaskTypesPage.class);
        if (page == null) {

          searchHandler.handle(Future.failedFuture("The stored taskType page is not valid."));

        } else {

          searchHandler.handle(Future.succeededFuture(page));
        }
      }
    });
  }

  /**
   * Search for the taskType with the specified identifier.
   *
   * @param query         that define the taskTypes to add into the page.
   * @param order         in witch has to return the taskTypes.
   * @param offset        index of the first taskType to return.
   * @param limit         number maximum of taskTypes to return.
   * @param searchHandler handler to manage the search.
   */
  void retrieveTaskTypesPageObject(JsonObject query, JsonObject order, int offset, int limit, Handler<AsyncResult<JsonObject>> searchHandler);

}
