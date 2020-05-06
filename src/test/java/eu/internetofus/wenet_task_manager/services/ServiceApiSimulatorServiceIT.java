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

package eu.internetofus.wenet_task_manager.services;

import static io.vertx.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.common.api.models.wenet.App;
import eu.internetofus.common.services.ServiceApiSimulatorService;
import eu.internetofus.common.services.ServiceApiSimulatorServiceTestCase;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

/**
 * Integration test over the {@link ServiceApiSimulatorService}.
 *
 * @see ServiceApiSimulatorService
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class ServiceApiSimulatorServiceIT extends ServiceApiSimulatorServiceTestCase {

	/**
	 * Should callback messages be captured.
	 *
	 * @param vertx       that contains the event bus to use.
	 * @param client      to use.
	 * @param testContext context over the tests.
	 */
	@Test
	public void shouldCaptureCallbackMessages(Vertx vertx, WebClient client, VertxTestContext testContext) {

		final ServiceApiSimulatorService service = ServiceApiSimulatorService.createProxy(vertx);
		service.createApp(new App(), testContext.succeeding(created -> {

			final JsonObject message1 = new JsonObject().put("key", "value1");
			testRequest(client.postAbs(created.messageCallbackUrl)).expect(res -> {

				assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
				final JsonObject body = new JsonObject(res.body());
				assertThat(body).isEqualTo(message1);
				final JsonObject message2 = new JsonObject().put("key", "value1");
				testRequest(client.postAbs(created.messageCallbackUrl)).expect(res2 -> {

					assertThat(res2.statusCode()).isEqualTo(Status.OK.getStatusCode());
					final JsonObject body2 = new JsonObject(res2.body());
					assertThat(body2).isEqualTo(message2);
					service.retrieveJsonCallbacks(created.appId, testContext.succeeding(retrieve -> testContext.verify(() -> {

						assertThat(retrieve)
								.isEqualTo(new JsonObject().put("messages", new JsonArray().add(message1).add(message2)));
						testContext.completeNow();

					})));

				}).sendJson(message2, testContext);

			}).sendJson(message1, testContext);

		}));

	}

}
