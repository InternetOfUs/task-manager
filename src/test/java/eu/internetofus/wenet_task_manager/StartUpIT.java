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

package eu.internetofus.wenet_task_manager;

import static org.assertj.core.api.Assertions.assertThat;

import eu.internetofus.common.components.Containers;
import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.models.TaskType;
import eu.internetofus.common.components.task_manager.WeNetTaskManager;
import eu.internetofus.common.protocols.DefaultProtocols;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepositoryImpl;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test the start up of the profile manager.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class StartUpIT {

  /**
   * Check that the migration process do not affect the defined task types.
   *
   * @param vertx       event bus to use.
   * @param testContext context used on the test.
   * @param pool        database client.
   */
  @Test
  public void shouldMigrationnotAffectTaskTypes(final Vertx vertx, final VertxTestContext testContext,
      final MongoClient pool) {

    @SuppressWarnings("rawtypes")
    final List<Future> stored = new ArrayList<>();
    final List<TaskType> defined = new ArrayList<>();
    for (final var protocol : DefaultProtocols.values()) {

      stored.add(protocol.load(vertx).compose(taskType -> {

        taskType.id = null;
        return StoreServices.storeTaskType(taskType, vertx, testContext).compose(storedTaskType -> {

          defined.add(storedTaskType);
          return pool.insert(TaskTypesRepositoryImpl.TASK_TYPES_COLLECTION, taskType.toJsonObject()).compose(id -> {

            taskType.id = id;
            defined.add(taskType);
            return Future.succeededFuture();
          });
        });

      }));

    }
    testContext.assertComplete(CompositeFuture.all(stored)).onSuccess(any -> {

      testContext.assertComplete(Containers.defaultEffectiveConfiguration(vertx)).onSuccess(config -> {

        final var schemaVersion = config.getJsonObject("help", new JsonObject()).getJsonObject("info", new JsonObject())
            .getString("apiVersion", "Undefined");
        testContext
            .assertComplete(new TaskTypesRepositoryImpl(vertx, pool, schemaVersion).migrateDocumentsToCurrentVersions())
            .onSuccess(ignored -> {

              @SuppressWarnings("rawtypes")
              final List<Future> toVerify = new ArrayList<>();
              for (final var taskType : defined) {

                WeNetTaskManager.createProxy(vertx).retrieveTaskType(taskType.id).compose(found -> {

                  testContext.verify(() -> assertThat(found).isEqualTo(taskType));
                  return Future.succeededFuture();
                });
              }

              testContext.assertComplete(CompositeFuture.all(toVerify))
                  .onSuccess(allRight -> testContext.completeNow());
            });
      });
    });

  }

}
