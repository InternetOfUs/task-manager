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

package eu.internetofus.wenet_task_manager.api.taskTypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import eu.internetofus.common.api.models.wenet.TaskType;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link TaskTypesResource}.
 *
 * @see TaskTypesResource
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class TaskTypesResourceTest {

	/**
	 * Create a resource where the repository is a mocked class.
	 *
	 * @return the created class with the mocked repository.
	 */
	public static TaskTypesResource createTaskTypesResource() {

		final TaskTypesResource resource = new TaskTypesResource(Vertx.vertx());
		resource.repository = mock(TaskTypesRepository.class);
		return resource;

	}

	/**
	 * Check fail create task type because repository can not store it.
	 *
	 * @param testContext test context.
	 */
	@Test
	public void shouldFailCreateTaskTypeBecasueRepositoryFailsToStore(VertxTestContext testContext) {

		final TaskTypesResource resource = createTaskTypesResource();
		final OperationRequest context = mock(OperationRequest.class);
		resource.createTaskType(new JsonObject(), context, testContext.succeeding(create -> {

			assertThat(create.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			testContext.completeNow();
		}));

		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<TaskType>>> storeHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.repository, times(1)).storeTaskType(any(), storeHandler.capture());
		storeHandler.getValue().handle(Future.failedFuture("Store task type error"));

	}

	/**
	 * Check fail update task type because repository can not update it.
	 *
	 * @param testContext test context.
	 */
	@Test
	public void shouldFailUpdateTaskTypeBecasueRepositoryFailsToUpdate(VertxTestContext testContext) {

		final TaskTypesResource resource = createTaskTypesResource();
		final OperationRequest context = mock(OperationRequest.class);
		final TaskType source = new TaskType();
		source.description = "New description";
		resource.updateTaskType("taskTypeId", source.toJsonObject(), context, testContext.succeeding(update -> {

			assertThat(update.getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
			testContext.completeNow();
		}));

		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<TaskType>>> searchHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.repository, times(1)).searchTaskType(any(), searchHandler.capture());
		searchHandler.getValue().handle(Future.succeededFuture(new TaskType()));
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<Handler<AsyncResult<Void>>> updateHandler = ArgumentCaptor.forClass(Handler.class);
		verify(resource.repository, times(1)).updateTaskType(any(TaskType.class), updateHandler.capture());
		updateHandler.getValue().handle(Future.failedFuture("Update task type error"));

	}

}
