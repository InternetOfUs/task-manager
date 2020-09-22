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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.common.components.task_manager.Task;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * Unit test to increases coverage of the {@link TasksRepository}
 *
 * @see TasksRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TasksRepositoryTest {

  /**
   * Verify that can not found a task because that returned by repository is not right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundTaskBecauseReturnedJsonObjectIsNotRight(final VertxTestContext testContext) {

    final TasksRepository repository = new TasksRepositoryImpl(null,null) {

      @Override
      public void searchTaskObject(final String id, final Handler<AsyncResult<JsonObject>> searchHandler) {

        searchHandler.handle(Future.succeededFuture(new JsonObject().put("key", "value")));

      }
    };

    repository.searchTask("any identifier", testContext.failing(fail -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can not store a task because that returned by repository is not right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task, Handler)
   */
  @Test
  public void shouldNotStoreTaskBecauseReturnedJsonObjectIsNotRight(final VertxTestContext testContext) {

    final TasksRepository repository = new TasksRepositoryImpl(null,null) {

      @Override
      public void storeTask(final JsonObject task, final Handler<AsyncResult<JsonObject>> storeHandler) {

        storeHandler.handle(Future.succeededFuture(new JsonObject().put("key", "value")));
      }
    };

    repository.storeTask(new Task(), testContext.failing(fail -> {
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can not store a task because that returned by repository is not right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task, Handler)
   */
  @Test
  public void shouldNotStoreTaskBecauseStoreFailed(final VertxTestContext testContext) {

    final Throwable cause = new IllegalArgumentException("Cause that can not be stored");
    final TasksRepository repository = new TasksRepositoryImpl(null,null) {

      @Override
      public void storeTask(final JsonObject task, final Handler<AsyncResult<JsonObject>> storeHandler) {

        storeHandler.handle(Future.failedFuture(cause));
      }

    };

    repository.storeTask(new Task(), testContext.failing(fail -> {
      assertThat(fail).isEqualTo(cause);
      testContext.completeNow();
    }));

  }

  /**
   * Verify that can not update a task because that returned by repository is not right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskBecauseUpdateFailed(final VertxTestContext testContext) {

    final Throwable cause = new IllegalArgumentException("Cause that can not be updated");
    final TasksRepository repository = new TasksRepositoryImpl(null,null) {

      @Override
      public void updateTask(final JsonObject task, final Handler<AsyncResult<Void>> updateHandler) {

        updateHandler.handle(Future.failedFuture(cause));
      }
    };

    repository.updateTask(new Task(), testContext.failing(fail -> {

      assertThat(fail).isEqualTo(cause);
      testContext.completeNow();
    }));

  }

}
