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

package eu.internetofus.wenet_task_manager.services;

import eu.internetofus.common.components.interaction_protocol_engine.WeNetInteractionProtocolEngine;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.components.service.WeNetService;
import eu.internetofus.common.vertx.AbstractServicesVerticle;
import io.vertx.core.json.JsonObject;

/**
 * The verticle that provide the services to interact with the other WeNet modules.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class ServicesVerticle extends AbstractServicesVerticle {

  /**
   * {@inheritDoc}
   */
  @Override
  protected void registerServices(final JsonObject serviceConf) throws Exception {

    // register the service to interact with the profile manager
    WeNetProfileManager.register(this.vertx, this.client, serviceConf);

    // register the service to interact with the service API
    WeNetService.register(this.vertx, this.client, serviceConf);

    // register the service to interact with the interaction protocol engine
    WeNetInteractionProtocolEngine.register(this.vertx, this.client, serviceConf);

  }

}
