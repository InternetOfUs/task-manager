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

package eu.internetofus.common.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * The class used to interact with the WeNet interaction protocol engine.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ProxyGen
public interface WeNetInteractionProtocolEngineService {

	/**
	 * The address of this service.
	 */
	String ADDRESS = "wenet_common.service.InteractionProtocolEngine";

	/**
	 * Create a proxy of the {@link WeNetInteractionProtocolEngineService}.
	 *
	 * @param vertx where the service has to be used.
	 *
	 * @return the task.
	 */
	static WeNetInteractionProtocolEngineService createProxy(Vertx vertx) {

		return new WeNetInteractionProtocolEngineServiceVertxEBProxy(vertx, WeNetInteractionProtocolEngineService.ADDRESS);
	}

	/**
	 * Register this service.
	 *
	 * @param vertx  that contains the event bus to use.
	 * @param client to do HTTP requests to other services.
	 * @param conf   configuration to use.
	 */
	static void register(Vertx vertx, WebClient client, JsonObject conf) {

		new ServiceBinder(vertx).setAddress(WeNetInteractionProtocolEngineService.ADDRESS).register(
				WeNetInteractionProtocolEngineService.class, new WeNetInteractionProtocolEngineServiceImpl(client, conf));

	}

	/**
	 * Return a community. It is used to verify if the identifier of a WeNet user is
	 * right.
	 *
	 * @param id              identifier of the community to get.
	 * @param retrieveHandler handler to manage the retrieve process.
	 */
	void retrieveCommunity(String id, Handler<AsyncResult<JsonObject>> retrieveHandler);

	/**
	 * Create a community. ATTENTION: this is used only for testing purposes.
	 *
	 * @param community     to create.
	 * @param createHandler handler to manage the creation process.
	 */
	void createCommunity(JsonObject community, Handler<AsyncResult<JsonObject>> createHandler);

	/**
	 * Return a community. ATTENTION: this is used only for testing purposes.
	 *
	 * @param id            identifier of the community to get.
	 * @param deleteHandler handler to manage the delete process.
	 */
	void deleteCommunity(String id, Handler<AsyncResult<Void>> deleteHandler);

}
