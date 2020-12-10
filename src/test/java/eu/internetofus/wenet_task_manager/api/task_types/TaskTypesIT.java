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

package eu.internetofus.wenet_task_manager.api.task_types;

import static eu.internetofus.common.vertx.HttpResponses.assertThatBodyIs;
import static io.reactiverse.junit5.web.TestRequest.queryParam;
import static io.reactiverse.junit5.web.TestRequest.testRequest;
import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.common.components.ErrorMessage;
import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.ValidationsTest;
import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.components.task_manager.TaskTypeTest;
import eu.internetofus.common.vertx.AbstractModelResourcesIT;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.api.tasks.Tasks;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

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
    model.name = ValidationsTest.STRING_256;
    return model;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void createValidModelExample(final int index, final Vertx vertx, final VertxTestContext testContext, final Handler<AsyncResult<TaskType>> createHandler) {

    createHandler.handle(Future.succeededFuture(new TaskTypeTest().createModelExample(index)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void storeModel(final TaskType source, final Vertx vertx, final VertxTestContext testContext, final Handler<AsyncResult<TaskType>> succeeding) {

    StoreServices.storeTaskType(source, vertx, testContext, succeeding);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void assertThatCreatedEquals(final TaskType source, final TaskType target) {

    source.id = target.id;
    source._creationTs = target._creationTs;
    source._lastUpdateTs = target._lastUpdateTs;
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
   * @see Tasks#createTask(io.vertx.core.json.JsonObject, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotStoreEmptyTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    final var taskType = new TaskType();
    testRequest(client, HttpMethod.POST, this.modelPath()).expect(res -> {

      assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      final var error = assertThatBodyIs(ErrorMessage.class, res);
      assertThat(error.code).isNotEmpty();
      assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

    }).sendJson(taskType.toJsonObject(), testContext);

  }

  /**
   * Verify that only update the task type name.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldMergeOnlyNameOnTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

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
    }));

  }

  /**
   * Verify that not merge the task type because it not produce any change.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskType(String, io.vertx.ext.web.api.OperationRequest, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotMergeBecasueNotChangedOnTaskType(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

      final var source = new TaskType();
      testRequest(client, HttpMethod.PATCH, this.modelPath() + "/" + target.id).expect(res -> testContext.verify(() -> {

        assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        final var error = assertThatBodyIs(ErrorMessage.class, res);
        assertThat(error.code).isNotEmpty().isEqualTo("task_type_to_merge_equal_to_original");
        assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

      })).sendJson(source.toJsonObject(), testContext);
    }));

  }

  /**
   * Verify that not retrieve task type page because the order is not right.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskTypesPage(String, String, java.util.List, java.util.List, int, int,
   *      io.vertx.ext.web.api.OperationRequest, Handler)
   */
  @Test
  public void shouldNotRetrieveTaskTypesPageBecauseBadOrder(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

      testRequest(client, HttpMethod.GET, this.modelPath()).with(queryParam("order", "name,-undefined")).expect(res -> testContext.verify(() -> {

        assertThat(res.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        final var error = assertThatBodyIs(ErrorMessage.class, res);
        assertThat(error).isNotNull();
        assertThat(error.code).isNotEmpty().isEqualTo("bad_order[1]");
        assertThat(error.message).isNotEmpty().isNotEqualTo(error.code);

      })).send(testContext);
    }));

  }

  /**
   * Verify that retrieve task type page.
   *
   * @param vertx       event bus to use.
   * @param client      to connect to the server.
   * @param testContext context to test.
   *
   * @see TaskTypes#retrieveTaskTypesPage(String, String, java.util.List, java.util.List, int, int,
   *      io.vertx.ext.web.api.OperationRequest, Handler)
   */
  @Test
  public void shouldRetrieveTaskTypesPage(final Vertx vertx, final WebClient client, final VertxTestContext testContext) {

    StoreServices.storeTaskTypeExample(1, vertx, testContext, testContext.succeeding(target -> {

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
    }));

  }

}
