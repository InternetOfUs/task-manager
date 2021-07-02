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

import eu.internetofus.common.components.models.Task;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
   * Verify that can not found a task because that returned by repository is not
   * right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotFoundTaskBecauseReturnedJsonObjectIsNotRight(final VertxTestContext testContext) {

    final TasksRepository repository = new TasksRepositoryImpl(null, null) {

      @Override
      public void searchTask(final String id, final Handler<AsyncResult<JsonObject>> searchHandler) {

        searchHandler.handle(Future.succeededFuture(new JsonObject().put("key", "value")));

      }
    };

    testContext.assertFailure(repository.searchTask("any identifier")).onFailure(fail -> testContext.completeNow());

  }

  /**
   * Verify that can not store a task because that returned by repository is not
   * right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task)
   */
  @Test
  public void shouldNotStoreTaskBecauseReturnedJsonObjectIsNotRight(final VertxTestContext testContext) {

    final TasksRepository repository = new TasksRepositoryImpl(null, null) {

      @Override
      public void storeTask(final JsonObject task, final Handler<AsyncResult<JsonObject>> storeHandler) {

        storeHandler.handle(Future.succeededFuture(new JsonObject().put("key", "value")));
      }
    };

    testContext.assertFailure(repository.storeTask(new Task())).onFailure(error -> testContext.completeNow());

  }

  /**
   * Verify that can not store a task because that returned by repository is not
   * right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#storeTask(Task)
   */
  @Test
  public void shouldNotStoreTaskBecauseStoreFailed(final VertxTestContext testContext) {

    final Throwable cause = new IllegalArgumentException("Cause that can not be stored");
    final TasksRepository repository = new TasksRepositoryImpl(null, null) {

      @Override
      public void storeTask(final JsonObject task, final Handler<AsyncResult<JsonObject>> storeHandler) {

        storeHandler.handle(Future.failedFuture(cause));
      }

    };

    testContext.assertFailure(repository.storeTask(new Task())).onFailure(error -> testContext.verify(() -> {

      assertThat(error).isEqualTo(cause);
      testContext.completeNow();

    }));

  }

  /**
   * Verify that can not update a task because that returned by repository is not
   * right.
   *
   * @param testContext context that executes the test.
   *
   * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
   */
  @Test
  public void shouldNotUpdateTaskBecauseUpdateFailed(final VertxTestContext testContext) {

    final Throwable cause = new IllegalArgumentException("Cause that can not be updated");
    final TasksRepository repository = new TasksRepositoryImpl(null, null) {

      @Override
      public void updateTask(final JsonObject task, final Handler<AsyncResult<Void>> updateHandler) {

        updateHandler.handle(Future.failedFuture(cause));
      }
    };

    testContext.assertFailure(repository.updateTask(new Task())).onFailure(fail -> testContext.verify(() -> {

      assertThat(fail).isEqualTo(cause);
      testContext.completeNow();
    }));

  }

}
