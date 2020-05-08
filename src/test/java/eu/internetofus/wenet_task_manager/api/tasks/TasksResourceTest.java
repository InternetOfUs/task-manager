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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import eu.internetofus.common.api.models.wenet.StoreServices;
import eu.internetofus.common.api.models.wenet.Task;
import eu.internetofus.common.api.models.wenet.TaskGoalTest;
import eu.internetofus.common.api.models.wenet.TaskTest;
import eu.internetofus.common.api.models.wenet.TaskType;
import eu.internetofus.common.api.models.wenet.TaskTypeTest;
import eu.internetofus.common.services.ServiceApiSimulatorServiceOnMemory;
import eu.internetofus.common.services.WeNetProfileManagerServiceOnMemory;
import eu.internetofus.common.services.WeNetTaskManagerServiceOnMemory;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationRequest;
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
	 * Register the necessary services before to test.
	 *
	 * @param vertx event bus to register the necessary services.
	 */
	@BeforeEach
	public void registerServices(Vertx vertx) {

		WeNetProfileManagerServiceOnMemory.register(vertx);
		WeNetTaskManagerServiceOnMemory.register(vertx);
		ServiceApiSimulatorServiceOnMemory.register(vertx);

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
	public void shouldFailCreateTaskBecasueRepositoryFailsToStore(Vertx vertx, VertxTestContext testContext) {

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
	public void shouldFailUpdateTaskBecasueRepositoryFailsToUpdate(Vertx vertx, VertxTestContext testContext) {

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
	public void shouldFailCreateTaskTypeBecasuetypesRepositoryFailsToStore(Vertx vertx, VertxTestContext testContext) {

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
	 * Check fail update task type because typesRepository can not update it.
	 *
	 * @param testContext test context.
	 */
	@Test
	public void shouldFailUpdateTaskTypeBecasuetypesRepositoryFailsToUpdate(VertxTestContext testContext) {

		final TasksResource resource = createTasksResource();
		final OperationRequest context = mock(OperationRequest.class);
		final TaskType source = new TaskType();
		source.description = "New description";
		resource.updateTaskType("taskTypeId", source.toJsonObject(), context, testContext.succeeding(update -> {

			assertThat(update.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			testContext.completeNow();
		}));

		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<TaskType>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.typesRepository, times(1)).searchTaskType(any(), searchHandler.capture());
		searchHandler.getValue().handle(Future.succeededFuture(new TaskTypeTest().createModelExample(1)));
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<Void>>> updateHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.typesRepository, times(1)).updateTaskType(any(TaskType.class), updateHandler.capture());
		updateHandler.getValue().handle(Future.failedFuture("Update task type error"));

	}

}
