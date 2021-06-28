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
package eu.internetofus.wenet_task_manager.api.task_transactions;

import eu.internetofus.common.model.ValidationErrorException;
import eu.internetofus.common.vertx.ServiceRequests;
import eu.internetofus.common.vertx.ServiceResponseHandlers;
import eu.internetofus.wenet_task_manager.api.tasks.Tasks;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import javax.ws.rs.core.Response.Status;
import org.tinylog.Logger;

/**
 * Resource that provide the methods for the {@link TaskTransactions}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TaskTransactionsResource implements TaskTransactions {

  /**
   * The event bus that is using.
   */
  protected Vertx vertx;

  /**
   * Create a new instance to provide the services of the {@link Tasks}.
   *
   * @param vertx where resource is defined.
   */
  public TaskTransactionsResource(final Vertx vertx) {

    this.vertx = vertx;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void retrieveTaskTransactionsPage(final String appId, final String requesterId, final String taskTypeId,
      final String goalName, final String goalDescription, final String goalKeywordsValue, final Long taskCreationFrom,
      final Long taskCreationTo, final Long taskUpdateFrom, final Long taskUpdateTo, final Boolean hasCloseTs,
      final Long closeFrom, final Long closeTo, final String taskId, final String id, final String label,
      final String actioneerId, final Long creationFrom, final Long creationTo, final Long updateFrom,
      final Long updateTo, final String orderValue, final int offset, final int limit, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    final var goalKeywords = ServiceRequests.extractQueryArray(goalKeywordsValue);
    final var order = ServiceRequests.extractQueryArray(orderValue);
    final var query = TasksRepository.createTaskTransactionsPageQuery(appId, requesterId, taskTypeId, goalName,
        goalDescription, goalKeywords, taskCreationFrom, taskCreationTo, taskUpdateFrom, taskUpdateTo, hasCloseTs,
        closeFrom, closeTo, taskId, id, label, actioneerId, creationFrom, creationTo, updateFrom, updateTo);

    try {

      final var sort = TasksRepository.createTaskTransactionsPageSort(order);
      TasksRepository.createProxy(this.vertx).retrieveTaskTransactionsPage(query, sort, offset, limit)
          .onComplete(retrieve -> {

            if (retrieve.failed()) {

              final var cause = retrieve.cause();
              Logger.debug(cause, "GET /taskTransactions with {} => Retrieve error", query);
              ServiceResponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

            } else {

              final var tasksPage = retrieve.result();
              Logger.debug("GET /taskTransactions with {} => {}.", query, tasksPage);
              ServiceResponseHandlers.responseOk(resultHandler, tasksPage);
            }

          });

    } catch (final ValidationErrorException error) {

      Logger.debug(error, "GET /taskTransactions with {} => Retrieve error", query);
      ServiceResponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, error);

    }
  }

}
