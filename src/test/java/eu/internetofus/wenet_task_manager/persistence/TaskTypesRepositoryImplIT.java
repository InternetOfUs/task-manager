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

import eu.internetofus.common.components.Containers;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration test over the {@link TaskTypesRepositoryImpl}.
 *
 * @see TaskTypesRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TaskTypesRepositoryImplIT {

  /**
   * The JSON codification of a task on API {@code 0.5.0}.
   */
  public static final String TASK_TYPE_0_5_0 = "{\"name\":\"Eat together\",\"transactions\":[{\"label\":\"cancel\"},{\"label\":\"acceptToAttend\",\"description\":\"Accept to attend\",\"attributes\":[{\"name\":\"volunteerId\",\"description\":\"Identifier of the volunteer\",\"type\":\"string\"}]}],\"schema_version\":\"0.5.0\",\"id\":\"1\"}";

  /**
   * The JSON codification of a task on API {@code 0.6.0}.
   */
  public static final String TASK_TYPE_0_6_0 = "{\"name\":\"Eat together\",\"transactions\":{\"cancel\":{},\"acceptToAttend\":{\"description\":\"Accept to attend\",\"properties\":{\"volunteerId\":{\"description\":\"Identifier of the volunteer\",\"type\":\"string\"}}}},\"schema_version\":\"0.6.0\",\"id\":\"1\",\"_creationTs\":1590686833,\"_lastUpdateTs\":1590686920}";

  /**
   * Start a mongo container.
   */
  @BeforeAll
  public static void startMongoContainer() {

    Containers.status().startMongoContainer();
  }

  /**
   * Verify the conversion of the task to the O.6.0.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepositoryImpl#migrateTaskTypeTo_0_6_0()
   */
  @Test
  public void shouldMigrateTo_0_6_0(final Vertx vertx, final VertxTestContext testContext) {

    final var pool = MongoClient.createShared(vertx, Containers.status().getMongoDBConfig(), "TEST");
    final var task_type_0_5_0 = (JsonObject) Json.decodeValue(TASK_TYPE_0_5_0);
    task_type_0_5_0.remove("id");
    testContext.assertComplete(pool.insert(TaskTypesRepositoryImpl.TASK_TYPES_COLLECTION, task_type_0_5_0))
        .onSuccess(id -> {
          final var repository = new TaskTypesRepositoryImpl(vertx, pool, "0.6.0");
          testContext.assertComplete(repository.migrateTaskTypeTo_0_6_0()).onSuccess(any -> {
            pool.findOne(TaskTypesRepositoryImpl.TASK_TYPES_COLLECTION, new JsonObject().put("_id", id), null)
                .onComplete(testContext.succeeding(migratedTask -> {

                  final var task_type_0_6_0 = (JsonObject) Json.decodeValue(TASK_TYPE_0_6_0);
                  task_type_0_6_0.remove("id");
                  task_type_0_6_0.put("_id", id).put("_creationTs", migratedTask.getLong("_creationTs"))
                      .put("_lastUpdateTs", migratedTask.getLong("_lastUpdateTs"));

                  testContext.verify(() -> assertThat(migratedTask).isEqualTo(task_type_0_6_0));
                  testContext.completeNow();

                }));
          });
        });

  }

  /**
   * Verify the conversion of the task to the current version.
   *
   * @param vertx       event bus to use.
   * @param testContext context that executes the test.
   *
   * @see TaskTypesRepositoryImpl#migrateDocumentsToCurrentVersions()
   */
  @Test
  public void shouldMigrate0_5_0_ToLatest(final Vertx vertx, final VertxTestContext testContext) {

    final var pool = MongoClient.createShared(vertx, Containers.status().getMongoDBConfig(), "TEST");
    final var task_type_0_5_0 = (JsonObject) Json.decodeValue(TASK_TYPE_0_5_0);
    task_type_0_5_0.remove("id");
    testContext.assertComplete(pool.insert(TaskTypesRepositoryImpl.TASK_TYPES_COLLECTION, task_type_0_5_0))
        .onSuccess(id -> {
          final var repository = new TaskTypesRepositoryImpl(vertx, pool, "latest");
          testContext.assertComplete(repository.migrateDocumentsToCurrentVersions()).onSuccess(migrated -> {

            testContext.assertComplete(repository.searchTaskType(id)).onSuccess(task -> testContext.completeNow());

          });

        });

  }

}
