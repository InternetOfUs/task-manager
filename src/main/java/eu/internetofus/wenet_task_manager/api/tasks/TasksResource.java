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
import eu.internetofus.common.components.profile_manager.CommunityMember;
import eu.internetofus.common.components.profile_manager.CommunityProfile;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.components.service.WeNetService;
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
import java.util.ArrayList;
import java.util.function.Consumer;
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
   * Return the community associated to an application.
   *
   * @param appId            identifier of the application to obtain the
   *                         associated community.
   * @param consumeCommunity function to consume the found community. If no
   *                         community found it pass a {@code null}.
   */
  protected void retrieveAppCommunity(final String appId, final Consumer<CommunityProfile> consumeCommunity) {

    final var service = WeNetProfileManager.createProxy(this.vertx);
    service.retrieveCommunityProfilesPage(appId, null, null, null, null, "-_creationTs", 0, 1).onComplete(retrieve -> {

      final var page = retrieve.result();
      if (retrieve.failed() || page.communities == null || page.communities.isEmpty()) {

        WeNetService.createProxy(this.vertx).retrieveAppUserIds(appId).onComplete(retrieveUsers -> {

          final var value = retrieveUsers.result();
          if (value == null || value.isEmpty()) {

            consumeCommunity.accept(null);

          } else {

            final var newCommunity = new CommunityProfile();
            newCommunity.appId = appId;
            newCommunity.name = "Community of " + appId;
            newCommunity.members = new ArrayList<>();
            for (var i = 0; i < value.size(); i++) {

              final var member = new CommunityMember();
              member.userId = value.getString(i);
              newCommunity.members.add(member);

            }
            service.createCommunity(newCommunity).onComplete(createHandler -> {

              final var community = createHandler.result();
              if (community == null) {

                consumeCommunity.accept(null);
              } else {

                consumeCommunity.accept(community);
              }

            });

          }

        });

      } else {

        consumeCommunity.accept(page.communities.get(0));

      }

    });

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTask(final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    if (body.getString("communityId", null) == null) {

      this.retrieveAppCommunity(body.getString("appId"), community -> {

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
      ModelResources.createModelChain(this.vertx, body, model, this.repository::storeTask, context, () -> {

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
        (id, hanlder) -> this.repository.searchTask(id).onComplete(hanlder), this.repository::updateTask, context);

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
        (id, hanlder) -> this.repository.searchTask(id).onComplete(hanlder), this.repository::updateTask, context);

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

}
