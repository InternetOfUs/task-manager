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

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.tinylog.Logger;

import eu.internetofus.common.api.OperationReponseHandlers;
import eu.internetofus.common.api.models.Model;
import eu.internetofus.common.api.models.wenet.TaskType;
import eu.internetofus.common.services.WeNetProfileManagerService;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

/**
 * Resource that provide the methods for the {@link TaskTypes}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TaskTypesResource implements TaskTypes {

	/**
	 * The event bus that is using.
	 */
	protected Vertx vertx;

	/**
	 * The repository to manage the taskTypes.
	 */
	protected TaskTypesRepository repository;

	/**
	 * The repository to manage the taskTypes.
	 */
	protected WeNetProfileManagerService profileManager;

	/**
	 * Create an empty resource. This is only used for unit tests.
	 */
	protected TaskTypesResource() {

	}

	/**
	 * Create a new instance to provide the services of the {@link TaskTypes}.
	 *
	 * @param vertx where resource is defined.
	 */
	public TaskTypesResource(Vertx vertx) {

		this.vertx = vertx;
		this.repository = TaskTypesRepository.createProxy(vertx);
		this.profileManager = WeNetProfileManagerService.createProxy(vertx);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retrieveTaskType(String taskTypeId, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.repository.searchTaskTypeObject(taskTypeId, search -> {

			final JsonObject taskType = search.result();
			if (taskType == null) {

				Logger.debug(search.cause(), "Not found task type type for {}", taskTypeId);
				OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND, "not_found_task_type",
						"Does not exist a task type type associated to '" + taskTypeId + "'.");

			} else {

				OperationReponseHandlers.responseOk(resultHandler, taskType);

			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createTaskType(JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		final TaskType taskType = Model.fromJsonObject(body, TaskType.class);
		if (taskType == null) {

			Logger.debug("The {} is not a valid TaskType.", body);
			OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_type",
					"The task type is not right.");

		} else {

			taskType.validate("bad_task_type", this.vertx).onComplete(validation -> {

				if (validation.failed()) {

					final Throwable cause = validation.cause();
					Logger.debug(cause, "The {} is not valid.", taskType);
					OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

				} else {

					this.repository.storeTaskType(taskType, stored -> {

						if (stored.failed()) {

							final Throwable cause = validation.cause();
							Logger.debug(cause, "Cannot store {}.", taskType);
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
	public void updateTaskType(String taskTypeId, JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		final TaskType source = Model.fromJsonObject(body, TaskType.class);
		if (source == null) {

			Logger.debug("The {} is not a valid TaskType to update.", body);
			OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_type_to_update",
					"The task type to update is not right.");

		} else {

			this.repository.searchTaskType(taskTypeId, search -> {

				final TaskType target = search.result();
				if (target == null) {

					Logger.debug(search.cause(), "Not found task type {} to update", taskTypeId);
					OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_FOUND,
							"not_found_task_type_to_update",
							"You can not update the task type '" + taskTypeId + "', because it does not exist.");

				} else {

					target.merge(source, "bad_new_task_type", this.vertx).onComplete(merge -> {

						if (merge.failed()) {

							final Throwable cause = merge.cause();
							Logger.debug(cause, "Cannot update {} with {}.", target, source);
							OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

						} else {

							final TaskType merged = merge.result();
							if (merged.equals(target)) {

								OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST,
										"taskType_to_update_equal_to_original", "You can not update the task type '" + taskTypeId
												+ "', because the new values is equals to the current one.");

							} else {
								this.repository.updateTaskType(merged, update -> {

									if (update.failed()) {

										final Throwable cause = update.cause();
										Logger.debug(cause, "Cannot update {}.", target);
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
	public void deleteTaskType(String taskTypeId, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.repository.deleteTaskType(taskTypeId, delete -> {

			if (delete.failed()) {

				final Throwable cause = delete.cause();
				Logger.debug(cause, "Cannot delete the task type {}.", taskTypeId);
				OperationReponseHandlers.responseFailedWith(resultHandler, Status.NOT_FOUND, cause);

			} else {

				OperationReponseHandlers.responseOk(resultHandler);
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retrieveTaskTypePage(String name, String description, List<String> keywords, int offset, int limit,
			OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.NOT_IMPLEMENTED, "not_implemented",
				"It is not implemented yet");

	}

}
