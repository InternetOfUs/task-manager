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

import javax.ws.rs.core.Response.Status;

import org.tinylog.Logger;

import eu.internetofus.common.api.OperationReponseHandlers;
import eu.internetofus.common.api.models.Model;
import eu.internetofus.common.services.WeNetProfileManagerService;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

/**
 * Resource that provide the methods for the {@link Tasks}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TasksResource implements Tasks {

	/**
	 * The repository to manage the tasks.
	 */
	protected TasksRepository repository;

	/**
	 * The repository to manage the tasks.
	 */
	protected WeNetProfileManagerService profileManager;

	/**
	 * Create an empty resource. This is only used for unit tests.
	 */
	protected TasksResource() {

	}

	/**
	 * Create a new instance to provide the services of the {@link Tasks}.
	 *
	 * @param vertx where resource is defined.
	 */
	public TasksResource(Vertx vertx) {

		this.repository = TasksRepository.createProxy(vertx);
		this.profileManager = WeNetProfileManagerService.createProxy(vertx);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retrieveTask(String taskId, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.repository.searchTaskObject(taskId, search -> {

			final JsonObject task = search.result();
			if (task == null) {

				Logger.debug(search.cause(), "Not found task for {}", taskId);
				OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task",
						"Does not exist a task associated to '" + taskId + "'.");

			} else {

				OperationReponseHandlers.responseOk(resultHandler, task);

			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createTask(JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		final Task task = Model.fromJsonObject(body, Task.class);
		if (task == null) {

			Logger.debug("The {} is not a valid Task.", body);
			OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task",
					"The task is not right.");

		} else {

			task.validate("bad_task", this.profileManager).setHandler(validation -> {

				if (validation.failed()) {

					final Throwable cause = validation.cause();
					Logger.debug(cause, "The {} is not valid.", task);
					OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

				} else {

					this.repository.storeTask(task, stored -> {

						if (stored.failed()) {

							final Throwable cause = stored.cause();
							Logger.debug(cause, "Cannot store  {}.", task);
							OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

						} else {

							OperationReponseHandlers.responseOk(resultHandler, stored.result());
						}
					});
				}

			});
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTask(String taskId, JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		final Task source = Model.fromJsonObject(body, Task.class);
		if (source == null) {

			Logger.debug("The {} is not a valid Task to update.", body);
			OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_to_update",
					"The task to update is not right.");

		} else {

			this.repository.searchTask(taskId, search -> {

				final Task target = search.result();
				if (target == null) {

					Logger.debug(search.cause(), "Not found task {} to update", taskId);
					OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task_to_update",
							"You can not update the task '" + taskId + "', because it does not exist.");

				} else {

					target.merge(source, "bad_new_task", this.profileManager).setHandler(merge -> {

						if (merge.failed()) {

							final Throwable cause = merge.cause();
							Logger.debug(cause, "Cannot update  {} with {}.", target, source);
							OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

						} else {

							final Task merged = merge.result();
							if (merged.equals(target)) {

								OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST,
										"task_to_update_equal_to_original", "You can not update the task '" + taskId
												+ "', because the new values is equals to the current one.");

							} else {
								this.repository.updateTask(merged, update -> {

									if (update.failed()) {

										final Throwable cause = update.cause();
										Logger.debug(cause, "Cannot update  {}.", target);
										OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

									} else {

										OperationReponseHandlers.responseOk(resultHandler, merged);

									}

								});
							}
						}
					}

					);

				}
			});
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteTask(String taskId, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.repository.deleteTask(taskId, delete -> {

			if (delete.failed()) {

				final Throwable cause = delete.cause();
				Logger.debug(cause, "Cannot delete the task  {}.", taskId);
				OperationReponseHandlers.responseFailedWith(resultHandler, Status.NOT_FOUND, cause);

			} else {

				OperationReponseHandlers.responseOk(resultHandler);
			}

		});

	}

}
