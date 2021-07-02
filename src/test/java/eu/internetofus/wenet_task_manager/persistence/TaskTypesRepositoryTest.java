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

package eu.internetofus.wenet_task_manager.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import eu.internetofus.common.model.ValidationErrorException;
import eu.internetofus.common.components.models.TaskType;
import eu.internetofus.common.vertx.ModelsPageContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

/**
 * Unit test to increases coverage of the {@link TaskTypesRepository}
 *
 * @see TaskTypesRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TaskTypesRepositoryTest {

  /**
   * Verify that can not create task type profiles page sort.
   *
   * @see TaskTypesRepository#createTaskTypesPageSort(List)
   */
  @Test
  public void shouldFailCreateTaskTypesPageSort() {

    final List<String> order = new ArrayList<>();
    order.add("-undefinedKey");
    assertThatThrownBy(() -> {
      TaskTypesRepository.createTaskTypesPageSort(order);
    }).isInstanceOf(ValidationErrorException.class);

  }

  /**
   * Verify that can not create task type profiles page sort.
   *
   * @see TaskTypesRepository#createTaskTypesPageSort(List)
   */
  @Test
  public void shouldCreateTaskTypesPageSort() {

    final List<String> order = new ArrayList<>();
    order.add("+description");
    order.add("name");
    order.add("-keywords");
    final var sort = TaskTypesRepository.createTaskTypesPageSort(order);
    assertThat(sort).isNotNull();
    assertThat(sort.getInteger("name")).isNotNull().isEqualTo(1);
    assertThat(sort.getInteger("description")).isNotNull().isEqualTo(1);
    assertThat(sort.getInteger("keywords")).isNotNull().isEqualTo(-1);

  }

  /**
   * Should not obtain task type if the obtainer object not match a
   * {@link TaskType}.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#searchTaskType(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldFailSearchTaskTypeWhenFoundObjectNotMatch(final VertxTestContext testContext) {

    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    testContext.assertFailure(repository.searchTaskType("id")).onFailure(error -> testContext.completeNow());

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).searchTaskType(any(), searchHandler.capture());
    searchHandler.getValue().handle(Future.succeededFuture(new JsonObject().put("udefinedKey", "value")));

  }

  /**
   * Should not store task type if the stored object not match a {@link TaskType}.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#storeTaskType(TaskType)
   */
  @Test
  public void shouldFailStoreTaskTypeWhenStoredObjectNotMatch(final VertxTestContext testContext) {

    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    testContext.assertFailure(repository.storeTaskType(new TaskType()).onFailure(error -> testContext.completeNow()));

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> storeHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).storeTaskType(any(JsonObject.class), storeHandler.capture());
    storeHandler.getValue().handle(Future.succeededFuture(new JsonObject().put("udefinedKey", "value")));

  }

  /**
   * Should not update task type because can not convert to an object.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#updateTaskType(TaskType)
   */
  @Test
  public void shouldFailUpdateTaskTypeBecauseNoObject(final VertxTestContext testContext) {

    final var taskType = new TaskType() {
      /**
       * {@inheritDoc}
       */
      @Override
      public JsonObject toJsonObjectWithEmptyValues() {

        return null;

      }
    };
    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    testContext.assertFailure(repository.updateTaskType(taskType)).onFailure(error -> testContext.completeNow());

  }

  /**
   * Should not update task type because can not convert to an object.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPage(ModelsPageContext)
   */
  @Test
  public void shouldFailRetrieveTaskTypesPageObjectWhenSearchFail(final VertxTestContext testContext) {

    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    final var context = new ModelsPageContext();
    context.query = TaskTypesRepository.createTaskTypesPageQuery("name", "description", null);
    context.sort = TaskTypesRepository.createTaskTypesPageSort(Arrays.asList("name", "-description"));
    context.offset = 3;
    context.limit = 11;
    testContext.assertFailure(repository.retrieveTaskTypesPage(context)).onFailure(error -> testContext.completeNow());

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).retrieveTaskTypesPage(eq(context.query), eq(context.sort),
        eq(context.offset), eq(context.limit), searchHandler.capture());
    searchHandler.getValue().handle(Future.failedFuture("Not found"));

  }

  /**
   * Should not update task type because the obtained object not match.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPage(ModelsPageContext)
   */
  @Test
  public void shouldFailRetrieveTaskTypesPageWhenObjectNotMatch(final VertxTestContext testContext) {

    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    final var context = new ModelsPageContext();
    context.query = TaskTypesRepository.createTaskTypesPageQuery("name", "description",
        Arrays.asList("keyword2", "keyword2"));
    context.sort = TaskTypesRepository.createTaskTypesPageSort(Arrays.asList("-name", "description"));
    context.offset = 23;
    context.limit = 100;
    testContext.assertFailure(repository.retrieveTaskTypesPage(context)).onFailure(error -> testContext.completeNow());

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).retrieveTaskTypesPage(eq(context.query), eq(context.sort),
        eq(context.offset), eq(context.limit), searchHandler.capture());
    searchHandler.getValue().handle(Future.succeededFuture(new JsonObject().put("udefinedKey", "value")));

  }

  /**
   * Should not update task type because the obtained object not found.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPage(ModelsPageContext)
   */
  @Test
  public void shouldFailRetrieveTaskTypesPageWhenObjectNotFound(final VertxTestContext testContext) {

    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    final var context = new ModelsPageContext();
    context.query = TaskTypesRepository.createTaskTypesPageQuery("name", "description",
        Arrays.asList("keyword2", "keyword2"));
    context.sort = TaskTypesRepository.createTaskTypesPageSort(Arrays.asList("-name", "description"));
    context.offset = 23;
    context.limit = 100;

    testContext.assertFailure(repository.retrieveTaskTypesPage(context)).onFailure(error -> testContext.completeNow());

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).retrieveTaskTypesPage(eq(context.query), eq(context.sort),
        eq(context.offset), eq(context.limit), searchHandler.capture());
    searchHandler.getValue().handle(Future.failedFuture("Not found"));

  }

}
