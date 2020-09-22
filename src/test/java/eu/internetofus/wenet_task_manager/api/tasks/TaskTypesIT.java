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

package eu.internetofus.wenet_task_manager.api.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;

import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.ValidationsTest;
import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.components.task_manager.TaskTypeTest;
import eu.internetofus.common.vertx.AbstractModelResourcesIT;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;

/**
 * The integration test over the {@link Tasks} when manage {@link TaskType}.
 *
 * @see Tasks
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

    return Tasks.PATH + Tasks.TYPES_PATH;
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

    final var model = new TaskTypeTest().createModelExample(index);
    createHandler.handle(Future.succeededFuture(model));

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
    if (source.norms != null && target.norms != null && source.norms.size() == target.norms.size()) {

      final var max = source.norms.size();
      for (var i = 0; i < max; i++) {

        source.norms.get(i).id = target.norms.get(i).id;
      }

    }
    assertThat(source).isEqualTo(target);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String idOf(final TaskType model) {

    return model.id;
  }

}
