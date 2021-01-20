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

import eu.internetofus.common.components.HumanDescription;
import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
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
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @return the future task to use in a test.
   */
  public Future<Task> assertTaskForTest(final Vertx vertx, VertxTestContext testContext) {

    var future = StoreServices.storeCommunityExample(1, vertx, testContext).compose(community -> {

      var task = new Task();
      task.appId = community.appId;
      task.communityId = community.id;
      task.goal = new HumanDescription();
      task.goal.name = "Where can I eat the best pizza?";
      task.requesterId = community.members.get(0).userId;
      task.attributes = new JsonObject().put("kindOfAnswerer", "anyone");
      return StoreServices.storeTask(task, vertx, testContext);

    });

    return testContext.assertComplete(future);
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
  public void shouldRetrieveTaskTransactionPageFromATask(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskExample(1, vertx, testContext).onSuccess(task -> {

      testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("taskId", task.id)).expect(res -> {

        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
        assertThat(page.total).isEqualTo(0l);
        assertThat(page.transactions).isNull();

      }).send(testContext);

    });
  }

}
