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

package eu.internetofus.wenet_task_manager.api.tasks;

import eu.internetofus.common.components.Model;
import eu.internetofus.common.components.ValidationErrorException;
import eu.internetofus.common.components.interaction_protocol_engine.WeNetInteractionProtocolEngine;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.components.service.App;
import eu.internetofus.common.components.service.Message;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskTransaction;
import eu.internetofus.common.vertx.ModelContext;
import eu.internetofus.common.vertx.ModelResources;
import eu.internetofus.common.vertx.Repository;
import eu.internetofus.common.vertx.ServiceContext;
import eu.internetofus.common.vertx.ServiceRequests;
import eu.internetofus.common.vertx.ServiceResponseHandlers;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import javax.ws.rs.core.Response.Status;
import org.tinylog.Logger;

/**
 * Resource that provide the methods for the {@link Tasks}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TasksResource implements Tasks {

  /**
   * The event bus that is using.
   */
  protected Vertx vertx;

  /**
   * The repository to manage the tasks.
   */
  protected TasksRepository repository;

  /**
   * The component that manage the profiles.
   */
  protected WeNetProfileManager profileManager;

  /**
   * The component that manage the interaction protocols.
   */
  protected WeNetInteractionProtocolEngine interactionProtocolEngine;

  /**
   * Create a new instance to provide the services of the {@link Tasks}.
   *
   * @param vertx where resource is defined.
   */
  public TasksResource(final Vertx vertx) {

    this.vertx = vertx;
    this.repository = TasksRepository.createProxy(vertx);
    this.profileManager = WeNetProfileManager.createProxy(vertx);
    this.interactionProtocolEngine = WeNetInteractionProtocolEngine.createProxy(this.vertx);
  }

  /**
   * Create the task context.
   *
   * @return the context of the {@link Task}.
   */
  protected ModelContext<Task, String> createTaskContext() {

    final var context = new ModelContext<Task, String>();
    context.name = "task";
    context.type = Task.class;
    return context;

  }

  /**
   * Create the task transaction context.
   *
   * @return the context of the {@link TaskTransaction}.
   */
  protected ModelContext<TaskTransaction, String> createTaskTransactionContext() {

    final var context = new ModelContext<TaskTransaction, String>();
    context.name = "taskTransaction";
    context.type = TaskTransaction.class;
    return context;

  }

  /**
   * Create the task transaction context.
   *
   * @return the context of the {@link Message}.
   */
  protected ModelContext<Message, Void> createMessageContext() {

    final var context = new ModelContext<Message, Void>();
    context.name = "message";
    context.type = Message.class;
    return context;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTask(final String taskId, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskContext();
    model.id = taskId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.retrieveModel(model, (id, hanlder) -> this.repository.searchTask(id).onComplete(hanlder), context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTask(final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    if (body.getString("communityId", null) == null) {

      App.getOrCreateDefaultCommunityFor(body.getString("appId"), this.vertx).onComplete(search -> {

        var community = search.result();
        if (community == null) {

          ServiceResponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task.communityId",
              "You must define the community where the task happens.");

        } else {

          body.put("communityId", community.id);
          this.createTask(body, request, resultHandler);
        }

      });

    } else {

      final var model = this.createTaskContext();
      final var context = new ServiceContext(request, resultHandler);
      ModelResources.createModelChain(this.vertx, body, model,
          (task, handler) -> this.repository.storeTask(task).onComplete(handler), context, () -> {

            ServiceResponseHandlers.responseWith(resultHandler, Status.CREATED, model.value);

            Logger.debug("Created task {}", model.value);
            this.interactionProtocolEngine.createdTask(model.value).onComplete(sent -> {

              if (sent.failed()) {

                final var cause = sent.cause();
                Logger.debug(cause,
                    "The interaction protocol engine does not accepted to process the creation of the task {}",
                    model.value);

              } else {

                Logger.debug("The interaction protocol engine accepted to process the creation of the task {}",
                    model.value);

              }
            });
          });
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateTask(final String taskId, final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskContext();
    model.id = taskId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.updateModel(this.vertx, body, model,
        (id, hanlder) -> this.repository.searchTask(id).onComplete(hanlder),
        (task, handler) -> this.repository.updateTask(task).onComplete(handler), context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mergeTask(final String taskId, final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskContext();
    model.id = taskId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.mergeModel(this.vertx, body, model,
        (id, hanlder) -> this.repository.searchTask(id).onComplete(hanlder),
        (task, handler) -> this.repository.updateTask(task).onComplete(handler), context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteTask(final String taskId, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskContext();
    model.id = taskId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.deleteModel(model, this.repository::deleteTask, context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doTaskTransaction(final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var taskTransaction = Model.fromJsonObject(body, TaskTransaction.class);
    if (taskTransaction == null) {

      Logger.debug("The {} is not a valid TaskTransaction.", body);
      ServiceResponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_transaction",
          "The task transaction is not right.");

    } else {

      taskTransaction.validate("bad_task_transaction", this.vertx).onComplete(validation -> {

        if (validation.failed()) {

          final var cause = validation.cause();
          Logger.debug(cause, "The {} is not valid.", taskTransaction);
          ServiceResponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          ServiceResponseHandlers.responseWith(resultHandler, Status.ACCEPTED, taskTransaction);
          this.interactionProtocolEngine.doTransaction(taskTransaction).onComplete(send -> {

            if (send.failed()) {

              final var cause = send.cause();
              Logger.trace(cause, "The interaction protocol engine does not accepted to do the transaction {}",
                  taskTransaction);

            } else {

              Logger.trace("The interaction protocol engine accepted to do the transaction {} ", taskTransaction);

            }

          });
        }

      });
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTasksPage(String appId, String requesterId, String taskTypeId, String goalName,
      String goalDescription, Long creationFrom, Long creationTo, Long updateFrom, Long updateTo, Boolean hasCloseTs,
      Long closeFrom, Long closeTo, String orderValue, int offset, int limit, ServiceRequest context,
      Handler<AsyncResult<ServiceResponse>> resultHandler) {

    var order = ServiceRequests.extractQueryArray(orderValue);
    final var query = TasksRepository.creteTasksPageQuery(appId, requesterId, taskTypeId, goalName, goalDescription,
        creationFrom, creationTo, updateFrom, updateTo, hasCloseTs, closeFrom, closeTo);

    try {

      final var sort = Repository.queryParamToSort(order, "bad_order", (value) -> {

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

      this.repository.retrieveTasksPageObject(query, sort, offset, limit, retrieve -> {

        if (retrieve.failed()) {

          final var cause = retrieve.cause();
          Logger.debug(cause, "GET /tasks with {} => Retrieve error", query);
          ServiceResponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          final var tasksPage = retrieve.result();
          Logger.debug("GET /tasks with {} => {}.", query, tasksPage);
          ServiceResponseHandlers.responseOk(resultHandler, tasksPage);
        }

      });

    } catch (final ValidationErrorException error) {

      Logger.debug(error, "GET /tasks with {} => Retrieve error", query);
      ServiceResponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, error);

    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addTransactionIntoTask(String taskId, JsonObject body, ServiceRequest request,
      Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var transactionModel = this.createTaskTransactionContext();
    final var context = new ServiceContext(request, resultHandler);

    ModelResources.toModel(body, transactionModel, context, () -> {

      if (transactionModel.source.taskId != null && !taskId.equals(transactionModel.source.taskId)) {

        Logger.debug("POST /tasks/{}/transactions with {} => BAD REQUEST", taskId, transactionModel.source);
        ServiceResponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_id",
            "The identifier of the transaction task not match the identifier of the task to add it");

      } else {

        transactionModel.source.taskId = taskId;
        ModelResources.validate(this.vertx, transactionModel, context, () -> {

          this.repository.addTransactionIntoTask(taskId, transactionModel.source).onComplete(added -> {

            if (added.failed()) {

              final var cause = added.cause();
              Logger.debug(cause, "POST /tasks/{}/transactions with {} => NOT FOUND", taskId, transactionModel.source);
              ServiceResponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task",
                  "Not found task to add the transaction");

            } else {

              var result = added.result();
              Logger.debug(" \"POST /tasks/{}/transactions with {} => CREATED {}.", taskId, transactionModel.source,
                  result);
              ServiceResponseHandlers.responseWith(resultHandler, Status.CREATED, result);

            }

          });

        });
      }

    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMessageIntoTransaction(String taskId, String taskTransactionId, JsonObject body,
      ServiceRequest request, Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var messageModel = this.createMessageContext();
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.toModel(body, messageModel, context, () -> {

      ModelResources.validate(this.vertx, messageModel, context, () -> {

        this.repository.addMessageIntoTransaction(taskId, taskTransactionId, messageModel.source).onComplete(added -> {

          if (added.failed()) {

            final var cause = added.cause();
            Logger.debug(cause, "POST /tasks/{}/transactions/{}/messages with {} => NOT FOUND", taskId,
                taskTransactionId, messageModel.source);
            ServiceResponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND,
                "undefined_task_or_transaction",
                "Not found task where is the transaction or not found the transaction in it to add the message");

          } else {

            var result = added.result();
            Logger.debug(" \"POST /tasks/{}/transactions/{}/messages with {} => CREATED {}.", taskId, taskTransactionId,
                messageModel.source, result);
            ServiceResponseHandlers.responseWith(resultHandler, Status.CREATED, result);

          }

        });

      });

    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskTransactionsPage(String appId, String requesterId, String taskTypeId, String goalName,
      String goalDescription, Long taskCreationFrom, Long taskCreationTo, Long taskUpdateFrom, Long taskUpdateTo,
      Boolean hasCloseTs, Long closeFrom, Long closeTo, String taskId, String label, String actioneerId,
      Long creationFrom, Long creationTo, Long updateFrom, Long updateTo, String order, int offset, int limit,
      ServiceRequest request, Handler<AsyncResult<ServiceResponse>> resultHandler) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskTransaction(String taskId, String transactionId, ServiceRequest request,
      Handler<AsyncResult<ServiceResponse>> resultHandler) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveMessagesPage(String appId, String requesterId, String taskTypeId, String goalName,
      String goalDescription, Long taskCreationFrom, Long taskCreationTo, Long taskUpdateFrom, Long taskUpdateTo,
      Boolean hasCloseTs, Long closeFrom, Long closeTo, String taskId, String transactionLabel, String actioneerId,
      Long transactionCreationFrom, Long transactionCreationTo, Long transactionUpdateFrom, Long transactionUpdateTo,
      String receiverId, String label, String order, int offset, int limit, ServiceRequest request,
      Handler<AsyncResult<ServiceResponse>> resultHandler) {
  }

}
