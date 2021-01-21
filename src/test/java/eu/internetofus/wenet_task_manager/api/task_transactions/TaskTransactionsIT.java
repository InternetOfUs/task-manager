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

import static eu.internetofus.common.vertx.HttpResponses.assertThatBodyIs;
import static io.reactiverse.junit5.web.TestRequest.queryParam;
import static io.reactiverse.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.HumanDescriptionTest;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskTransaction;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test the {@link TaskTransactions} integration.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TaskTransactionsIT {

  /**
   * Create the task for testing.
   *
   * @param index       of the task to create.
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @return the future task to use in a test.
   */
  public Future<Task> assertTaskForTest(int index, final Vertx vertx, VertxTestContext testContext) {

    Task task = new Task();
    task.id = UUID.randomUUID().toString();
    task.appId = "App_" + index;
    task.attributes = new JsonObject().put("index", index);
    task.communityId = "Community_" + index;
    task.goal = new HumanDescriptionTest().createModelExample(index);
    task.requesterId = "Requester_" + index;
    task.taskTypeId = "TaskType_" + index;
    task.transactions = new ArrayList<>();
    for (var i = 0; i < 20; i++) {

      var transaction = new TaskTransaction();
      transaction.id = String.valueOf(i);
      transaction.actioneerId = "Actioneer_" + i;
      transaction.taskId = task.id;
      transaction.label = "label_" + i;
      transaction.attributes = new JsonObject().put("i", i).put("index", index);
      task.transactions.add(transaction);

    }

    return testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(task));
  }

  /**
   * Should retrieve empty task transactions page.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveEmptyTaskTransactionPage(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("taskId", "undefined")).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
      assertThat(page.total).isEqualTo(0l);
      assertThat(page.transactions).isNull();

    }).send(testContext);

  }

  /**
   * Should retrieve empty task transactions page.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageFromApp(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    this.assertTaskForTest(1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(2, vertx, testContext).onSuccess(task2 -> {

        this.assertTaskForTest(1, vertx, testContext).onSuccess(task3 -> {

          testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("appId", task.appId))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
                assertThat(page.total).isEqualTo(40l);
                if (task.id.compareTo(task3.id) > 0) {

                  assertThat(page.transactions).isEqualTo(task3.transactions.subList(0, 10));

                } else {

                  assertThat(page.transactions).isEqualTo(task.transactions.subList(0, 10));
                }

              }).send(testContext);
        });
      });
    });
  }

  /**
   * Should retrieve empty task transactions page.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageFromTask(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    this.assertTaskForTest(1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("taskId", task.id),
            queryParam("offset", String.valueOf(5)), queryParam("limit", String.valueOf(7))).expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
              assertThat(page.total).isEqualTo(20l);
              assertThat(page.transactions).isEqualTo(task.transactions.subList(5, 12));

            }).send(testContext);
      });
    });
  }

}
