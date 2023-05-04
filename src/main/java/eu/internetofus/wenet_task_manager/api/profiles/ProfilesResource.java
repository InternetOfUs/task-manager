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

package eu.internetofus.wenet_task_manager.api.profiles;

import eu.internetofus.common.components.interaction_protocol_engine.WeNetInteractionProtocolEngine;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.vertx.ServiceResponseHandlers;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import org.tinylog.Logger;

/**
 * Resource that implements the web services defined at {@link Profiles}.
 *
 * @see Profiles
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class ProfilesResource implements Profiles {

  /**
   * The event bus that is using.
   */
  protected Vertx vertx;

  /**
   * Create a new instance to provide the services of the {@link Profiles}.
   *
   * @param vertx with the event bus to use.
   */
  public ProfilesResource(final Vertx vertx) {

    this.vertx = vertx;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void profileDeleted(final String profileId, final ServiceRequest request,
      final Handler<AsyncResult<ServiceResponse>> resultHandler) {

    ServiceResponseHandlers.responseOk(resultHandler);

    TasksRepository.createProxy(this.vertx).deleteAllTaskWithRequester(profileId).onComplete(deleted -> {

      if (deleted.failed()) {

        Logger.trace(deleted.cause(), "Cannot delete the task with the requester {}.", profileId);

      } else {

        final var taskIds = deleted.result();
        if (taskIds != null) {

          final var profileManager = WeNetProfileManager.createProxy(this.vertx);
          final var interactionProtocolEngine = WeNetInteractionProtocolEngine.createProxy(this.vertx);
          for (final var taskId : taskIds) {

            profileManager.taskDeleted(taskId).onComplete(profileManagerNotified -> {

              if (profileManagerNotified.failed()) {

                Logger.trace(profileManagerNotified.cause(),
                    "Cannot notify the profile manager that the task {} has been deleted.", taskId);
              }
            });

            interactionProtocolEngine.taskDeleted(taskId).onComplete(interactionProtocolEngineNotified -> {

              if (interactionProtocolEngineNotified.failed()) {

                Logger.trace(interactionProtocolEngineNotified.cause(),
                    "Cannot notify the interaction protocol engine that the task {} has been deleted.", taskId);
              }
            });

          }
        }

      }

    });

    TasksRepository.createProxy(this.vertx).deleteAllTransactionByActioneer(profileId).onComplete(deleted -> {

      if (deleted.failed()) {

        Logger.trace(deleted.cause(), "Cannot delete all the transactions that has been done by {}.", profileId);
      }

    });

    TasksRepository.createProxy(this.vertx).deleteAllMessagesWithReceiver(profileId).onComplete(deleted -> {

      if (deleted.failed()) {

        Logger.trace(deleted.cause(), "Cannot delete all the messages that has been received by {}.", profileId);
      }

    });

  }

}
