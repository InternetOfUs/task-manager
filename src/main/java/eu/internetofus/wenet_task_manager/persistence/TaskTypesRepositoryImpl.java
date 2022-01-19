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

import eu.internetofus.common.model.TimeManager;
import eu.internetofus.common.vertx.Repository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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
   * The path to the file that contains an array of the default task types to
   */
  public static final String DEFAULT_TASK_TYPE_RESOURCE_PREFIX = "eu/internetofus/wenet_task_manager/persistence/DefaultTaskType_";

  /**
   * Create a new service.
   *
   * @param vertx   that contains the event bus to use.
   * @param pool    to create the connections.
   * @param version of the schemas.
   */
  public TaskTypesRepositoryImpl(final Vertx vertx, final MongoClient pool, final String version) {

    super(vertx, pool, version);

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
  public void retrieveTaskTypesPage(final JsonObject query, final JsonObject order, final int offset, final int limit,
      final Handler<AsyncResult<JsonObject>> searchHandler) {

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

    return this.migrateTaskTypeTo_0_6_0()
        .compose(empty -> this.migrateSchemaVersionOnCollectionTo(this.schemaVersion, TASK_TYPES_COLLECTION));

  }

  /**
   * Migrate the data base Task to the new model of the task for the API {0.6.0}.
   *
   * @return the future with the update result.
   */
  protected Future<Void> migrateTaskTypeTo_0_6_0() {

    final var notExists = new JsonObject().put(SCHEMA_VERSION, new JsonObject().put("$exists", false));
    final var notEq = new JsonObject().put(SCHEMA_VERSION, new JsonObject().put("$lt", "0.6.0"));
    final var query = new JsonObject().put("$or", new JsonArray().add(notExists).add(notEq));
    final Promise<Void> promise = Promise.promise();
    this.migrateTaskTypeTo_0_6_0(query, promise);
    return promise.future();

  }

  /**
   * Migrate the task types.
   *
   * @param query   to the task type.
   * @param promise to inform of the migration process.
   */
  private void migrateTaskTypeTo_0_6_0(final JsonObject query, final Promise<Void> promise) {

    this.pool.findOne(TASK_TYPES_COLLECTION, query, new JsonObject()).onComplete(search -> {

      if (search.failed()) {

        promise.fail(search.cause());

      } else {

        final var taskType = search.result();
        if (taskType == null) {

          promise.complete();

        } else {

          final var newTransactions = this.migrateTaskTypeTo_0_6_0(taskType);
          final var now = TimeManager.now();
          final var updateQuery = new JsonObject().put("$set", new JsonObject().put(SCHEMA_VERSION, "0.6.0")
              .put("_creationTs", now).put("_lastUpdateTs", now).put("transactions", newTransactions));

          final var options = new UpdateOptions().setMulti(false);
          this.pool.updateCollectionWithOptions(TASK_TYPES_COLLECTION,
              new JsonObject().put("_id", taskType.getString("_id")), updateQuery, options).onComplete(update -> {

                if (update.failed()) {

                  promise.fail(update.cause());

                } else {

                  this.migrateTaskTypeTo_0_6_0(query, promise);
                }
              });
        }
      }
    });

  }

  /**
   * Migrate a document.
   *
   * @param taskType to migrate.
   *
   * @return the migrated transactions.
   */
  private JsonObject migrateTaskTypeTo_0_6_0(final JsonObject taskType) {

    final var newTransactions = new JsonObject();
    var oldTransactions = new JsonArray();
    final var value = taskType.getValue("transactions");
    if (value instanceof JsonArray) {

      oldTransactions = (JsonArray) value;
    }
    final var maxTransactions = oldTransactions.size();
    for (var i = 0; i < maxTransactions; i++) {

      final var taskTransactionType = oldTransactions.getJsonObject(i);
      final var properties = new JsonObject();
      final var taskAttributeTypes = taskTransactionType.getJsonArray("attributes", new JsonArray());
      final var maxAttributes = taskAttributeTypes.size();
      for (var j = 0; j < maxAttributes; j++) {

        final var taskAttribute = taskAttributeTypes.getJsonObject(j);
        final var newAttribute = new JsonObject();
        properties.put(taskAttribute.getString("name"), newAttribute);
        final var description = taskAttribute.getString("description");
        if (description != null) {

          newAttribute.put("description", description);
        }

        final var type = taskAttribute.getString("type");
        if (type != null) {

          newAttribute.put("type", type);
        }

      }
      final var newTransaction = new JsonObject();
      final var description = taskTransactionType.getString("description");
      if (description != null) {

        newTransaction.put("description", description);
      }
      if (!properties.isEmpty()) {

        newTransaction.put("properties", properties);
      }
      newTransactions.put(taskTransactionType.getString("label"), newTransaction);

    }

    return newTransactions;

  }
}
