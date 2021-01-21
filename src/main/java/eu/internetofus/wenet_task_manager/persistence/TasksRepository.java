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
import eu.internetofus.common.components.ValidationErrorException;
import eu.internetofus.common.components.service.Message;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskTransaction;
import eu.internetofus.common.vertx.QueryBuilder;
import eu.internetofus.common.vertx.Repository;
import eu.internetofus.wenet_task_manager.api.messages.MessagesPage;
import eu.internetofus.wenet_task_manager.api.task_transactions.TaskTransactionsPage;
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
import java.util.List;
import javax.validation.constraints.NotNull;

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
   * @param task to store.
   *
   * @return the future stored task.
   */
  @GenIgnore
  default Future<Task> storeTask(@NotNull final Task task) {

    Promise<JsonObject> promise = Promise.promise();
    this.storeTask(task.toJsonObject(), promise);
    return Model.fromFutureJsonObject(promise.future(), Task.class);

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
   * @param task to update.
   *
   * @return the future update result.
   */
  @GenIgnore
  default Future<Void> updateTask(@NotNull final Task task) {

    final var object = task.toJsonObjectWithEmptyValues();
    if (object == null) {

      return Future.failedFuture("The task can not converted to JSON.");

    } else {

      Promise<Void> promise = Promise.promise();
      this.updateTask(object, promise);
      return promise.future();
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
   * @param id identifier of the task to delete.
   *
   * @return the future delete result.
   */
  @GenIgnore
  default Future<Void> deleteTask(String id) {

    Promise<Void> promise = Promise.promise();
    this.deleteTask(id, promise);
    return promise.future();

  }

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
  static JsonObject createTasksPageQuery(final String appId, final String requesterId, final String taskTypeId,
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
   * Create the sort query of the task.
   *
   * @param order to sort the tasks.
   *
   * @return the sort query of the tasks page.
   *
   * @throws ValidationErrorException If the values are not right.
   */
  static JsonObject createTasksPageSort(final List<String> order) throws ValidationErrorException {

    return Repository.queryParamToSort(order, "bad_order", (value) -> {

      switch (value) {
      case "goalName":
      case "goal.name":
        return "goal.name";
      case "goalDescription":
      case "goal.description":
        return "goal.description";
      case "updateTs":
      case "update":
      case "_updateTs":
      case "lastUpdateTs":
      case "lastUpdate":
      case "_lastUpdateTs":
        return "_lastUpdateTs";
      case "creationTs":
      case "creation":
      case "_creationTs":
        return "_creationTs";
      case "id":
      case "taskTypeId":
      case "requesterId":
      case "appId":
        return value;
      default:
        return null;
      }

    });

  }

  /**
   * Obtain the tasks that satisfies a query.
   *
   * @param query  that define the tasks to add into the page.
   * @param order  in witch has to return the tasks.
   * @param offset index of the first task to return.
   * @param limit  number maximum of tasks to return.
   *
   * @return the future found page.
   */
  @GenIgnore
  default Future<TasksPage> retrieveTasksPage(final JsonObject query, final JsonObject order, final int offset,
      final int limit) {

    Promise<JsonObject> promise = Promise.promise();
    this.retrieveTasksPage(query, order, offset, limit, promise);
    return Model.fromFutureJsonObject(promise.future(), TasksPage.class);

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
  void retrieveTasksPage(JsonObject query, JsonObject order, int offset, int limit,
      Handler<AsyncResult<JsonObject>> searchHandler);

  /**
   * Called to add a transaction into a task.
   *
   * @param taskId      identifier of the task to add the transaction.
   * @param transaction to add.
   *
   * @return the added task transaction.
   */
  @GenIgnore
  default Future<TaskTransaction> addTransactionIntoTask(String taskId, TaskTransaction transaction) {

    Promise<JsonObject> promise = Promise.promise();
    this.addTransactionIntoTask(taskId, transaction.toJsonObject(), promise);
    return Model.fromFutureJsonObject(promise.future(), TaskTransaction.class);

  }

  /**
   * Called to add a transaction into a task.
   *
   * @param taskId      identifier of the task to add the transaction.
   * @param transaction to add.
   * @param handler     to manage the add result.
   */
  void addTransactionIntoTask(String taskId, JsonObject transaction, Handler<AsyncResult<JsonObject>> handler);

  /**
   * Called to add a message into a transaction.
   *
   * @param taskId            identifier of the task where is the transaction.
   * @param taskTransactionId identifier of the transaction to add the message.
   * @param message           to add.
   *
   * @return the added task transaction.
   */
  @GenIgnore
  default Future<Message> addMessageIntoTransaction(String taskId, String taskTransactionId, Message message) {

    Promise<JsonObject> promise = Promise.promise();
    this.addMessageIntoTransaction(taskId, taskTransactionId, message.toJsonObject(), promise);
    return Model.fromFutureJsonObject(promise.future(), Message.class);

  }

  /**
   * Called to add a message into a transaction.
   *
   * @param taskId            identifier of the task where is the transaction.
   * @param taskTransactionId identifier of the transaction to add the message.
   * @param message           to add.
   * @param handler           to manage the add result.
   */
  void addMessageIntoTransaction(String taskId, String taskTransactionId, JsonObject message,
      Handler<AsyncResult<JsonObject>> handler);

  /**
   * Obtain the task transactions that satisfies a query.
   *
   * @param query  that define the task transactions to add into the page.
   * @param order  in witch has to return the task transactions.
   * @param offset index of the first task transaction to return.
   * @param limit  number maximum of task transactions to return.
   *
   * @return the future found page.
   */
  @GenIgnore
  default Future<TaskTransactionsPage> retrieveTaskTransactionsPage(final JsonObject query, final JsonObject order,
      final int offset, final int limit) {

    Promise<JsonObject> promise = Promise.promise();
    this.retrieveTaskTransactionsPage(query, order, offset, limit, promise);
    return Model.fromFutureJsonObject(promise.future(), TaskTransactionsPage.class);

  }

  /**
   * Search for the task transaction with the specified identifier.
   *
   * @param query         that define the task transactions to add into the page.
   * @param order         in witch has to return the task transactions.
   * @param offset        index of the first task transaction to return.
   * @param limit         number maximum of task transactions to return.
   * @param searchHandler handler to manage the search.
   */
  void retrieveTaskTransactionsPage(JsonObject query, JsonObject order, int offset, int limit,
      Handler<AsyncResult<JsonObject>> searchHandler);

  /**
   * Create the query to ask about some task transactions.
   *
   * @param appId            application identifier to match for the tasks where
   *                         are the transactions to return.
   * @param requesterId      requester identifier to match for the tasks where are
   *                         the transactions to return.
   * @param taskTypeId       task type identifier to match for the tasks where are
   *                         the transactions to return.
   * @param goalName         pattern to match with the goal name of the tasks
   *                         where are the transactions to return.
   * @param goalDescription  pattern to match with the goal description of the
   *                         tasks where are the transactions to return.
   * @param goalKeywords     patterns to match with the goal keywords of the tasks
   *                         where are the transactions to return.
   * @param taskCreationFrom minimal creation time stamp of the tasks where are
   *                         the transactions to return.
   * @param taskCreationTo   maximal creation time stamp of the tasks where are
   *                         the transactions to return.
   * @param taskUpdateFrom   minimal update time stamp of the tasks where are the
   *                         transactions to return.
   * @param taskUpdateTo     maximal update time stamp of the tasks where are the
   *                         transactions to return.
   * @param hasCloseTs       this is {@code true} if the tasks to return need to
   *                         have a {@link Task#closeTs}
   * @param closeFrom        minimal close time stamp of the tasks where are the
   *                         transactions to return.
   * @param closeTo          maximal close time stamp of the tasks where are the
   *                         transactions to return.
   * @param taskId           identifier of the task where are the transactions to
   *                         return.
   * @param id               identifier of the transactions to return.
   * @param label            of the transactions to return.
   * @param actioneerId      identifier of the user that done the transactions to
   *                         return.
   * @param creationFrom     minimal creation time stamp of the transactions to
   *                         return.
   * @param creationTo       maximal creation time stamp of the transactions to
   *                         return.
   * @param updateFrom       minimal update time stamp of the transactions to
   *                         return.
   * @param updateTo         maximal update time stamp of the transactions to
   *                         return.
   *
   * @return the query that you have to use to obtains some task transactions.
   */
  static JsonObject createTaskTransactionsPageQuery(String appId, String requesterId, String taskTypeId,
      String goalName, String goalDescription, List<String> goalKeywords, Long taskCreationFrom, Long taskCreationTo,
      Long taskUpdateFrom, Long taskUpdateTo, Boolean hasCloseTs, Long closeFrom, Long closeTo, String taskId,
      String id, String label, String actioneerId, Long creationFrom, Long creationTo, Long updateFrom, Long updateTo) {

    return new QueryBuilder().withEqOrRegex("_id", taskId).withEqOrRegex("appId", appId)
        .withEqOrRegex("requesterId", requesterId).withEqOrRegex("taskTypeId", taskTypeId)
        .withEqOrRegex("goal.name", goalName).withEqOrRegex("goal.description", goalDescription)
        .withEqOrRegex("goal.keywords", goalKeywords).withRange("_creationTs", taskCreationFrom, taskCreationTo)
        .withRange("_lastUpdateTs", taskUpdateFrom, taskUpdateTo).withExist("closeTs", hasCloseTs)
        .withRange("closeTs", closeFrom, closeTo).withEqOrRegex("transactions.id", id)
        .withEqOrRegex("transactions.label", label).withEqOrRegex("transactions.actioneerId", actioneerId)
        .withRange("transactions._creationTs", creationFrom, creationTo)
        .withRange("transactions._lastUpdateTs", updateFrom, updateTo).build();

  }

  /**
   * Create the sort query of the task transactions.
   *
   * @param order to sort the task transactions.
   *
   * @return the sort query of the task transactions page.
   *
   * @throws ValidationErrorException If the values are not right.
   */
  static JsonObject createTaskTransactionsPageSort(final List<String> order) throws ValidationErrorException {

    var sort = Repository.queryParamToSort(order, "bad_order", (value) -> {

      switch (value) {
      case "id":
        return "transactions.id";
      case "goalName":
      case "goal.name":
        return "goal.name";
      case "goalDescription":
      case "goal.description":
        return "goal.description";
      case "goalKeywords":
      case "goal.keywords":
        return "goal.keywords";
      case "taskTypeId":
      case "requesterId":
      case "appId":
      case "transactionsIndex":
        return value;
      case "taskUpdateTs":
      case "taskUpdate":
      case "task_updateTs":
      case "taskLastUpdateTs":
      case "taskLastUpdate":
      case "task_lastUpdateTs":
        return "_lastUpdateTs";
      case "taskCreationTs":
      case "taskCreation":
      case "task_creationTs":
        return "_creationTs";
      case "closeTs":
      case "close":
        return "closeTs";
      case "taskId":
        return "_id";
      case "label":
      case "actioneerId":
        return "transactions." + value;
      case "updateTs":
      case "update":
      case "_updateTs":
      case "lastUpdateTs":
      case "lastUpdate":
      case "_lastUpdateTs":
        return "transactions._lastUpdateTs";
      case "creationTs":
      case "creation":
      case "_creationTs":
        return "transactions._creationTs";
      default:
        return null;
      }

    });

    if (sort == null) {

      return new JsonObject().put("_creationTs", 1).put("_id", 1).put("transactionsIndex", 1);

    } else {

      return sort;
    }

  }

  /**
   * Obtain the messages that satisfies a query.
   *
   * @param query  that define the messages to add into the page.
   * @param order  in witch has to return the messages.
   * @param offset index of the first message to return.
   * @param limit  number maximum of messages to return.
   *
   * @return the future found page.
   */
  @GenIgnore
  default Future<MessagesPage> retrieveMessagesPage(final JsonObject query, final JsonObject order, final int offset,
      final int limit) {

    Promise<JsonObject> promise = Promise.promise();
    this.retrieveMessagesPage(query, order, offset, limit, promise);
    return Model.fromFutureJsonObject(promise.future(), MessagesPage.class);

  }

  /**
   * Search for the message with the specified identifier.
   *
   * @param query         that define the messages to add into the page.
   * @param order         in witch has to return the messages.
   * @param offset        index of the first message to return.
   * @param limit         number maximum of messages to return.
   * @param searchHandler handler to manage the search.
   */
  void retrieveMessagesPage(JsonObject query, JsonObject order, int offset, int limit,
      Handler<AsyncResult<JsonObject>> searchHandler);

  /**
   * Create the query to ask about some messages.
   *
   * @param appId                   application identifier to match for the tasks
   *                                where are the messages to return.
   * @param requesterId             requester identifier to match for the tasks
   *                                where are the messages to return.
   * @param taskTypeId              task type identifier to match for the tasks
   *                                where are the messages to return.
   * @param goalName                pattern to match with the goal name of the
   *                                tasks where are the messages to return.
   * @param goalDescription         pattern to match with the goal description of
   *                                the tasks where are the messages to return.
   * @param goalKeywords            patterns to match with the goal keywords of
   *                                the tasks where are the messages to return.
   * @param taskCreationFrom        minimal creation time stamp of the tasks where
   *                                are the messages to return.
   * @param taskCreationTo          maximal creation time stamp of the tasks where
   *                                are the messages to return.
   * @param taskUpdateFrom          minimal update time stamp of the tasks where
   *                                are the messages to return.
   * @param taskUpdateTo            maximal update time stamp of the tasks where
   *                                are the messages to return.
   * @param hasCloseTs              this is {@code true} if the tasks to return
   *                                need to have a {@link Task#closeTs}
   * @param closeFrom               minimal close time stamp of the tasks where
   *                                are the messages to return.
   * @param closeTo                 maximal close time stamp of the tasks where
   *                                are the messages to return.
   * @param taskId                  identifier of the task where are the messages
   *                                to return.
   * @param transactionLabel        label of the transaction where are the
   *                                messages to return.
   * @param transactionId           identifier of the transaction where are the
   *                                messages to return.
   * @param transactionActioneerId  identifier of the user that done the
   *                                transaction where are messages to return.
   * @param transactionCreationFrom minimal creation time stamp of the transaction
   *                                where are the messages to return.
   * @param transactionCreationTo   maximal creation time stamp of the transaction
   *                                where are the messages to return.
   * @param transactionUpdateFrom   minimal update time stamp of the transaction
   *                                where are the messages to return.
   * @param transactionUpdateTo     maximal update time stamp of the transaction
   *                                where are the messages to return.
   * @param receiverId              of the messages to return.
   * @param label                   of the messages to return.
   *
   * @return the query that you have to use to obtains some messages.
   */
  static JsonObject createMessagesPageQuery(String appId, String requesterId, String taskTypeId, String goalName,
      String goalDescription, List<String> goalKeywords, Long taskCreationFrom, Long taskCreationTo,
      Long taskUpdateFrom, Long taskUpdateTo, Boolean hasCloseTs, Long closeFrom, Long closeTo, String taskId,
      String transactionId, String transactionLabel, String transactionActioneerId, Long transactionCreationFrom,
      Long transactionCreationTo, Long transactionUpdateFrom, Long transactionUpdateTo, String receiverId,
      String label) {

    return new QueryBuilder().withEqOrRegex("_id", taskId).withEqOrRegex("transactions.id", transactionId)
        .withEqOrRegex("transactions.label", transactionLabel)
        .withEqOrRegex("transactions.actioneerId", transactionActioneerId)
        .withRange("transactions._creationTs", transactionCreationFrom, transactionCreationTo)
        .withRange("transactions._lastUpdateTs", transactionUpdateFrom, transactionUpdateTo)
        .withEqOrRegex("transactions.messages.receiverId", receiverId)
        .withEqOrRegex("transactions.messages.label", label).withEqOrRegex("appId", appId)
        .withEqOrRegex("requesterId", requesterId).withEqOrRegex("taskTypeId", taskTypeId)
        .withEqOrRegex("goal.name", goalName).withEqOrRegex("goal.description", goalDescription)
        .withEqOrRegex("goal.keywords", goalKeywords).withRange("_creationTs", taskCreationFrom, taskCreationTo)
        .withRange("_lastUpdateTs", taskUpdateFrom, taskUpdateTo).withExist("closeTs", hasCloseTs)
        .withRange("closeTs", closeFrom, closeTo).build();

  }

  /**
   * Create the sort query of the messages.
   *
   * @param order to sort the messages.
   *
   * @return the sort query of the messages page.
   *
   * @throws ValidationErrorException If the values are not right.
   */
  static JsonObject createMessagesPageSort(final List<String> order) throws ValidationErrorException {

    var sort = Repository.queryParamToSort(order, "bad_order", (value) -> {

      switch (value) {
      case "taskTypeId":
      case "requesterId":
      case "appId":
      case "transactionsIndex":
      case "messagesIndex":
        return value;
      case "goalName":
      case "goal.name":
        return "goal.name";
      case "goalDescription":
      case "goal.description":
        return "goal.description";
      case "goalKeywords":
      case "goal.keywords":
        return "goal.keywords";
      case "taskUpdateTs":
      case "taskUpdate":
      case "task_updateTs":
      case "taskLastUpdateTs":
      case "taskLastUpdate":
      case "task_lastUpdateTs":
        return "_lastUpdateTs";
      case "close":
      case "closeTs":
        return "closeTs";
      case "taskCreationTs":
      case "taskCreation":
      case "task_creationTs":
        return "_creationTs";
      case "taskId":
        return "transactions." + value;
      case "transactionActioneerId":
      case "actioneerId":
        return "transactions.actioneerId";
      case "transactionId":
        return "transactions.id";
      case "transactionLabel":
        return "transactions.label";
      case "transactionUpdateTs":
      case "transactionUpdate":
      case "transaction_updateTs":
      case "transactionLastUpdateTs":
      case "transactionLastUpdate":
      case "transaction_lastUpdateTs":
        return "transactions._lastUpdateTs";
      case "transactionCreationTs":
      case "transactionCreation":
      case "transaction_creationTs":
        return "transactions._creationTs";
      case "receiverId":
      case "label":
        return "transactions.messages." + value;
      default:
        return null;
      }

    });

    if (sort == null) {

      return new JsonObject().put("_creationTs", 1).put("_id", 1).put("transactionsIndex", 1).put("messagesIndex", 1);

    } else {

      return sort;
    }

  }
}
