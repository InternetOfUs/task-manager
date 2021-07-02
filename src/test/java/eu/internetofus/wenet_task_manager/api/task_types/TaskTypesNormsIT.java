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

import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.models.ProtocolNorm;
import eu.internetofus.common.components.models.ProtocolNormTest;
import eu.internetofus.common.components.models.TaskType;
import eu.internetofus.common.components.models.TaskTypeTest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Check the manipulation of the personal behaviors ({@link ProtocolNorm}) in a
 * {@link TaskType}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TaskTypesNormsIT extends AbstractTaskTypeFieldResourcesIT<ProtocolNorm, Integer> {

  /**
   * {@inheritDoc}
   */
  @Override
  protected String fieldPath() {

    return "/norms";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Future<ProtocolNorm> createValidModelFieldElementExample(final int index, final Vertx vertx,
      final VertxTestContext testContext) {

    return Future.succeededFuture(new ProtocolNormTest().createModelExample(index));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProtocolNorm createInvalidModelFieldElement() {

    final var element = new ProtocolNormTest().createModelExample(0);
    element.whenever = element.thenceforth;
    return element;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<ProtocolNorm> fieldOf(final TaskType model) {

    return model.norms;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Future<TaskType> storeValidExampleModelWithFieldElements(final int index, final Vertx vertx,
      final VertxTestContext testContext) {

    return testContext
        .assertComplete(new TaskTypeTest().createModelExample(index, vertx, testContext).compose(tasktype -> {

          tasktype.id = null;
          tasktype.norms = new ArrayList<>();
          tasktype.norms.add(new ProtocolNormTest().createModelExample(index));
          return StoreServices.storeTaskType(tasktype, vertx, testContext);

        }));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Future<TaskType> storeValidExampleModelWithNullField(final int index, final Vertx vertx,
      final VertxTestContext testContext) {

    return testContext
        .assertComplete(new TaskTypeTest().createModelExample(index, vertx, testContext).compose(tasktype -> {
          tasktype.id = null;
          tasktype.norms = null;
          return StoreServices.storeTaskType(tasktype, vertx, testContext);
        }));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void assertEqualsAdded(final ProtocolNorm source, final ProtocolNorm target) {

    assertThat(source).isEqualTo(target);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Integer idOfElementIn(final TaskType model, final ProtocolNorm element) {

    if (model.norms == null) {

      return -1;

    } else {

      return model.norms.indexOf(element);

    }

  }

}
