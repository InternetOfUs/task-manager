/*
 * -----------------------------------------------------------------------------
 *
 * Copyright (c) 1994 - 2021 UDT-IA, IIIA-CSIC
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
