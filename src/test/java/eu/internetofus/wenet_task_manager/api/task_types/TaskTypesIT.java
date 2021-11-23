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

package eu.internetofus.wenet_task_manager.api.task_types;

import static eu.internetofus.common.vertx.HttpResponses.assertThatBodyIs;
import static io.reactiverse.junit5.web.TestRequest.queryParam;
import static io.reactiverse.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.models.TaskType;
import eu.internetofus.common.components.models.TaskTypeTest;
import eu.internetofus.common.components.task_manager.TaskTypesPage;
import eu.internetofus.common.model.ErrorMessage;
import eu.internetofus.common.model.Merges;
import eu.internetofus.common.vertx.AbstractModelResourcesIT;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.api.tasks.Tasks;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The integration test over the {@link TaskTypes}.
 *
 * @see TaskTypes
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TaskTypesIT extends AbstractModelResourcesIT<TaskType, String> {

  /**
   * {@inheritDoc}
   */
  @Override
  protected String modelPath() {

    return TaskTypes.PATH;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected TaskType createInvalidModel() {

    final var model = new TaskTypeTest().createModelExample(1);
    model.norms.get(0).whenever = model.norms.get(0).thenceforth;
    return model;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Future<TaskType> createValidModelExample(final int index, final Vertx vertx,
      final VertxTestContext testContext) {

    return new TaskTypeTest().createModelExample(index, vertx, testContext);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Future<TaskType> storeModel(final TaskType source, final Vertx vertx, final VertxTestContext testContext) {

    return StoreServices.storeTaskType(source, vertx, testContext);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void assertThatCreatedEquals(final TaskType source, final TaskType target) {

    source.id = target.id;
    source._creationTs = target._creationTs;
    source._lastUpdateTs = target._lastUpdateTs;
    source.attributes = Merges.mergeJsonObjects(target.attributes, source.attributes);
    source.transactions = Merges.mergeJsonObjects(target.transactions, source.transactions);
    source.callbacks = Merges.mergeJsonObjects(target.callbacks, source.callbacks);
    assertThat(source).isEqualTo(target);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String idOf(final TaskType model) {

    return model.id;
  }

  /**
   * Verify that store an empty task type.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see Tasks#createTask(io.vertx.core.json.JsonObject,
   *      io.vertx.ext.web.api.service.ServiceRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldStoreEmptyTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final var taskType = new TaskType();
    testRequest(client, HttpMethod.POST, this.modelPath()).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.CREATED.getStatusCode());
      final var created = assertThatBodyIs(TaskType.class, res);
      taskType.id = created.id;
      taskType._creationTs = taskType._lastUpdateTs = created._creationTs;
      assertThat(created).isEqualTo(taskType);

    }).sendJson(taskType.toJsonObject(), testContext);

  }

  /**
   * Verify that only update the task type name.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskType(String,
   *      io.vertx.ext.web.api.service.ServiceRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldMergeOnlyNameOnTaskType(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext).onSuccess(target -> {

      final var source = new TaskType();
      source.name = "NEW task type name";
      testRequest(client, HttpMethod.PATCH, this.modelPath() + "/" + target.id).expect(res -> testContext.verify(() -> {

        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final var updated = assertThatBodyIs(TaskType.class, res);
        assertThat(updated).isNotEqualTo(target).isNotEqualTo(source);
        target._lastUpdateTs = updated._lastUpdateTs;
        target.name = "NEW task type name";
        assertThat(updated).isEqualTo(target);

      })).sendJson(source.toJsonObject(), testContext);
    });

  }

  /**
   * Verify that not merge the task type because it not produce any change.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskType(String,
   *      io.vertx.ext.web.api.service.ServiceRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldMergeWithNotChangedOnTaskType(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext).onSuccess(target -> {

      final var source = new TaskType();
      testRequest(client, HttpMethod.PATCH, this.modelPath() + "/" + target.id).expect(res -> testContext.verify(() -> {

        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final var merged = assertThatBodyIs(TaskType.class, res);
        assertThat(merged).isEqualTo(target);

      })).sendJson(source.toJsonObject(), testContext);
    });

  }

  /**
   * Verify that not retrieve task type page because the order is not right.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskTypesPage(String, String, String, String, int,
   *      int, io.vertx.ext.web.api.service.ServiceRequest, Handler)
   */
  @Test
  public void shouldNotRetrieveTaskTypesPageBecauseBadOrder(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext).onSuccess(target -> {

      testRequest(client, HttpMethod.GET, this.modelPath()).with(queryParam("order", "name,-undefined"))
          .expect(res -> testContext.verify(() -> {

            assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
            final var error = assertThatBodyIs(ErrorMessage.class, res);
            assertThat(error).isNotNull();
            assertThat(error.code).isNotEmpty().isEqualTo("bad_order[1]");
            assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

          })).send(testContext);
    });

  }

  /**
   * Verify that retrieve task type page.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskTypesPage(String, String, String, String, int,
   *      int, io.vertx.ext.web.api.service.ServiceRequest, Handler)
   */
  @Test
  public void shouldRetrieveTaskTypesPage(final Vertx vertx, final WebClient client,
      final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext).onSuccess(target -> {

      testRequest(client, HttpMethod.GET, this.modelPath()).expect(res -> testContext.verify(() -> {

        assertThat(res.statusCode()).isEqualTo(Status.OK.getStatusCode());
        final var page = assertThatBodyIs(TaskTypesPage.class, res);
        assertThat(page).isNotNull();
        if (page.total > 0) {

          assertThat(page.taskTypes).isNotEmpty();

        } else {

          assertThat(page.taskTypes).isNullOrEmpty();
        }

      })).send(testContext);
    });

  }

}
