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

package eu.internetofus.wenet_task_manager.api;

import eu.internetofus.common.components.task_manager.WeNetTaskManager;
import eu.internetofus.common.components.task_manager.WeNetTaskManagerClient;
import eu.internetofus.common.vertx.AbstractAPIVerticle;
import eu.internetofus.wenet_task_manager.api.help.Help;
import eu.internetofus.wenet_task_manager.api.help.HelpResource;
import eu.internetofus.wenet_task_manager.api.tasks.Tasks;
import eu.internetofus.wenet_task_manager.api.tasks.TasksResource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.client.WebClient;
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
  protected void mountServiceInterfaces(final OpenAPI3RouterFactory routerFactory) {

    routerFactory.mountServiceInterface(Help.class, Help.ADDRESS);
    new ServiceBinder(this.vertx).setAddress(Help.ADDRESS).register(Help.class, new HelpResource(this));

    routerFactory.mountServiceInterface(Tasks.class, Tasks.ADDRESS);
    new ServiceBinder(this.vertx).setAddress(Tasks.ADDRESS).register(Tasks.class, new TasksResource(this.vertx));

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

    final JsonObject conf = new JsonObject();
    conf.put(WeNetTaskManagerClient.TASK_MANAGER_CONF_KEY, "http://" + host + ":" + port);
    final WebClient client = WebClient.create(this.vertx);
    WeNetTaskManager.register(this.vertx, client, conf);

  }

}
