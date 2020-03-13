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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import eu.internetofus.common.api.models.Model;
import eu.internetofus.wenet_task_manager.WeNetTaskManagerIntegrationExtension;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link TasksResource}.
 *
 * @see TasksResource
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class TasksResourceTest {

	/**
	 * Create a resource where the repository is a mocked class.
	 *
	 * @return the created class with the mocked repository.
	 */
	public static TasksResource createTasksResource() {

		final TasksResource resource = new TasksResource();
		resource.repository = mock(TasksRepository.class);
		return resource;

	}

	/**
	 * Check fail create task because repository can not store it.
	 *
	 * @param testContext test context.
	 */
	@Test
	public void shouldFailCreateTaskBecasueRepositoryFailsToStore(VertxTestContext testContext) {

		final TasksResource resource = createTasksResource();
		final OperationRequest context = mock(OperationRequest.class);
		resource.createTask(new JsonObject(), context, testContext.succeeding(create -> {

			assertThat(create.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			testContext.completeNow();
		}));

		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<Task>>> storeHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.repository, times(1)).storeTask(any(), storeHandler.capture());
		storeHandler.getValue().handle(Future.failedFuture("Search task error"));

	}

	/**
	 * Check fail update task because repository can not update it.
	 *
	 * @param testContext test context.
	 */
	@Test
	public void shouldFailUpdateTaskBecasueRepositoryFailsToUpdate(VertxTestContext testContext) {

		final TasksResource resource = createTasksResource();
		final OperationRequest context = mock(OperationRequest.class);
		resource.updateTask("taskId", new JsonObject().put("state", TaskState.Cancelled), context,
				testContext.succeeding(update -> {

					assertThat(update.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
					testContext.completeNow();
				}));

		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<Task>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.repository, times(1)).searchTask(any(), searchHandler.capture());
		searchHandler.getValue()
				.handle(Future.succeededFuture(Model.fromJsonObject(new JsonObject().put("taskId", "taskId"), Task.class)));
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<Void>>> updateHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.repository, times(1)).updateTask((Task) any(), updateHandler.capture());
		updateHandler.getValue().handle(Future.failedFuture("Update task error"));

	}

}
