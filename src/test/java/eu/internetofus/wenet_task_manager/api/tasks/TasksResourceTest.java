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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.components.profile_manager.WeNetProfileManagerMocker;
import eu.internetofus.common.components.service.WeNetService;
import eu.internetofus.common.components.service.WeNetServiceMocker;
import eu.internetofus.common.components.service.WeNetServiceSimulator;
import eu.internetofus.common.components.task_manager.Task;
import eu.internetofus.common.components.task_manager.TaskGoalTest;
import eu.internetofus.common.components.task_manager.TaskTest;
import eu.internetofus.common.components.task_manager.TaskType;
import eu.internetofus.common.components.task_manager.TaskTypeTest;
import eu.internetofus.common.components.task_manager.WeNetTaskManager;
import eu.internetofus.common.components.task_manager.WeNetTaskManagerMocker;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link TasksResource}.
 *
 * @see TasksResource
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TasksResourceTest {

  /**
   * The profile manager mocked server.
   */
  protected static WeNetProfileManagerMocker profileManagerMocker;

  /**
   * The task manager mocked server.
   */
  protected static WeNetTaskManagerMocker taskManagerMocker;

  /**
   * The service mocked server.
   */
  protected static WeNetServiceMocker serviceMocker;

  /**
   * Start the mocker server.
   */
  @BeforeAll
  public static void startMockers() {

    profileManagerMocker = WeNetProfileManagerMocker.start();
    taskManagerMocker = WeNetTaskManagerMocker.start();
    serviceMocker = WeNetServiceMocker.start();
  }

  /**
   * Register the necessary services before to test.
   *
   * @param vertx event bus to register the necessary services.
   */
  @BeforeEach
  public void registerServices(final Vertx vertx) {

    final WebClient client = WebClient.create(vertx);
    final JsonObject profileConf = profileManagerMocker.getComponentConfiguration();
    WeNetProfileManager.register(vertx, client, profileConf);

    final JsonObject taskConf = taskManagerMocker.getComponentConfiguration();
    WeNetTaskManager.register(vertx, client, taskConf);

    final JsonObject conf = serviceMocker.getComponentConfiguration();
    WeNetService.register(vertx, client, conf);
    WeNetServiceSimulator.register(vertx, client, conf);

  }

  /**
   * Create a resource where the repository is a mocked class.
   *
   * @return the created class with the mocked repository.
   */
  public static TasksResource createTasksResource() {

    final TasksResource resource = new TasksResource();
    resource.repository = mock(TasksRepository.class);
    resource.typesRepository = mock(TaskTypesRepository.class);
    return resource;

  }

  /**
   * Check fail create task because repository can not store it.
   *
   * @param vertx       event bus to use.
   * @param testContext test context.
   */
  @Test
  public void shouldFailCreateTaskBecasueRepositoryFailsToStore(final Vertx vertx, final VertxTestContext testContext) {

    new TaskTest().createModelExample(1, vertx, testContext, testContext.succeeding(task -> {

      final TasksResource resource = createTasksResource();
      final OperationRequest context = mock(OperationRequest.class);
      resource.createTask(task.toJsonObject(), context, testContext.succeeding(create -> {

        assertThat(create.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        testContext.completeNow();
      }));

      @SuppressWarnings("unchecked")
      final ArgumentCaptor<Handler<AsyncResult<Task>>> storeHandler = ArgumentCaptor.forClass(Handler.class);
      verify(resource.repository, times(1)).storeTask(any(), storeHandler.capture());
      storeHandler.getValue().handle(Future.failedFuture("Search task error"));

    }));
  }

  /**
   * Check fail update task because repository can not update it.
   *
   * @param vertx       event bus to use.
   * @param testContext test context.
   */
  @Test
  public void shouldFailUpdateTaskBecasueRepositoryFailsToUpdate(final Vertx vertx, final VertxTestContext testContext) {

    StoreServices.storeTaskExample(2, vertx, testContext, testContext.succeeding(created -> {

      final TasksResource resource = createTasksResource();
      final OperationRequest context = mock(OperationRequest.class);
      final Task source = new Task();
      source.goal = new TaskGoalTest().createModelExample(1);
      resource.updateTask("taskId", source.toJsonObject(), context, testContext.succeeding(update -> {

        assertThat(update.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        testContext.completeNow();
      }));

      @SuppressWarnings("unchecked")
      final ArgumentCaptor<Handler<AsyncResult<Task>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
      verify(resource.repository, times(1)).searchTask(any(), searchHandler.capture());
      searchHandler.getValue().handle(Future.succeededFuture(created));
      @SuppressWarnings("unchecked")
      final ArgumentCaptor<Handler<AsyncResult<Void>>> updateHandler = ArgumentCaptor.forClass(Handler.class);
      verify(resource.repository, times(1)).updateTask(any(Task.class), updateHandler.capture());
      updateHandler.getValue().handle(Future.failedFuture("Update task  error"));

    }));

  }

  /**
   * Check fail create task type because typesRepository can not store it.
   *
   * @param vertx       event bus to use.
   * @param testContext test context.
   */
  @Test
  public void shouldFailCreateTaskTypeBecasuetypesRepositoryFailsToStore(final Vertx vertx, final VertxTestContext testContext) {

    new TaskTest().createModelExample(1, vertx, testContext, testContext.succeeding(task -> {
      final TasksResource resource = createTasksResource();
      final OperationRequest context = mock(OperationRequest.class);
      resource.createTaskType(task.toJsonObject(), context, testContext.succeeding(create -> {

        assertThat(create.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        testContext.completeNow();
      }));

      @SuppressWarnings("unchecked")
      final ArgumentCaptor<Handler<AsyncResult<TaskType>>> storeHandler = ArgumentCaptor.forClass(Handler.class);
      verify(resource.typesRepository, times(1)).storeTaskType(any(), storeHandler.capture());
      storeHandler.getValue().handle(Future.failedFuture("Store task type error"));

    }));

  }

  /**
   * Check fail merge task type because typesRepository can not merge it.
   *
   * @param testContext test context.
   */
  @Test
  public void shouldFailMergeTaskTypeBecasuetypesRepositoryFailsToMerge(final VertxTestContext testContext) {

    final TasksResource resource = createTasksResource();
    final OperationRequest context = mock(OperationRequest.class);
    final TaskType source = new TaskType();
    source.description = "New description";
    resource.mergeTaskType("taskTypeId", source.toJsonObject(), context, testContext.succeeding(merge -> {

      assertThat(merge.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
      testContext.completeNow();
    }));

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<TaskType>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
    verify(resource.typesRepository, times(1)).searchTaskType(any(), searchHandler.capture());
    searchHandler.getValue().handle(Future.succeededFuture(new TaskTypeTest().createModelExample(1)));
    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Handler<AsyncResult<Void>>> mergeHandler = ArgumentCaptor.forClass(Handler.class);
    verify(resource.typesRepository, times(1)).updateTaskType(any(TaskType.class), mergeHandler.capture());
    mergeHandler.getValue().handle(Future.failedFuture("Merge task type error"));

  }

}
