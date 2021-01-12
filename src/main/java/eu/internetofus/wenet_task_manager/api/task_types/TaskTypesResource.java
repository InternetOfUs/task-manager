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

package eu.internetofus.wenet_task_manager.api.task_types;

import eu.internetofus.common.components.ValidationErrorException;
import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.vertx.ModelContext;
import eu.internetofus.common.vertx.ModelResources;
import eu.internetofus.common.vertx.ServiceContext;
import eu.internetofus.common.vertx.ServiceRequests;
import eu.internetofus.common.vertx.ServiceResponseHandlers;
import eu.internetofus.wenet_task_manager.api.tasks.Tasks;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import javax.ws.rs.core.Response.Status;
import org.tinylog.Logger;

/**
 * Resource that provide the methods for the {@link TaskTypes}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TaskTypesResource implements TaskTypes {

  /**
   * The event bus that is using.
   */
  protected Vertx vertx;

  /**
   * The repository to manage the task types.
   */
  protected TaskTypesRepository typesRepository;

  /**
   * Create a new instance to provide the services of the {@link Tasks}.
   *
   * @param vertx where resource is defined.
   */
  public TaskTypesResource(final Vertx vertx) {

    this.vertx = vertx;
    this.typesRepository = TaskTypesRepository.createProxy(vertx);

  }

  /**
   * Create the task type context.
   *
   * @return the context of the {@link TaskType}.
   */
  protected ModelContext<TaskType, String> createTaskTypeContext() {

    final var context = new ModelContext<TaskType, String>();
    context.name = "task_type";
    context.type = TaskType.class;
    return context;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskType(final String taskTypeId, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskTypeContext();
    model.id = taskTypeId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.retrieveModel(model, this.typesRepository::searchTaskType, context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTaskType(final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskTypeContext();
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.createModel(this.vertx, body, model, this.typesRepository::storeTaskType, context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateTaskType(final String taskTypeId, final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskTypeContext();
    model.id = taskTypeId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.updateModel(this.vertx, body, model, this.typesRepository::searchTaskType,
        this.typesRepository::updateTaskType, context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteTaskType(final String taskTypeId, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskTypeContext();
    model.id = taskTypeId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.deleteModel(model, this.typesRepository::deleteTaskType, context);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskTypesPage(final String name, final String description, final String keywordsValue,
      final String orderValue, final int offset, final int limit, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    var keywords = ServiceRequests.extractQueryArray(keywordsValue);
    var order = ServiceRequests.extractQueryArray(orderValue);
    final var query = TaskTypesRepository.createTaskTypesPageQuery(name, description, keywords);

    try {

      final var sort = TaskTypesRepository.createTaskTypesPageSort(order);
      this.typesRepository.retrieveTaskTypesPageObject(query, sort, offset, limit, retrieve -> {

        if (retrieve.failed()) {

          final var cause = retrieve.cause();
          Logger.debug(cause, "GET /tasks/types with {} => Retrieve error", query);
          ServiceResponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

        } else {

          final var taskTypesPage = retrieve.result();
          Logger.debug("GET /tasks/types with {} => {}.", query, taskTypesPage);
          ServiceResponseHandlers.responseOk(resultHandler, taskTypesPage);
        }

      });

    } catch (final ValidationErrorException error) {

      Logger.debug(error, "GET /tasks/types with {} => Retrieve error", query);
      ServiceResponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, error);

    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mergeTaskType(final String taskTypeId, final JsonObject body, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var model = this.createTaskTypeContext();
    model.id = taskTypeId;
    final var context = new ServiceContext(request, resultHandler);
    ModelResources.mergeModel(this.vertx, body, model, this.typesRepository::searchTaskType,
        this.typesRepository::updateTaskType, context);

  }

}
