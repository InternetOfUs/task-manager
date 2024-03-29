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

import eu.internetofus.common.components.models.Task;
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
import java.util.UUID;

/**
 * Implementation of the {@link TasksRepository}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TasksRepositoryImpl extends Repository implements TasksRepository {

  /**
   * The name of the collection that contains the tasks.
   */
  public static final String TASKS_COLLECTION = "tasks";

  /**
   * Create a new service.
   *
   * @param vertx   event bus to use.
   * @param pool    to create the connections.
   * @param version of the schemas.
   */
  public TasksRepositoryImpl(final Vertx vertx, final MongoClient pool, final String version) {

    super(vertx, pool, version);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void searchTask(final String id, final Handler<AsyncResult<JsonObject>> searchHandler) {

    final var query = new JsonObject().put("_id", id);
    this.findOneDocument(TASKS_COLLECTION, query, null, found -> {
      final var _id = (String) found.remove("_id");
      return found.put("id", _id);
    }).onComplete(searchHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void storeTask(final JsonObject task, final Handler<AsyncResult<JsonObject>> storeHandler) {

    final var id = (String) task.remove("id");
    if (id != null) {

      task.put("_id", id);
    }
    this.storeOneDocument(TASKS_COLLECTION, task, stored -> {

      final var _id = (String) stored.remove("_id");
      return stored.put("id", _id);

    }).onComplete(storeHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateTask(final JsonObject task, final Handler<AsyncResult<Void>> updateHandler) {

    final var id = task.remove("id");
    final var query = new JsonObject().put("_id", id);
    this.updateOneDocument(TASKS_COLLECTION, query, task).onComplete(updateHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteTask(final String id, final Handler<AsyncResult<Void>> deleteHandler) {

    final var query = new JsonObject().put("_id", id);
    this.deleteOneDocument(TASKS_COLLECTION, query).onComplete(deleteHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTasksPage(final JsonObject query, final JsonObject order, final int offset, final int limit,
      final Handler<AsyncResult<JsonObject>> searchHandler) {

    final var options = new FindOptions();
    options.setSort(order);
    options.setSkip(offset);
    options.setLimit(limit);
    this.searchPageObject(TASKS_COLLECTION, query, options, "tasks", task -> task.put("id", task.remove("_id")))
        .onComplete(searchHandler);

  }

  /**
   * Migrate the collections to the current version.
   *
   * @return the future that will inform if the migration is a success or not.
   */
  public Future<Void> migrateDocumentsToCurrentVersions() {

    return this.migrateTaskTo_0_6_0()
        .compose(empty -> this.migrateSchemaVersionOnCollectionTo(this.schemaVersion, TASKS_COLLECTION));

  }

  /**
   * Migrate the data base Task to the new model of the task for the API {0.6.0}.
   *
   * @return the future with the update result.
   */
  protected Future<Void> migrateTaskTo_0_6_0() {

    final var query = this.createQueryToReturnDocumentsWithAVersionLessThan("0.6.0");
    final var update = new JsonObject().put("$set", new JsonObject().put(SCHEMA_VERSION, "0.6.0")).put("$rename",
        new JsonObject().put("startTs", "attributes.startTs").put("endTs", "attributes.endTs").put("deadlineTs",
            "attributes.deadlineTs"));

    return this.updateCollection(TASKS_COLLECTION, query, update).compose(empty -> this.migrateFixingCommunityId());

  }

  /**
   * Fix that all the tasks have a {@link Task#communityId} value.
   *
   * @return the future with the update result.
   */
  protected Future<Void> migrateFixingCommunityId() {

    final Promise<Void> promise = Promise.promise();
    final var notExists = new JsonObject().put("communityId", new JsonObject().put("$exists", false));
    final var notString = new JsonObject().put("communityId",
        new JsonObject().put("$not", new JsonObject().put("$type", "string")));
    final var query = new JsonObject().put("$or",
        new JsonArray().add(notExists).add(notString).add(new JsonObject().putNull("communityId")));
    final var batch = this.pool.findBatch(TASKS_COLLECTION, query);
    batch.handler(task -> {

    });
    batch.endHandler(end -> promise.complete());
    batch.exceptionHandler(cause -> promise.fail(cause));
    return promise.future();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addTransactionIntoTask(final String taskId, final JsonObject transaction,
      final Handler<AsyncResult<JsonObject>> handler) {

    final var tmpId = UUID.randomUUID().toString();
    final var now = TimeManager.now();
    transaction.put("id", tmpId).put("_creationTs", now).put("_lastUpdateTs", now);
    final var query = new JsonObject().put("_id", taskId);
    final var update = new JsonObject().put("$set", new JsonObject().put("_lastUpdateTs", now)).put("$push",
        new JsonObject().put("transactions", transaction));

    this.pool.findOneAndUpdate(TASKS_COLLECTION, query, update).compose(task -> {

      if (task == null) {

        return Future.failedFuture("Not found task");

      } else {

        final var transactions = task.getJsonArray("transactions", new JsonArray());
        final var transactionId = String.valueOf(transactions.size());
        transaction.put("id", transactionId);
        query.put("transactions", new JsonObject().put("$elemMatch", new JsonObject().put("id", tmpId)));
        final var update2 = new JsonObject().put("$set", new JsonObject().put("transactions.$.id", transactionId));
        return this.pool.findOneAndUpdate(TASKS_COLLECTION, query, update2).compose(task2 -> {

          if (task2 == null) {

            return Future.failedFuture("Not update task");

          } else {

            return Future.succeededFuture(transaction);

          }

        });

      }

    }).onComplete(handler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMessageIntoTransaction(final String taskId, final String taskTransactionId, final JsonObject message,
      final Handler<AsyncResult<JsonObject>> handler) {

    final var queryNull = new JsonObject().put("_id", taskId).put("transactions",
        new JsonObject().put("$elemMatch", new JsonObject().put("id", taskTransactionId).putNull("messages")));
    final var updateNull = new JsonObject().put("$set",
        new JsonObject().put("transactions.$.messages", new JsonArray()));
    this.pool.findOneAndUpdate(TASKS_COLLECTION, queryNull, updateNull).compose(task -> {

      final var query = new JsonObject().put("_id", taskId).put("transactions",
          new JsonObject().put("$elemMatch", new JsonObject().put("id", taskTransactionId)));
      final var now = TimeManager.now();
      final var update = new JsonObject().put("$push", new JsonObject().put("transactions.$.messages", message))
          .put("$set", new JsonObject().put("_lastUpdateTs", now).put("transactions.$._lastUpdateTs", now));
      return this.pool.findOneAndUpdate(TASKS_COLLECTION, query, update);

    }).compose(task -> {

      if (task == null) {

        return Future.failedFuture("Not found task or transaction");

      } else {

        return Future.succeededFuture(message);

      }

    }).onComplete(handler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskTransactionsPage(final JsonObject query, final JsonObject order, final int offset,
      final int limit, final Handler<AsyncResult<JsonObject>> searchHandler) {

    this.aggregatePageObject(TASKS_COLLECTION, query, order, offset, limit, "transactions").onComplete(searchHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveMessagesPage(final JsonObject query, final JsonObject order, final int offset, final int limit,
      final Handler<AsyncResult<JsonObject>> searchHandler) {

    this.aggregatePageObject(TASKS_COLLECTION, query, order, offset, limit, "transactions.messages")
        .onComplete(searchHandler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteAllTaskWithRequester(final String profileId, final Handler<AsyncResult<JsonArray>> deleteHanndler) {

    final var query = new JsonObject().put("requesterId", profileId);
    this.pool.find(TASKS_COLLECTION, query).transform(handler -> {

      if (handler.failed()) {

        return Future.failedFuture(handler.cause());

      } else {

        final var tasks = handler.result();
        final var ids = new JsonArray();
        for (final var task : tasks) {

          final var taskId = task.getString("_id");
          ids.add(taskId);
        }

        final var deleteQuery = new JsonObject().put("_id", new JsonObject().put("$in", ids));
        return this.pool.removeDocuments(TASKS_COLLECTION, deleteQuery).map(any -> ids);
      }

    }).onComplete(deleteHanndler);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteAllTransactionByActioneer(final String profileId, final Handler<AsyncResult<Void>> deleteHanndler) {

    final var query = new JsonObject().put("transactions.actioneerId", profileId);
    final var update = new JsonObject().put("$pull",
        new JsonObject().put("transactions", new JsonObject().put("actioneerId", profileId)));
    final var options = new UpdateOptions().setMulti(true);
    this.pool.updateCollectionWithOptions(TASKS_COLLECTION, query, update, options).map(result -> (Void) null)
        .onComplete(deleteHanndler);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteAllMessagesWithReceiver(final String profileId, final Handler<AsyncResult<Void>> deleteHanndler) {

    final var query = new JsonObject().put("transactions.messages.receiverId", profileId);
    final var update = new JsonObject().put("$pull",
        new JsonObject().put("transactions.$[].messages", new JsonObject().put("receiverId", profileId)));
    final var options = new UpdateOptions().setMulti(true);
    this.pool.updateCollectionWithOptions(TASKS_COLLECTION, query, update, options).map(result -> (Void) null)
        .onComplete(deleteHanndler);

  }

}
