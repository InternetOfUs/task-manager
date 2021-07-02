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
import eu.internetofus.common.components.models.TaskTransactionTest;
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
