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

import static eu.internetofus.common.components.AbstractComponentMocker.createClientWithDefaultSession;
import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.interaction_protocol_engine.WeNetInteractionProtocolEngine;
import eu.internetofus.common.components.interaction_protocol_engine.WeNetInteractionProtocolEngineMocker;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.components.profile_manager.WeNetProfileManagerMocker;
import eu.internetofus.common.components.service.WeNetService;
import eu.internetofus.common.components.service.WeNetServiceSimulator;
import eu.internetofus.common.components.service.WeNetServiceSimulatorMocker;
import eu.internetofus.common.components.task_manager.TaskTransactionTest;
import eu.internetofus.common.components.task_manager.WeNetTaskManager;
import eu.internetofus.common.components.task_manager.WeNetTaskManagerMocker;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test the {@link TasksResource}
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith({ VertxExtension.class, MockitoExtension.class })
public class TasksResourceTest {

  /**
   * The profile manager mocked server.
   */
  protected static WeNetProfileManagerMocker profileManagerMocker;

  /**
   * The task manager mocked server.
   */
  protected static WeNetTaskManagerMocker taskManagerMocker;

  /**
   * The service mocked server.
   */
  protected static WeNetServiceSimulatorMocker serviceMocker;

  /**
   * Start the mocker server.
   */
  @BeforeAll
  public static void startMockers() {

    profileManagerMocker = WeNetProfileManagerMocker.start();
    taskManagerMocker = WeNetTaskManagerMocker.start();
    serviceMocker = WeNetServiceSimulatorMocker.start();
  }

  /**
   * Stop the mocker server.
   */
  @AfterAll
  public static void stopMockers() {

    profileManagerMocker.stopServer();
    taskManagerMocker.stopServer();
    serviceMocker.stopServer();
  }

  /**
   * Register the necessary services before to test.
   *
   * @param vertx event bus to register the necessary services.
   */
  @BeforeEach
  public void registerServices(final Vertx vertx) {

    final var client = createClientWithDefaultSession(vertx);
    final var profileConf = profileManagerMocker.getComponentConfiguration();
    WeNetProfileManager.register(vertx, client, profileConf);

    final var taskConf = taskManagerMocker.getComponentConfiguration();
    WeNetTaskManager.register(vertx, client, taskConf);

    final var conf = serviceMocker.getComponentConfiguration();
    WeNetService.register(vertx, client, conf);
    WeNetServiceSimulator.register(vertx, client, conf);

  }

  /**
   * Should not retrieve tasks page because can not found the page.
   *
   * @param vertx       event bus to use.
   * @param testContext context of the test.
   * @param request     mocked request to do the operation.
   */
  @Test
  public void shouldNotRetrieveTaskPageBecuaseRetrieveFailed(final Vertx vertx, final VertxTestContext testContext,
      @Mock final ServiceRequest request) {

    final var resource = new TasksResource(vertx);
    resource.retrieveTasksPage(null, null, null, null, null, null, null, null, null, null, null, null, null, 0, 100,
        request, testContext.succeeding(response -> testContext.verify(() -> {

          assertThat(response.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
          testContext.completeNow();

        })));

  }

  /**
   * Should not do a transaction because can not send message to the interaction
   * protocol engine.
   *
   * @param vertx       event bus to use.
   * @param testContext context of the test.
   * @param request     mocked request to do the operation.
   */
  @Test
  public void shouldDoTransactionBecauseSendMessageFailed(final Vertx vertx, final VertxTestContext testContext,
      @Mock final ServiceRequest request) {

    new TaskTransactionTest().createModelExample(1, vertx, testContext)
        .onComplete(testContext.succeeding(transaction -> {

          final var resource = new TasksResource(vertx);
          resource.doTaskTransaction(transaction.toJsonObject(), request,
              testContext.succeeding(response -> testContext.verify(() -> {

                assertThat(response.getStatusCode()).isEqualTo(Status.ACCEPTED.getStatusCode());
                testContext.completeNow();

              })));
        }));

  }

  /**
   * Should do a transaction.
   *
   * @param vertx       event bus to use.
   * @param testContext context of the test.
   * @param request     mocked request to do the operation.
   */
  @Test
  public void shouldDoTransaction(final Vertx vertx, final VertxTestContext testContext,
      @Mock final ServiceRequest request) {

    final var client = createClientWithDefaultSession(vertx);
    final var interactionProtocolEngineMocker = WeNetInteractionProtocolEngineMocker.start();
    final var interactionProtocolEngineConf = interactionProtocolEngineMocker.getComponentConfiguration();
    WeNetInteractionProtocolEngine.register(vertx, client, interactionProtocolEngineConf);

    new TaskTransactionTest().createModelExample(1, vertx, testContext)
        .onComplete(testContext.succeeding(transaction -> {

          final var resource = new TasksResource(vertx);
          resource.doTaskTransaction(transaction.toJsonObject(), request,
              testContext.succeeding(response -> testContext.verify(() -> {

                assertThat(response.getStatusCode()).isEqualTo(Status.ACCEPTED.getStatusCode());
                interactionProtocolEngineMocker.stopServer();
                testContext.completeNow();

              })));
        }));

  }

}
