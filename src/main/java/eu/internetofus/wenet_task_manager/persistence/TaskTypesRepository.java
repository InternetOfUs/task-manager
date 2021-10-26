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

import eu.internetofus.common.model.Model;
import eu.internetofus.common.model.ValidationErrorException;
import eu.internetofus.common.components.models.TaskType;
import eu.internetofus.common.components.task_manager.TaskTypesPage;
import eu.internetofus.common.vertx.ModelsPageContext;
import eu.internetofus.common.vertx.QueryBuilder;
import eu.internetofus.common.vertx.Repository;
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
import java.util.List;

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

    final var repository = new TaskTypesRepositoryImpl(vertx, pool, version);
    new ServiceBinder(vertx).setAddress(TaskTypesRepository.ADDRESS).register(TaskTypesRepository.class, repository);
    return repository.migrateDocumentsToCurrentVersions();

  }

  /**
   * Search for the task type with the specified identifier.
   *
   * @param id identifier of the task type to search.
   *
   * @return the future found task type.
   */
  @GenIgnore
  default Future<TaskType> searchTaskType(final String id) {

    Promise<JsonObject> promise = Promise.promise();
    this.searchTaskType(id, promise);
    return Model.fromFutureJsonObject(promise.future(), TaskType.class);

  }

  /**
   * Search for the task type with the specified identifier.
   *
   * @param id            identifier of the task type to search.
   * @param searchHandler handler to manage the search.
   */
  void searchTaskType(String id, Handler<AsyncResult<JsonObject>> searchHandler);

  /**
   * Store a task type.
   *
   * @param taskType to store.
   *
   * @return the future stored task type.
   */
  @GenIgnore
  default Future<TaskType> storeTaskType(final TaskType taskType) {

    final var object = taskType.toJsonObject();
    if (object == null) {

      return Future.failedFuture("The task type can not converted to JSON.");

    } else {

      Promise<JsonObject> promise = Promise.promise();
      this.storeTaskType(object, promise);
      return Model.fromFutureJsonObject(promise.future(), TaskType.class);

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
   * @param taskType to update.
   *
   * @return the future update result.
   */
  @GenIgnore
  default Future<Void> updateTaskType(final TaskType taskType) {

    final var object = taskType.toJsonObjectWithEmptyValues();
    if (object == null) {

      return Future.failedFuture("The taskType can not converted to JSON.");

    } else {

      Promise<Void> promise = Promise.promise();
      this.updateTaskType(object, promise);
      return promise.future();
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
   * @param id identifier of the task type to delete.
   *
   * @return the future delete result.
   */
  @GenIgnore
  default Future<Void> deleteTaskType(String id) {

    Promise<Void> promise = Promise.promise();
    this.deleteTaskType(id, promise);
    return promise.future();

  }

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
  static JsonObject createTaskTypesPageQuery(final String name, final String description, final List<String> keywords) {

    return new QueryBuilder().withEqOrRegex("name", name).withEqOrRegex("description", description)
        .withEqOrRegex("keywords", keywords).build();

  }

  /**
   * Create the sort query of the task type.
   *
   * @param order to sort the task types.
   *
   * @return the sort query of the task types page.
   *
   * @throws ValidationErrorException If the values are not right.
   */
  static JsonObject createTaskTypesPageSort(final List<String> order) throws ValidationErrorException {

    return Repository.queryParamToSort(order, "bad_order", (value) -> {

      switch (value) {
      case "name":
      case "description":
      case "keywords":
        return value;
      default:
        return null;
      }

    });

  }

  /**
   * Retrieve the task types defined on the context.
   *
   * @param context that describe witch page want to obtain.
   *
   * @return the future page.
   */
  @GenIgnore
  default Future<TaskTypesPage> retrieveTaskTypesPage(final ModelsPageContext context) {

    return this.retrieveTaskTypesPage(context.query, context.sort, context.offset, context.limit);

  }

  /**
   * Retrieve the task types defined on the context.
   *
   * @param query  to obtain the required task types.
   * @param sort   describe how has to be ordered the obtained task types.
   * @param offset the index of the first community to return.
   * @param limit  the number maximum of task types to return.
   *
   * @return the future page.
   */
  @GenIgnore
  default Future<TaskTypesPage> retrieveTaskTypesPage(final JsonObject query, final JsonObject sort, final int offset,
      final int limit) {

    Promise<JsonObject> promise = Promise.promise();
    this.retrieveTaskTypesPage(query, sort, offset, limit, promise);
    return Model.fromFutureJsonObject(promise.future(), TaskTypesPage.class);

  }

  /**
   * Retrieve a page with some task types.
   *
   * @param query   to obtain the required task types.
   * @param sort    describe how has to be ordered the obtained task types.
   * @param offset  the index of the first community to return.
   * @param limit   the number maximum of task types to return.
   * @param handler to inform of the found task types.
   */
  void retrieveTaskTypesPage(JsonObject query, JsonObject sort, int offset, int limit,
      Handler<AsyncResult<JsonObject>> handler);

}
