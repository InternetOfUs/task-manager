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
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
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

package eu.internetofus.wenet_task_manager.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import eu.internetofus.common.components.ValidationErrorException;
import eu.internetofus.common.components.task_manager.TaskType;
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
   * @see TaskTypesRepository#storeTaskType(TaskType, Handler)
   */
  @Test
  public void shouldFailStoreTaskTypeWhenStoredObjectNotMatch(final VertxTestContext testContext) {

    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    repository.storeTaskType(new TaskType(), testContext.failing(error -> testContext.completeNow()));

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
   * @see TaskTypesRepository#updateTaskType(TaskType, Handler)
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
    final Handler<AsyncResult<Void>> handler = testContext.failing(error -> testContext.completeNow());
    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    repository.updateTaskType(taskType, handler);

  }

  /**
   * Should not update task type because can not convert to an object.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPageObject(eu.internetofus.common.vertx.ModelsPageContext,
   *      Handler)
   */
  @Test
  public void shouldFailRetrieveTaskTypesPageObjectWhenSearchFail(final VertxTestContext testContext) {

    final DummyTaskTypesRepository repository = spy(new DummyTaskTypesRepository());
    final var context = new ModelsPageContext();
    context.query = TaskTypesRepository.createTaskTypesPageQuery("name", "description", null);
    context.sort = TaskTypesRepository.createTaskTypesPageSort(Arrays.asList("name", "-description"));
    context.offset = 3;
    context.limit = 11;
    repository.retrieveTaskTypesPageObject(context, testContext.failing(error -> testContext.completeNow()));

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).retrieveTaskTypesPageObject(eq(context.query), eq(context.sort),
        eq(context.offset), eq(context.limit), searchHandler.capture());
    searchHandler.getValue().handle(Future.failedFuture("Not found"));

  }

  /**
   * Should not update task type because the obtained object not match.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPage(ModelsPageContext, Handler)
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
    repository.retrieveTaskTypesPage(context, testContext.failing(error -> testContext.completeNow()));

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).retrieveTaskTypesPageObject(eq(context.query), eq(context.sort),
        eq(context.offset), eq(context.limit), searchHandler.capture());
    searchHandler.getValue().handle(Future.succeededFuture(new JsonObject().put("udefinedKey", "value")));

  }

  /**
   * Should not update task type because the obtained object not found.
   *
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepository#retrieveTaskTypesPage(ModelsPageContext, Handler)
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
    repository.retrieveTaskTypesPage(context, testContext.failing(error -> testContext.completeNow()));

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<JsonObject>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(repository, timeout(30000).times(1)).retrieveTaskTypesPageObject(eq(context.query), eq(context.sort),
        eq(context.offset), eq(context.limit), searchHandler.capture());
    searchHandler.getValue().handle(Future.failedFuture("Not found"));

  }

}
