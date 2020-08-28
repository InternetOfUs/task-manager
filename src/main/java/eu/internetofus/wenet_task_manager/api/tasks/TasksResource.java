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
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
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

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.tinylog.Logger;

import eu.internetofus.common.components.Model;
import eu.internetofus.common.components.ValidationErrorException;
import eu.internetofus.common.components.interaction_protocol_engine.Message;
import eu.internetofus.common.components.interaction_protocol_engine.Message.Type;
import eu.internetofus.common.components.interaction_protocol_engine.WeNetInteractionProtocolEngine;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskTransaction;
import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.vertx.OperationReponseHandlers;
import eu.internetofus.common.vertx.Repository;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

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
   * The repository to manage the task types.
   */
  protected TaskTypesRepository typesRepository;

  /**
   * The component that manage the profiles.
   */
  protected WeNetProfileManager profileManager;

  /**
   * The component that manage the interaction protocols.
   */
  protected WeNetInteractionProtocolEngine interactionProtocolEngine;

  /**
   * Create an empty resource. This is only used for unit tests.
   */
  protected TasksResource() {

  }

  /**
   * Create a new instance to provide the services of the {@link Tasks}.
   *
   * @param vertx where resource is defined.
   */
  public TasksResource(final Vertx vertx) {

    this.vertx = vertx;
    this.repository = TasksRepository.createProxy(vertx);
    this.typesRepository = TaskTypesRepository.createProxy(vertx);
    this.profileManager = WeNetProfileManager.createProxy(vertx);
    this.interactionProtocolEngine = WeNetInteractionProtocolEngine.createProxy(this.vertx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTask(final String taskId, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    this.repository.searchTaskObject(taskId, search -> {

      final var task = search.result();
      if (task == null) {

        Logger.debug(search.cause(), "Not found task for {}", taskId);
        OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task", "Does not exist a task associated to '" + taskId + "'.");

      } else {

        OperationReponseHandlers.responseOk(resultHandler, task);

      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTask(final JsonObject body, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var task = Model.fromJsonObject(body, Task.class);
    if (task == null) {

      Logger.trace("The {} is not a valid Task.", body);
      OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task", "The task is not right.");

    } else {

      task.validate("bad_task", this.vertx).onComplete(validation -> {

        if (validation.failed()) {

          final var cause = validation.cause();
          Logger.debug(cause, "The {} is not valid.", task);
          OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          this.repository.storeTask(task, stored -> {

            if (stored.failed()) {

              final var cause = validation.cause();
              Logger.debug(cause, "Cannot store {}.", task);
              OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              final var storedTask = stored.result();
              OperationReponseHandlers.responseOk(resultHandler, storedTask);

              Logger.debug("Created task {}", storedTask);
              final var message = new Message();
              message.taskId = storedTask.id;
              message.appId = storedTask.appId;
              message.type = Type.TASK_CREATED;
              message.content = storedTask.toJsonObject();
              this.interactionProtocolEngine.sendMessage(message.toJsonObject(), sent -> {

                if (sent.failed()) {

                  final var cause = validation.cause();
                  Logger.debug(cause, "Cannot send message {}.", message);
                  OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

                } else {

                  Logger.debug("Interaction protocol engine accepted {}", storedTask);

                }
              });
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
  public void updateTask(final String taskId, final JsonObject body, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var source = Model.fromJsonObject(body, Task.class);
    if (source == null) {

      Logger.debug("The {} is not a valid Task to update.", body);
      OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_to_update", "The task to update is not right.");

    } else {

      this.repository.searchTask(taskId, search -> {

        final var target = search.result();
        if (target == null) {

          Logger.debug(search.cause(), "Not found task {} to update", taskId);
          OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task_to_update", "You can not update the task '" + taskId + "', because it does not exist.");

        } else {

          source.id = null;
          source.validate("bad_new_profile", this.vertx).onComplete(validate -> {

            if (validate.failed()) {

              final var cause = validate.cause();
              Logger.debug(cause, "Cannot update {} with {}.", target, source);
              OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              source.id = target.id;
              source._creationTs = target._creationTs;
              source._lastUpdateTs = target._lastUpdateTs;
              if (source.equals(target)) {

                OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "task_to_update_equal_to_original",
                    "You can not update the task '" + source.id + "', because the new values is equals to the current one.");

              } else {

                this.repository.updateTask(source, update -> {

                  if (update.failed()) {

                    final var cause = update.cause();
                    Logger.debug(cause, "Cannot update {}.", target);
                    OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

                  } else {

                    OperationReponseHandlers.responseOk(resultHandler, source);

                  }

                });
              }
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
  public void deleteTask(final String taskId, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    this.repository.deleteTask(taskId, delete -> {

      if (delete.failed()) {

        final var cause = delete.cause();
        Logger.debug(cause, "Cannot delete the task  {}.", taskId);
        OperationReponseHandlers.responseFailedWith(resultHandler, Status.NOT_FOUND, cause);

      } else {

        OperationReponseHandlers.responseOk(resultHandler);
      }

    });

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskType(final String taskTypeId, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    this.typesRepository.searchTaskTypeObject(taskTypeId, search -> {

      final var taskType = search.result();
      if (taskType == null) {

        Logger.debug(search.cause(), "Not found task type type for {}", taskTypeId);
        OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task_type", "Does not exist a task type type associated to '" + taskTypeId + "'.");

      } else {

        OperationReponseHandlers.responseOk(resultHandler, taskType);

      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTaskType(final JsonObject body, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var taskType = Model.fromJsonObject(body, TaskType.class);
    if (taskType == null) {

      Logger.debug("The {} is not a valid TaskType.", body);
      OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_type", "The task type is not right.");

    } else {

      taskType.validate("bad_task_type", this.vertx).onComplete(validation -> {

        if (validation.failed()) {

          final var cause = validation.cause();
          Logger.debug(cause, "The {} is not valid.", taskType);
          OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          this.typesRepository.storeTaskType(taskType, stored -> {

            if (stored.failed()) {

              final var cause = validation.cause();
              Logger.debug(cause, "Cannot store {}.", taskType);
              OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              OperationReponseHandlers.responseOk(resultHandler, stored.result());
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
  public void updateTaskType(final String taskTypeId, final JsonObject body, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var source = Model.fromJsonObject(body, TaskType.class);
    if (source == null) {

      Logger.debug("The {} is not a valid TaskType to update.", body);
      OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_type_to_update", "The task type to update is not right.");

    } else {

      this.typesRepository.searchTaskType(taskTypeId, search -> {

        final var target = search.result();
        if (target == null) {

          Logger.debug(search.cause(), "Not found task type {} to update", taskTypeId);
          OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task_type_to_update", "You can not update the task type '" + taskTypeId + "', because it does not exist.");

        } else {

          source.id = null;
          source.validate("bad_new_task_type", this.vertx).onComplete(validate -> {

            if (validate.failed()) {

              final var cause = validate.cause();
              Logger.debug(cause, "Cannot update {} with {}.", target, source);
              OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              source.id = target.id;
              if (source.equals(target)) {

                OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "task_type_to_update_equal_to_original",
                    "You can not update the task type of the user '" + source.id + "', because the new values is equals to the current one.");

              } else {

                this.typesRepository.updateTaskType(source, update -> {

                  if (update.failed()) {

                    final var cause = update.cause();
                    Logger.debug(cause, "Cannot update {}.", target);
                    OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

                  } else {

                    OperationReponseHandlers.responseOk(resultHandler, source);

                  }

                });
              }
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
  public void deleteTaskType(final String taskTypeId, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    this.typesRepository.deleteTaskType(taskTypeId, delete -> {

      if (delete.failed()) {

        final var cause = delete.cause();
        Logger.debug(cause, "Cannot delete the task type {}.", taskTypeId);
        OperationReponseHandlers.responseFailedWith(resultHandler, Status.NOT_FOUND, cause);

      } else {

        OperationReponseHandlers.responseOk(resultHandler);
      }

    });

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doTaskTransaction(final JsonObject body, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var taskTransaction = Model.fromJsonObject(body, TaskTransaction.class);
    if (taskTransaction == null) {

      Logger.debug("The {} is not a valid TaskTransaction.", body);
      OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_transaction", "The task transaction is not right.");

    } else {

      taskTransaction.validate("bad_task_transaction", this.vertx).onComplete(validation -> {

        if (validation.failed()) {

          final var cause = validation.cause();
          Logger.debug(cause, "The {} is not valid.", taskTransaction);
          OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          final var message = new Message();
          message.taskId = taskTransaction.taskId;
          message.type = Type.TASK_TRANSACTION;
          message.content = taskTransaction.toJsonObject();
          this.interactionProtocolEngine.sendMessage(message, send -> {

            if (send.failed()) {

              final var cause = send.cause();
              Logger.trace(cause, "Fail doTaskTransaction: {} of {} is not accepted", message, body);
              OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              Logger.trace("Accepted sendIncentive {} ", body);
              OperationReponseHandlers.responseWith(resultHandler, Status.ACCEPTED, taskTransaction);
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
  public void retrieveTasksPage(final String appId, final String requesterId, final String taskTypeId, final String goalName, final String goalDescription, final Long startFrom, final Long startTo, final Long endFrom, final Long endTo,
      final Long deadlineFrom, final Long deadlineTo, final Boolean hasCloseTs, final Long closeFrom, final Long closeTo, final List<String> order, final int offset, final int limit, final OperationRequest context,
      final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var query = TasksRepository.creteTasksPageQuery(appId, requesterId, taskTypeId, goalName, goalDescription, startFrom, startTo, deadlineFrom, deadlineTo, endFrom, endTo, hasCloseTs, closeFrom, closeTo);

    try {

      final var sort = Repository.queryParamToSort(order, "bad_order", (value) -> {

        switch (value) {
        case "goalName":
        case "goal.name":
          return "goal.name";
        case "goalDescription":
        case "goal.description":
          return "goal.description";
        case "start":
        case "end":
        case "deadline":
        case "close":
          return value + "Ts";
        case "id":
        case "taskTypeId":
        case "requesterId":
        case "appId":
        case "startTs":
        case "endTs":
        case "deadlineTs":
        case "closeTs":
          return value;
        default:
          return null;
        }

      });

      this.repository.retrieveTasksPageObject(query, sort, offset, limit, retrieve -> {

        if (retrieve.failed()) {

          final var cause = retrieve.cause();
          Logger.debug(cause, "GET /tasks with {} => Retrieve error", query);
          OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          final var tasksPage = retrieve.result();
          Logger.debug("GET /tasks with {} => {}.", query, tasksPage);
          OperationReponseHandlers.responseOk(resultHandler, tasksPage);
        }

      });

    } catch (final ValidationErrorException error) {

      Logger.debug(error, "GET /tasks with {} => Retrieve error", query);
      OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, error);

    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskTypesPage(final String name, final String description, final List<String> keywords, final List<String> order, final int offset, final int limit, final OperationRequest context,
      final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var query = TaskTypesRepository.creteTaskTypesPageQuery(name, description, keywords);

    try {

      final var sort = Repository.queryParamToSort(order, "bad_order", (value) -> {

        switch (value) {
        case "name":
        case "description":
        case "keywords":
          return value;
        default:
          return null;
        }

      });

      this.typesRepository.retrieveTaskTypesPageObject(query, sort, offset, limit, retrieve -> {

        if (retrieve.failed()) {

          final var cause = retrieve.cause();
          Logger.debug(cause, "GET /tasks/types with {} => Retrieve error", query);
          OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          final var taskTypesPage = retrieve.result();
          Logger.debug("GET /tasks/types with {} => {}.", query, taskTypesPage);
          OperationReponseHandlers.responseOk(resultHandler, taskTypesPage);
        }

      });

    } catch (final ValidationErrorException error) {

      Logger.debug(error, "GET /tasks/types with {} => Retrieve error", query);
      OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, error);

    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mergeTask(final String taskId, final JsonObject body, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var source = Model.fromJsonObject(body, Task.class);
    if (source == null) {

      Logger.debug("The {} is not a valid Task to merge.", body);
      OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_to_merge", "The task to merge is not right.");

    } else {

      this.repository.searchTask(taskId, search -> {

        final var target = search.result();
        if (target == null) {

          Logger.debug(search.cause(), "Not found task {} to merge", taskId);
          OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task_to_merge", "You can not merge the task '" + taskId + "', because it does not exist.");

        } else {

          target.merge(source, "bad_new_task", this.vertx).onComplete(merge -> {

            if (merge.failed()) {

              final var cause = merge.cause();
              Logger.debug(cause, "Cannot merge {} with {}.", target, source);
              OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              final var merged = merge.result();
              if (merged.equals(target)) {

                OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "task_to_merge_equal_to_original",
                    "You can not merge the task of the task '" + taskId + "', because the new values is equals to the current one.");

              } else {

                this.repository.updateTask(merged, updated -> {

                  if (updated.failed()) {

                    final var cause = updated.cause();
                    Logger.debug(cause, "Cannot merge {}.", target);
                    OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

                  } else {

                    OperationReponseHandlers.responseOk(resultHandler, merged);

                  }

                });
              }
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
  public void mergeTaskType(final String taskTypeId, final JsonObject body, final OperationRequest context, final Handler<AsyncResult<OperationResponse>> resultHandler) {

    final var source = Model.fromJsonObject(body, TaskType.class);
    if (source == null) {

      Logger.debug("The {} is not a valid TaskType to merge.", body);
      OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_type_to_merge", "The task type to merge is not right.");

    } else {

      this.typesRepository.searchTaskType(taskTypeId, search -> {

        final var target = search.result();
        if (target == null) {

          Logger.debug(search.cause(), "Not found task type {} to merge", taskTypeId);
          OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task_type_to_merge", "You can not merge the task type '" + taskTypeId + "', because it does not exist.");

        } else {

          target.merge(source, "bad_new_task_type", this.vertx).onComplete(merge -> {

            if (merge.failed()) {

              final var cause = merge.cause();
              Logger.debug(cause, "Cannot merge {} with {}.", target, source);
              OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              final var merged = merge.result();
              if (merged.equals(target)) {

                OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "task_type_to_merge_equal_to_original",
                    "You can not merge the task type '" + taskTypeId + "', because the new values is equals to the current one.");

              } else {
                this.typesRepository.updateTaskType(merged, update -> {

                  if (update.failed()) {

                    final var cause = update.cause();
                    Logger.debug(cause, "Cannot update {}.", target);
                    OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

                  } else {

                    OperationReponseHandlers.responseOk(resultHandler, merged);

                  }

                });
              }
            }
          });
        }
      });
    }

  }

}
