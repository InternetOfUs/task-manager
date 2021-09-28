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

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Vertx;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import javax.ws.rs.core.Response.Status;
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

}
