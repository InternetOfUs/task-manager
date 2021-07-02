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

package eu.internetofus.wenet_task_manager.api.tasks;

import eu.internetofus.common.components.interaction_protocol_engine.WeNetInteractionProtocolEngine;
import eu.internetofus.common.components.models.Message;
import eu.internetofus.common.components.models.Task;
import eu.internetofus.common.components.models.TaskTransaction;
import eu.internetofus.common.components.service.App;
import eu.internetofus.common.model.Model;
import eu.internetofus.common.model.ValidationErrorException;
import eu.internetofus.common.vertx.ModelContext;
import eu.internetofus.common.vertx.ModelResources;
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
   * Create a new instance to provide the services of the {@link Tasks}.
   *
   * @param vertx where resource is defined.
   */
  public TasksResource(final Vertx vertx) {

    this.vertx = vertx;
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
    ModelResources.retrieveModel(model,
        (id, hanlder) -> TasksRepository.createProxy(this.vertx).searchTask(id).onComplete(hanlder), context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTask(final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    if (body.getString("communityId", null) == null) {

      App.getOrCreateDefaultCommunityFor(body.getString("appId"), this.vertx).onComplete(search -> {

        final var community = search.result();
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
          (task, handler) -> TasksRepository.createProxy(this.vertx).storeTask(task).onComplete(handler), context,
          () -> {

            ServiceResponseHandlers.responseWith(resultHandler, Status.CREATED, model.value);

            Logger.debug("Created task {}", model.value);
            WeNetInteractionProtocolEngine.createProxy(this.vertx).createdTask(model.value).onComplete(sent -> {

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
        (id, hanlder) -> TasksRepository.createProxy(this.vertx).searchTask(id).onComplete(hanlder),
        (task, handler) -> TasksRepository.createProxy(this.vertx).updateTask(task).onComplete(handler), context);

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
        (id, hanlder) -> TasksRepository.createProxy(this.vertx).searchTask(id).onComplete(hanlder),
        (task, handler) -> TasksRepository.createProxy(this.vertx).updateTask(task).onComplete(handler), context);

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
    ModelResources.deleteModel(model, TasksRepository.createProxy(this.vertx)::deleteTask, context);

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
          WeNetInteractionProtocolEngine.createProxy(this.vertx).doTransaction(taskTransaction).onComplete(send -> {

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
  public void retrieveTasksPage(final String appId, final String requesterId, final String taskTypeId,
      final String goalName, final String goalDescription, final Long creationFrom, final Long creationTo,
      final Long updateFrom, final Long updateTo, final Boolean hasCloseTs, final Long closeFrom, final Long closeTo,
      final String orderValue, final int offset, final int limit, final ServiceRequest context,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var order = ServiceRequests.extractQueryArray(orderValue);
    final var query = TasksRepository.createTasksPageQuery(appId, requesterId, taskTypeId, goalName, goalDescription,
        creationFrom, creationTo, updateFrom, updateTo, hasCloseTs, closeFrom, closeTo);

    try {

      final var sort = TasksRepository.createTasksPageSort(order);
      TasksRepository.createProxy(this.vertx).retrieveTasksPage(query, sort, offset, limit, retrieve -> {

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
  public void addTransactionIntoTask(final String taskId, final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

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

          TasksRepository.createProxy(this.vertx).addTransactionIntoTask(taskId, transactionModel.source)
              .onComplete(added -> {

                if (added.failed()) {

                  final var cause = added.cause();
                  Logger.debug(cause, "POST /tasks/{}/transactions with {} => NOT FOUND", taskId,
                      transactionModel.source);
                  ServiceResponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task",
                      "Not found task to add the transaction");

                } else {

                  final var result = added.result();
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
  public void addMessageIntoTransaction(final String taskId, final String taskTransactionId, final JsonObject body,
      final ServiceRequest request, final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var messageModel = this.createMessageContext();
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.toModel(body, messageModel, context, () -> {

      ModelResources.validate(this.vertx, messageModel, context, () -> {

        TasksRepository.createProxy(this.vertx)
            .addMessageIntoTransaction(taskId, taskTransactionId, messageModel.source).onComplete(added -> {

              if (added.failed()) {

                final var cause = added.cause();
                Logger.debug(cause, "POST /tasks/{}/transactions/{}/messages with {} => NOT FOUND", taskId,
                    taskTransactionId, messageModel.source);
                ServiceResponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND,
                    "undefined_task_or_transaction",
                    "Not found task where is the transaction or not found the transaction in it to add the message");

              } else {

                final var result = added.result();
                Logger.debug(" \"POST /tasks/{}/transactions/{}/messages with {} => CREATED {}.", taskId,
                    taskTransactionId, messageModel.source, result);
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
  public void retrieveTaskTransaction(final String taskId, final String transactionId, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskContext();
    model.id = taskId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.retrieveModelChain(model,
        (id, hanlder) -> TasksRepository.createProxy(this.vertx).searchTask(id).onComplete(hanlder), context, () -> {

          if (model.target.transactions != null) {

            for (final var transaction : model.target.transactions) {

              if (transaction.id.equals(transactionId)) {

                Logger.trace("Retrieve transaction {}.\n{}", transaction, context);
                ServiceResponseHandlers.responseOk(context.resultHandler, transaction);
                return;
              }

            }

          }

          // Not found
          Logger.trace("Not found transaction {} on {}.\n{}", transactionId, taskId, context);
          ServiceResponseHandlers.responseWithErrorMessage(context.resultHandler, Status.NOT_FOUND,
              "not_found_task_transaction",
              "On the task '" + taskId + "' not found a transaction with the identifier '" + transactionId + "'.");

        });

  }

}
