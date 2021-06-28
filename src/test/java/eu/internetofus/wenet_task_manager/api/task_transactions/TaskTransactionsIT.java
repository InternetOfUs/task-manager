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

import eu.internetofus.common.model.TimeManager;
import eu.internetofus.common.model.ErrorMessage;
import eu.internetofus.common.components.models.HumanDescription;
import eu.internetofus.common.components.models.Task;
import eu.internetofus.common.components.models.TaskTransaction;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
   * @param testId      identifier of the test.
   * @param index       of the task to create.
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @return the future task to use in a test.
   */
  public Future<Task> assertTaskForTest(String testId, int index, final Vertx vertx, VertxTestContext testContext) {

    Task task = new Task();
    task.id = UUID.randomUUID().toString();
    task.appId = testId + "_App_" + index;
    task.attributes = new JsonObject().put("index", index).put("testId", testId);
    task.communityId = testId + "_Community_" + index;
    task.goal = new HumanDescription();
    task.goal.name = testId + "_Goal_name_" + index;
    task.goal.description = testId + "_Goal_description_" + index;
    task.goal.keywords = new ArrayList<>();
    task.goal.keywords.add(testId);
    task.goal.keywords.add(String.valueOf(index));
    task.requesterId = testId + "_Requester_" + index;
    task.taskTypeId = testId + "_TaskType_" + index;
    task.transactions = new ArrayList<>();
    task._creationTs = TimeManager.now() - index * 100000;
    task._lastUpdateTs = task._creationTs + index * 10000;
    for (var i = 0; i < 20; i++) {

      var transaction = new TaskTransaction();
      transaction.id = String.valueOf(i);
      transaction.actioneerId = testId + "_Actioneer_" + i;
      transaction.taskId = task.id;
      transaction.label = testId + "_label_" + i;
      transaction.attributes = new JsonObject().put("i", i).put("index", index);
      transaction._creationTs = TimeManager.now() - index * 50000;
      transaction._lastUpdateTs = task._creationTs + index * 5000;
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
   * Should not retrieve page if the order is not right.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldFailRetrieveTaskTransactionPageWithBadOrder(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("order", "undefined")).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      final var error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty();
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

    }).send(testContext);

  }

  /**
   * Should retrieve page by task field.
   *
   * @param field       to obtain the page.
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @ParameterizedTest(name = "Should retrieve page that filter by {0}")
  @ValueSource(strings = { "appId", "requesterId", "taskTypeId" })
  public void shouldRetrieveTaskTransactionPageFromTaskField(String field, final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task3 -> {

          testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
              .with(queryParam("order", field + ",taskId,transactionsIndex"),
                  queryParam(field, task.toJsonObject().getString(field)))
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
   * Should retrieve page by task goal field.
   *
   * @param field       to obtain the page.
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @ParameterizedTest(name = "Should retrieve page that filter by goal {0}")
  @ValueSource(strings = { "name", "description" })
  public void shouldRetrieveTaskTransactionPageFromTaskByGoalField(String field, final Vertx vertx,
      final WebClient client, final VertxTestContext testContext) {

    var goalField = "goal" + field.substring(0, 1).toUpperCase() + field.substring(1);
    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task3 -> {

          testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
              .with(queryParam("order", goalField + ",taskId,transactionsIndex"),
                  queryParam(goalField, task.goal.toJsonObject().getString(field)))
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
   * Should retrieve page by task goal keywords.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageFromTaskByGoalKeywords(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task3 -> {

          testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
              .with(queryParam("order", "goalKeywords,taskId,transactionsIndex"),
                  queryParam("goalKeywords", "1," + testId))
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
   * Should retrieve empty task transactions page because no transaction on the
   * specified time range.
   *
   * @param fieldPrefix prefix of the time field that not have values on the
   *                    range.
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @ParameterizedTest(name = "Should retrieve empty page that filter by {0}From and {0}To")
  @ValueSource(strings = { "taskCreation", "taskUpdate", "close", "creation", "update" })
  public void shouldRetrieveEmptyTaskTransactionPageBecauseNoTransactionOnTimeRange(String fieldPrefix,
      final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
            .with(queryParam("order", fieldPrefix + "Ts,taskId,transactionsIndex"), queryParam("goalKeywords", testId),
                queryParam(fieldPrefix + "From", "0"), queryParam(fieldPrefix + "To", "1"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
              assertThat(page.total).isEqualTo(0l);
              assertThat(page.transactions).isNull();

            }).send(testContext);

      });
    });

  }

  /**
   * Should retrieve empty task transactions page because no transaction on the
   * specified time range.
   *
   * @param fieldPrefix prefix of the time field that not have values on the
   *                    range.
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @ParameterizedTest(name = "Should retrieve page that filter by {0}From and {0}To")
  @ValueSource(strings = { "taskCreation", "taskUpdate", "creation", "update" })
  public void shouldRetrieveTaskTransactionPageOnTimeRange(String fieldPrefix, final Vertx vertx,
      final WebClient client, final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("goalKeywords", testId),
            queryParam("order", "taskId,transactionsIndex"), queryParam(fieldPrefix + "From", "0"),
            queryParam(fieldPrefix + "To", "" + Long.MAX_VALUE), queryParam("offset", "3"), queryParam("limit", "7"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
              assertThat(page.total).isEqualTo(40l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.transactions).isEqualTo(task2.transactions.subList(3, 10));

              } else {

                assertThat(page.transactions).isEqualTo(task.transactions.subList(3, 10));
              }

            }).send(testContext);

      });
    });

  }

  /**
   * Should retrieve empty task transactions on close range.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageOnCloseTimeRange(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        task2.closeTs = TimeManager.now();
        testContext.assertComplete(TasksRepository.createProxy(vertx).updateTask(task2)).onSuccess(empty -> {

          testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
              .with(queryParam("goalKeywords", testId), queryParam("closeFrom", "0"),
                  queryParam("closeTo", "" + Long.MAX_VALUE), queryParam("offset", "3"), queryParam("limit", "7"))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
                assertThat(page.total).isEqualTo(20l);
                assertThat(page.transactions).isEqualTo(task2.transactions.subList(3, 10));

              }).send(testContext);
        });
      });
    });

  }

  /**
   * Should retrieve empty task transactions on close task.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageOnCloseTask(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        task2.closeTs = TimeManager.now();
        testContext.assertComplete(TasksRepository.createProxy(vertx).updateTask(task2)).onSuccess(empty -> {

          testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("goalKeywords", testId),
              queryParam("hasCloseTs", "true"), queryParam("offset", "3"), queryParam("limit", "7")).expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
                assertThat(page.total).isEqualTo(20l);
                assertThat(page.transactions).isEqualTo(task2.transactions.subList(3, 10));

              }).send(testContext);
        });
      });
    });

  }

  /**
   * Should retrieve empty task transactions page because no transaction on a
   * closed task.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveEmptyTaskTransactionPageBecauseNoTransactionOnClosedTask(final Vertx vertx,
      final WebClient client, final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
            .with(queryParam("order", "taskId"), queryParam("goalKeywords", testId), queryParam("hasCloseTs", "true"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
              assertThat(page.total).isEqualTo(0l);
              assertThat(page.transactions).isNull();

            }).send(testContext);

      });
    });

  }

  /**
   * Should retrieve a page depending of the task identifier.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageFromTaskId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task2 -> {

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

  /**
   * Should retrieve a page depending of the transaction identifier.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageFromTransactionId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, TaskTransactions.PATH).with(queryParam("goalKeywords", testId),
            queryParam("id", "/^[1|5|7]$/"), queryParam("offset", String.valueOf(2)),
            queryParam("limit", String.valueOf(3)), queryParam("order", "id,taskId")).expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
              assertThat(page.total).isEqualTo(6l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.transactions).containsExactly(task2.transactions.get(5), task.transactions.get(5),
                    task2.transactions.get(7));

              } else {

                assertThat(page.transactions).containsExactly(task.transactions.get(5), task2.transactions.get(5),
                    task.transactions.get(7));
              }

            }).send(testContext);
      });
    });
  }

  /**
   * Should retrieve a page depending of the transaction label.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageFromTransactionLabel(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
            .with(queryParam("label", "/^" + testId + "_label_[1|5|7]$/"), queryParam("offset", String.valueOf(2)),
                queryParam("limit", String.valueOf(3)), queryParam("order", "label,taskId"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
              assertThat(page.total).isEqualTo(6l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.transactions).containsExactly(task2.transactions.get(5), task.transactions.get(5),
                    task2.transactions.get(7));

              } else {

                assertThat(page.transactions).containsExactly(task.transactions.get(5), task2.transactions.get(5),
                    task.transactions.get(7));
              }

            }).send(testContext);
      });
    });
  }

  /**
   * Should retrieve a page depending of the transaction actioneer.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveTaskTransactionPageFromTransactionActioneer(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, TaskTransactions.PATH)
            .with(queryParam("actioneerId", "/^" + testId + "_Actioneer_[1|5|7]$/"),
                queryParam("offset", String.valueOf(2)), queryParam("limit", String.valueOf(3)),
                queryParam("order", "actioneerId,taskId"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(TaskTransactionsPage.class, res);
              assertThat(page.total).isEqualTo(6l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.transactions).containsExactly(task2.transactions.get(5), task.transactions.get(5),
                    task2.transactions.get(7));

              } else {

                assertThat(page.transactions).containsExactly(task.transactions.get(5), task2.transactions.get(5),
                    task.transactions.get(7));
              }

            }).send(testContext);
      });
    });
  }

}
