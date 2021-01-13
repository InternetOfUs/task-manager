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
 * Integration test over the {@link TasksRepositoryImpl}.
 *
 * @see TasksRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TasksRepositoryImplIT {

  /**
   * The JSON codification of a task on API {@code 0.5.0}.
   */
  public static final String TASK_0_5_0 = "{\"_creationTs\":1590686833,\"_lastUpdateTs\":1590686920,\"taskTypeId\":\"5ecff45b2078fb229cddda0b\",\"requesterId\":\"5ebd80ef829c2405a3064fbe\",\"appId\":\"1\",\"goal\":{\"name\":\"After covid Party\",\"description\":\"Celebrate a big party after we have won teh COVID deseaze.\"},\"startTs\":1609356600,\"endTs\":1609360200,\"deadlineTs\":1606935600,\"attributes\":{\"outcome\":\"cancelled\"},\"closeTs\":1590686920,\"schema_version\":\"0.5.0\",\"id\":\"5ecff4702078fb229cddda0c\"}";

  /**
   * The JSON codification of a task on API {@code 0.6.0}.
   */
  public static final String TASK_0_6_0 = "{\"_creationTs\":1590686833,\"_lastUpdateTs\":1590686920,\"taskTypeId\":\"5ecff45b2078fb229cddda0b\",\"requesterId\":\"5ebd80ef829c2405a3064fbe\",\"appId\":\"1\",\"goal\":{\"name\":\"After covid Party\",\"description\":\"Celebrate a big party after we have won teh COVID deseaze.\"},\"attributes\":{\"outcome\":\"cancelled\",\"startTs\":1609356600,\"endTs\":1609360200,\"deadlineTs\":1606935600},\"closeTs\":1590686920,\"schema_version\":\"0.6.0\",\"id\":\"5ecff4702078fb229cddda0c\"}";

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
   * @see TasksRepositoryImpl#migrateTaskTo_0_6_0()
   */
  @Test
  public void shouldMigrateTo_0_6_0(final Vertx vertx, final VertxTestContext testContext) {

    var pool = MongoClient.createShared(vertx, Containers.status().getMongoDBConfig(), "TEST");
    var task_0_5_0 = (JsonObject) Json.decodeValue(TASK_0_5_0);
    task_0_5_0.remove("id");
    testContext.assertComplete(pool.insert(TasksRepositoryImpl.TASKS_COLLECTION, task_0_5_0)).onSuccess(id -> {
      var repository = new TasksRepositoryImpl(pool, "0.6.0");
      testContext.assertComplete(repository.migrateTaskTo_0_6_0()
          .compose(empty -> pool.findOne(TasksRepositoryImpl.TASKS_COLLECTION, new JsonObject().put("_id", id), null))
          .onSuccess(migratedTask -> {

            var task_0_6_0 = (JsonObject) Json.decodeValue(TASK_0_6_0);
            task_0_6_0.remove("id");
            task_0_6_0 = task_0_6_0.put("_id", id);
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
   * @see TasksRepositoryImpl#migrateDocumentsToCurrentVersions()
   */
  @Test
  public void shouldMigrate0_5_0_ToLatest(final Vertx vertx, final VertxTestContext testContext) {

    var pool = MongoClient.createShared(vertx, Containers.status().getMongoDBConfig(), "TEST");
    var task_0_5_0 = (JsonObject) Json.decodeValue(TASK_0_5_0);
    task_0_5_0.remove("id");
    testContext.assertComplete(pool.insert(TasksRepositoryImpl.TASKS_COLLECTION, task_0_5_0)).onSuccess(id -> {
      var repository = new TasksRepositoryImpl(pool, "latest");
      testContext.assertComplete(repository.migrateDocumentsToCurrentVersions()).onSuccess(migrated -> {

        testContext.assertComplete(repository.searchTask(id)).onSuccess(task -> testContext.completeNow());

      });

    });

  }

}
