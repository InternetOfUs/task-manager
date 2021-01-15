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

import eu.internetofus.common.Containers;
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
   * @see TaskTypesRepositoryImpl#migrateTaskTo_0_6_0()
   */
  @Test
  public void shouldMigrateTo_0_6_0(final Vertx vertx, final VertxTestContext testContext) {

    var pool = MongoClient.createShared(vertx, Containers.status().getMongoDBConfig(), "TEST");
    var task_0_5_0 = (JsonObject) Json.decodeValue(TASK_TYPE_0_5_0);
    task_0_5_0.remove("id");
    testContext.assertComplete(pool.insert(TaskTypesRepositoryImpl.TASK_TYPES_COLLECTION, task_0_5_0)).onSuccess(id -> {
      var repository = new TaskTypesRepositoryImpl(vertx, pool, "0.6.0");
      testContext.assertComplete(repository.migrateTaskTo_0_6_0().compose(
          empty -> pool.findOne(TaskTypesRepositoryImpl.TASK_TYPES_COLLECTION, new JsonObject().put("_id", id), null))
          .onSuccess(migratedTask -> {

            var task_0_6_0 = (JsonObject) Json.decodeValue(TASK_TYPE_0_6_0);
            task_0_6_0.remove("id");
            task_0_6_0 = task_0_6_0.put("_id", id).put("_creationTs", migratedTask.getLong("_creationTs"))
                .put("_lastUpdateTs", migratedTask.getLong("_lastUpdateTs"));

            assertThat(migratedTask).isEqualTo(task_0_6_0);
            testContext.completeNow();

          }));

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

    var pool = MongoClient.createShared(vertx, Containers.status().getMongoDBConfig(), "TEST");
    var task_0_5_0 = (JsonObject) Json.decodeValue(TASK_TYPE_0_5_0);
    task_0_5_0.remove("id");
    testContext.assertComplete(pool.insert(TaskTypesRepositoryImpl.TASK_TYPES_COLLECTION, task_0_5_0)).onSuccess(id -> {
      var repository = new TaskTypesRepositoryImpl(vertx, pool, "latest");
      testContext.assertComplete(repository.migrateDocumentsToCurrentVersions()).onSuccess(migrated -> {

        testContext.assertComplete(repository.searchTaskType(id)).onSuccess(task -> testContext.completeNow());

      });

    });

  }

}
