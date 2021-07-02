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

package eu.internetofus.wenet_task_manager.api;

import eu.internetofus.common.components.task_manager.WeNetTaskManager;
import eu.internetofus.common.components.task_manager.WeNetTaskManagerClient;
import eu.internetofus.common.vertx.AbstractAPIVerticle;
import eu.internetofus.common.vertx.AbstractServicesVerticle;
import eu.internetofus.wenet_task_manager.api.help.Help;
import eu.internetofus.wenet_task_manager.api.help.HelpResource;
import eu.internetofus.wenet_task_manager.api.messages.Messages;
import eu.internetofus.wenet_task_manager.api.messages.MessagesResource;
import eu.internetofus.wenet_task_manager.api.task_transactions.TaskTransactions;
import eu.internetofus.wenet_task_manager.api.task_transactions.TaskTransactionsResource;
import eu.internetofus.wenet_task_manager.api.task_types.TaskTypes;
import eu.internetofus.wenet_task_manager.api.task_types.TaskTypesResource;
import eu.internetofus.wenet_task_manager.api.tasks.Tasks;
import eu.internetofus.wenet_task_manager.api.tasks.TasksResource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * The verticle that provide the manage the WeNet task manager API.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class APIVerticle extends AbstractAPIVerticle {

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getOpenAPIResourcePath() {

    return "wenet-task_manager-openapi.yaml";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void mountServiceInterfaces(final RouterBuilder routerFactory) {

    routerFactory.mountServiceInterface(Help.class, Help.ADDRESS);
    new ServiceBinder(this.vertx).setAddress(Help.ADDRESS).register(Help.class, new HelpResource(this));

    routerFactory.mountServiceInterface(Tasks.class, Tasks.ADDRESS);
    new ServiceBinder(this.vertx).setAddress(Tasks.ADDRESS).register(Tasks.class, new TasksResource(this.vertx));

    routerFactory.mountServiceInterface(TaskTypes.class, TaskTypes.ADDRESS);
    new ServiceBinder(this.vertx).setAddress(TaskTypes.ADDRESS).register(TaskTypes.class,
        new TaskTypesResource(this.vertx));

    routerFactory.mountServiceInterface(TaskTransactions.class, TaskTransactions.ADDRESS);
    new ServiceBinder(this.vertx).setAddress(TaskTransactions.ADDRESS).register(TaskTransactions.class,
        new TaskTransactionsResource(this.vertx));

    routerFactory.mountServiceInterface(Messages.class, Messages.ADDRESS);
    new ServiceBinder(this.vertx).setAddress(Messages.ADDRESS).register(Messages.class,
        new MessagesResource(this.vertx));

  }

  /**
   * Register the services provided by the API.
   *
   * {@inheritDoc}
   *
   * @see WeNetTaskManager
   */
  @Override
  protected void startedServerAt(final String host, final int port) {

    final var conf = new JsonObject();
    conf.put(WeNetTaskManagerClient.TASK_MANAGER_CONF_KEY, "http://" + host + ":" + port);
    final var client = AbstractServicesVerticle.createWebClientSession(this.getVertx(), this.config());
    WeNetTaskManager.register(this.vertx, client, conf);

  }

}
