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
package eu.internetofus.wenet_task_manager.api.messages;

import static eu.internetofus.common.vertx.HttpResponses.assertThatBodyIs;
import static io.reactiverse.junit5.web.TestRequest.queryParam;
import static io.reactiverse.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.models.HumanDescription;
import eu.internetofus.common.components.models.Message;
import eu.internetofus.common.components.models.Task;
import eu.internetofus.common.components.models.TaskTransaction;
import eu.internetofus.common.model.ErrorMessage;
import eu.internetofus.common.model.TimeManager;
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
 * Test the {@link Messages} integration.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class MessagesIT {

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
  public Future<Task> assertTaskForTest(final String testId, final int index, final Vertx vertx,
      final VertxTestContext testContext) {

    final var task = new Task();
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

      final var transaction = new TaskTransaction();
      transaction.id = String.valueOf(i);
      transaction.actioneerId = testId + "_Actioneer_" + i;
      transaction.taskId = task.id;
      transaction.label = testId + "_label_" + i;
      transaction.attributes = new JsonObject().put("i", i).put("index", index);
      transaction._creationTs = TimeManager.now() - index * 50000;
      transaction._lastUpdateTs = task._creationTs + index * 5000;
      task.transactions.add(transaction);
      transaction.messages = new ArrayList<>();
      for (var j = 0; j < 20; j++) {

        final var message = new Message();
        message.appId = task.appId;
        message.label = testId + "_label_" + j;
        message.attributes = new JsonObject().put("i", i).put("j", j);
        message.receiverId = testId + "_Receiver_" + j;
        transaction.messages.add(message);
      }

    }

    return testContext.assertComplete(TasksRepository.createProxy(vertx).storeTask(task));
  }

  /**
   * Should retrieve empty task messages page.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveEmptyTaskMessagePage(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Messages.PATH).with(queryParam("taskId", "undefined")).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
      final var page = assertThatBodyIs(MessagesPage.class, res);
      assertThat(page.total).isEqualTo(0l);
      assertThat(page.messages).isNull();

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
  public void shouldFailRetrieveTaskMessagePageWithBadOrder(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    testRequest(client, HttpMethod.GET, Messages.PATH).with(queryParam("order", "undefined")).expect(res -> {

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
  public void shouldRetrieveTaskMessagePageFromTaskField(final String field, final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task3 -> {

          testRequest(client, HttpMethod.GET, Messages.PATH)
              .with(queryParam("order", field + ",taskId,transactionsIndex,messagesIndex"),
                  queryParam(field, task.toJsonObject().getString(field)))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(MessagesPage.class, res);
                assertThat(page.total).isEqualTo(800l);
                if (task.id.compareTo(task3.id) > 0) {

                  assertThat(page.messages).isEqualTo(task3.transactions.get(0).messages.subList(0, 10));

                } else {

                  assertThat(page.messages).isEqualTo(task.transactions.get(0).messages.subList(0, 10));
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
  public void shouldRetrieveTaskMessagePageFromTaskByGoalField(final String field, final Vertx vertx,
      final WebClient client, final VertxTestContext testContext) {

    final var goalField = "goal" + field.substring(0, 1).toUpperCase() + field.substring(1);
    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task3 -> {

          testRequest(client, HttpMethod.GET, Messages.PATH)
              .with(queryParam("order", goalField + ",taskId,transactionsIndex,messagesIndex"),
                  queryParam(goalField, task.goal.toJsonObject().getString(field)))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(MessagesPage.class, res);
                assertThat(page.total).isEqualTo(800l);
                if (task.id.compareTo(task3.id) > 0) {

                  assertThat(page.messages).isEqualTo(task3.transactions.get(0).messages.subList(0, 10));

                } else {

                  assertThat(page.messages).isEqualTo(task.transactions.get(0).messages.subList(0, 10));
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
  public void shouldRetrieveMessagePageFromTaskByGoalKeywords(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task3 -> {

          testRequest(client, HttpMethod.GET, Messages.PATH)
              .with(queryParam("order", "goalKeywords,taskId,transactionsIndex,messagesIndex"),
                  queryParam("goalKeywords", "1," + testId))
              .expect(res -> {

                assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
                final var page = assertThatBodyIs(MessagesPage.class, res);
                assertThat(page.total).isEqualTo(800l);
                if (task.id.compareTo(task3.id) > 0) {

                  assertThat(page.messages).isEqualTo(task3.transactions.get(0).messages.subList(0, 10));

                } else {

                  assertThat(page.messages).isEqualTo(task.transactions.get(0).messages.subList(0, 10));
                }

              }).send(testContext);
        });
      });
    });
  }

  /**
   * Should retrieve empty task messages page because no message on the specified
   * time range.
   *
   * @param fieldPrefix prefix of the time field that not have values on the
   *                    range.
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @ParameterizedTest(name = "Should retrieve empty page that filter by {0}From and {0}To")
  @ValueSource(strings = { "taskCreation", "taskUpdate", "close", "transactionCreation", "transactionUpdate" })
  public void shouldRetrieveEmptyMessagePageBecauseNoMessageOnTimeRange(final String fieldPrefix, final Vertx vertx,
      final WebClient client, final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH)
            .with(queryParam("order", fieldPrefix + "Ts"), queryParam("goalKeywords", testId),
                queryParam(fieldPrefix + "From", "0"), queryParam(fieldPrefix + "To", "1"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(0l);
              assertThat(page.messages).isNull();

            }).send(testContext);

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
  public void shouldRetrieveEmptyMessagePageBecauseNoTransactionOnClosedTask(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH)
            .with(queryParam("order", "taskId"), queryParam("goalKeywords", testId), queryParam("hasCloseTs", "true"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(0l);
              assertThat(page.messages).isNull();

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
  public void shouldRetrieveMessagePageFromTaskId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH).with(queryParam("taskId", task.id),
            queryParam("offset", String.valueOf(5)), queryParam("limit", String.valueOf(7))).expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(400l);
              assertThat(page.messages).isEqualTo(task.transactions.get(0).messages.subList(5, 12));

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
  public void shouldRetrieveMessagePageFromTransactionId(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH)
            .with(queryParam("goalKeywords", testId), queryParam("transactionId", "/^[1|5|7]$/"),
                queryParam("offset", String.valueOf(4)), queryParam("limit", String.valueOf(3)),
                queryParam("order", "transactionId,taskId,transactionsIndex,messagesIndex"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(120l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.messages).containsExactly(task2.transactions.get(1).messages.get(4),
                    task2.transactions.get(1).messages.get(5), task2.transactions.get(1).messages.get(6));

              } else {

                assertThat(page.messages).containsExactly(task.transactions.get(1).messages.get(4),
                    task.transactions.get(1).messages.get(5), task.transactions.get(1).messages.get(6));
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
  public void shouldRetrieveMessagePageFromTransactionLabel(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH)
            .with(queryParam("transactionLabel", "/^" + testId + "_label_[1|5|7]$/"),
                queryParam("offset", String.valueOf(1)), queryParam("limit", String.valueOf(3)),
                queryParam("order", "transactionLabel,taskId,transactionsIndex,messagesIndex"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(120l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.messages).containsExactly(task2.transactions.get(1).messages.get(1),
                    task2.transactions.get(1).messages.get(2), task2.transactions.get(1).messages.get(3));

              } else {

                assertThat(page.messages).containsExactly(task.transactions.get(1).messages.get(1),
                    task.transactions.get(1).messages.get(2), task.transactions.get(1).messages.get(3));
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
  public void shouldRetrieveMessagePageFromTransactionActioneer(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH)
            .with(queryParam("transactionActioneerId", "/^" + testId + "_Actioneer_[1|5|7]$/"),
                queryParam("offset", String.valueOf(2)), queryParam("limit", String.valueOf(3)),
                queryParam("order", "transactionActioneerId,taskId,transactionsIndex,messagesIndex"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(120l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.messages).containsExactly(task2.transactions.get(1).messages.get(2),
                    task2.transactions.get(1).messages.get(3), task2.transactions.get(1).messages.get(4));

              } else {

                assertThat(page.messages).containsExactly(task.transactions.get(1).messages.get(2),
                    task.transactions.get(1).messages.get(3), task.transactions.get(1).messages.get(4));
              }

            }).send(testContext);
      });
    });
  }

  /**
   * Should retrieve a page depending of the message label.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveMessagePageFromMessageLabel(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH)
            .with(queryParam("label", "/^" + testId + "_label_[1|5|7]$/"), queryParam("offset", String.valueOf(2)),
                queryParam("limit", String.valueOf(3)), queryParam("order", "label,taskId,transactionsIndex"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(120l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.messages).containsExactly(task2.transactions.get(2).messages.get(1),
                    task2.transactions.get(3).messages.get(1), task2.transactions.get(4).messages.get(1));

              } else {

                assertThat(page.messages).containsExactly(task.transactions.get(2).messages.get(1),
                    task.transactions.get(3).messages.get(1), task.transactions.get(4).messages.get(1));
              }

            }).send(testContext);
      });
    });
  }

  /**
   * Should retrieve a page depending of the message receiver.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   */
  @Test
  public void shouldRetrieveMessagePageFromMessageReceiver(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    final var testId = UUID.randomUUID().toString();
    this.assertTaskForTest(testId, 1, vertx, testContext).onSuccess(task -> {

      this.assertTaskForTest(testId, 2, vertx, testContext).onSuccess(task2 -> {

        testRequest(client, HttpMethod.GET, Messages.PATH)
            .with(queryParam("receiverId", "/^" + testId + "_Receiver_[1|5|7]$/"),
                queryParam("offset", String.valueOf(3)), queryParam("limit", String.valueOf(2)),
                queryParam("order", "receiverId,taskId,transactionsIndex"))
            .expect(res -> {

              assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
              final var page = assertThatBodyIs(MessagesPage.class, res);
              assertThat(page.total).isEqualTo(120l);
              if (task.id.compareTo(task2.id) > 0) {

                assertThat(page.messages).containsExactly(task2.transactions.get(3).messages.get(1),
                    task2.transactions.get(4).messages.get(1));

              } else {

                assertThat(page.messages).containsExactly(task.transactions.get(3).messages.get(1),
                    task.transactions.get(4).messages.get(1));
              }

            }).send(testContext);
      });
    });
  }

}
