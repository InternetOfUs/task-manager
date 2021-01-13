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

import eu.internetofus.common.TimeManager;
import eu.internetofus.common.vertx.Repository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

/**
 * Implementation of the {@link TaskTypesRepository}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TaskTypesRepositoryImpl extends Repository implements TaskTypesRepository {

  /**
   * The name of the collection that contains the taskTypes.
   */
  public static final String TASK_TYPES_COLLECTION = "taskTypes";

  /**
   * Create a new service.
   *
   * @param pool    to create the connections.
   * @param version of the schemas.
   */
  public TaskTypesRepositoryImpl(final MongoClient pool, final String version) {

    super(pool, version);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void searchTaskType(final String id, final Handler<AsyncResult<JsonObject>> searchHandler) {

    final var query = new JsonObject().put("_id", id);
    this.findOneDocument(TASK_TYPES_COLLECTION, query, null, found -> {
      final var _id = (String) found.remove("_id");
      return found.put("id", _id);
    }).onComplete(searchHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void storeTaskType(final JsonObject taskType, final Handler<AsyncResult<JsonObject>> storeHandler) {

    final var id = (String) taskType.remove("id");
    if (id != null) {

      taskType.put("_id", id);
    }
    this.storeOneDocument(TASK_TYPES_COLLECTION, taskType, stored -> {

      final var _id = (String) stored.remove("_id");
      return stored.put("id", _id);

    }).onComplete(storeHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateTaskType(final JsonObject taskType, final Handler<AsyncResult<Void>> updateHandler) {

    final var id = taskType.remove("id");
    final var query = new JsonObject().put("_id", id);
    this.updateOneDocument(TASK_TYPES_COLLECTION, query, taskType).onComplete(updateHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteTaskType(final String id, final Handler<AsyncResult<Void>> deleteHandler) {

    final var query = new JsonObject().put("_id", id);
    this.deleteOneDocument(TASK_TYPES_COLLECTION, query).onComplete(deleteHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskTypesPageObject(final JsonObject query, final JsonObject order, final int offset,
      final int limit, final Handler<AsyncResult<JsonObject>> searchHandler) {

    final var options = new FindOptions();
    options.setSort(order);
    options.setSkip(offset);
    options.setLimit(limit);
    this.searchPageObject(TASK_TYPES_COLLECTION, query, options, "taskTypes",
        taskType -> taskType.put("id", taskType.remove("_id"))).onComplete(searchHandler);

  }

  /**
   * Migrate the collections to the current version.
   *
   * @return the future that will inform if the migration is a success or not.
   */
  public Future<Void> migrateDocumentsToCurrentVersions() {

    return this.migrateTaskTo_0_6_0().compose(empty -> this.updateSchemaVersionOnCollection(TASK_TYPES_COLLECTION));

  }

  /**
   * Migrate the data base Task to the new model of the task for the API {0.6.0}.
   *
   * @return the future with the update result.
   */
  protected Future<Void> migrateTaskTo_0_6_0() {

    final var notExists = new JsonObject().put(SCHEMA_VERSION, new JsonObject().put("$exists", false));
    final var notEq = new JsonObject().put(SCHEMA_VERSION, new JsonObject().put("$lt", "0.6.0"));
    final var query = new JsonObject().put("$or", new JsonArray().add(notExists).add(notEq));
    Promise<Void> promise = Promise.promise();
    this.pool.findBatch(TASK_TYPES_COLLECTION, query).handler(taskType -> {

      var newTransactions = new JsonObject();
      var oldTransactions = taskType.getJsonArray("transactions", new JsonArray());
      var maxTransactions = oldTransactions.size();
      for (var i = 0; i < maxTransactions; i++) {

        var taskTransactionType = oldTransactions.getJsonObject(i);
        var properties = new JsonObject();
        var taskAttributeTypes = taskTransactionType.getJsonArray("attributes", new JsonArray());
        var maxAttributes = taskAttributeTypes.size();
        for (var j = 0; j < maxAttributes; j++) {

          var taskAttribute = taskAttributeTypes.getJsonObject(j);
          var newAttribute = new JsonObject();
          properties.put(taskAttribute.getString("name"), newAttribute);
          var description = taskAttribute.getString("description");
          if (description != null) {

            newAttribute.put("description", description);
          }

          var type = taskAttribute.getString("type");
          if (type != null) {

            newAttribute.put("type", type);
          }

        }
        var newTransaction = new JsonObject();
        var description = taskTransactionType.getString("description");
        if (description != null) {

          newTransaction.put("description", description);
        }
        if (!properties.isEmpty()) {

          newTransaction.put("properties", properties);
        }
        newTransactions.put(taskTransactionType.getString("label"), newTransaction);

      }

      var now = TimeManager.now();
      var updateQuery = new JsonObject().put("$set", new JsonObject().put(SCHEMA_VERSION, "0.6.0")
          .put("_creationTs", now).put("_lastUpdateTs", now).put("transactions", newTransactions));

      final var options = new UpdateOptions().setMulti(false);
      this.pool.updateCollectionWithOptions(TASK_TYPES_COLLECTION,
          new JsonObject().put("_id", taskType.getString("_id")), updateQuery, options)
          .onFailure(cause -> promise.tryFail(cause));

    }).exceptionHandler(error -> promise.tryFail(error)).endHandler(end -> promise.tryComplete());

    return promise.future();

  }

}
