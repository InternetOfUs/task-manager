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

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.tinylog.Logger;

import eu.internetofus.common.api.OperationReponseHandlers;
import eu.internetofus.common.api.models.Model;
import eu.internetofus.common.api.models.ValidationErrorException;
import eu.internetofus.common.api.models.wenet.InteractionProtocolMessage;
import eu.internetofus.common.api.models.wenet.Task;
import eu.internetofus.common.api.models.wenet.TaskTransaction;
import eu.internetofus.common.api.models.wenet.TaskType;
import eu.internetofus.common.services.WeNetInteractionProtocolEngineService;
import eu.internetofus.common.services.WeNetProfileManagerService;
import eu.internetofus.wenet_task_manager.persistence.TaskTypesRepository;
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
	 * The event bus that is using.
	 */
	protected Vertx vertx;

	/**
	 * The repository to manage the tasks.
	 */
	protected TasksRepository repository;

	/**
	 * The repository to manage the task types.
	 */
	protected TaskTypesRepository typesRepository;

	/**
	 * The component that manage the profiles.
	 */
	protected WeNetProfileManagerService profileManager;

	/**
	 * The component that manage the interaction protocols.
	 */
	protected WeNetInteractionProtocolEngineService interactionProtocolEngine;

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

		this.vertx = vertx;
		this.repository = TasksRepository.createProxy(vertx);
		this.typesRepository = TaskTypesRepository.createProxy(vertx);
		this.profileManager = WeNetProfileManagerService.createProxy(vertx);
		this.interactionProtocolEngine = WeNetInteractionProtocolEngineService.createProxy(this.vertx);
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

			task.validate("bad_task", this.vertx).onComplete(validation -> {

				if (validation.failed()) {

					final Throwable cause = validation.cause();
					Logger.debug(cause, "The {} is not valid.", task);
					OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

				} else {

					this.repository.storeTask(task, stored -> {

						if (stored.failed()) {

							final Throwable cause = validation.cause();
							Logger.debug(cause, "Cannot store {}.", task);
							OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

						} else {

							final InteractionProtocolMessage message = new InteractionProtocolMessage();
							message.taskId = task.id;
							message.appId = task.appId;
							message.content = new JsonObject().put("message", "Looking for volunteers");
							this.interactionProtocolEngine.sendMessage(message.toJsonObject(), sent -> {

								if (sent.failed()) {

									final Throwable cause = validation.cause();
									Logger.debug(cause, "Cannot send message {}.", message);
									OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

								} else {

									OperationReponseHandlers.responseOk(resultHandler, stored.result());
								}

							});

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

					target.merge(source, "bad_new_task", this.vertx).onComplete(merge -> {

						if (merge.failed()) {

							final Throwable cause = merge.cause();
							Logger.debug(cause, "Cannot update {} with {}.", target, source);
							OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

						} else {

							final Task merged = merge.result();
							if (merged.equals(target)) {

								OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST,
										"task_to_update_equal_to_original", "You can not update the task of the task '" + taskId
												+ "', because the new values is equals to the current one.");

							} else {
								this.repository.updateTask(merged, update -> {

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retrieveTaskType(String taskTypeId, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.typesRepository.searchTaskTypeObject(taskTypeId, search -> {

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

					this.typesRepository.storeTaskType(taskType, stored -> {

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

			this.typesRepository.searchTaskType(taskTypeId, search -> {

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
								this.typesRepository.updateTaskType(merged, update -> {

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

		this.typesRepository.deleteTaskType(taskTypeId, delete -> {

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doTaskTransaction(JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		final TaskTransaction taskTransaction = Model.fromJsonObject(body, TaskTransaction.class);
		if (taskTransaction == null) {

			Logger.debug("The {} is not a valid TaskTransaction.", body);
			OperationReponseHandlers.responseWithErrorMessage(resultHandler, Status.BAD_REQUEST, "bad_task_transaction",
					"The task transaction is not right.");

		} else {

			taskTransaction.validate("bad_task_transaction", this.vertx).onComplete(validation -> {

				if (validation.failed()) {

					final Throwable cause = validation.cause();
					Logger.debug(cause, "The {} is not valid.", taskTransaction);
					OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

				} else {

					this.repository.searchTask(taskTransaction.taskId, search -> {

						if (search.failed()) {

							OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST,
									new ValidationErrorException("bad_task_transaction.taskId",
											"Does not exist a task associated to the identifier"));

						} else {

							final Task task = search.result();
							final InteractionProtocolMessage message = new InteractionProtocolMessage();
							message.taskId = task.id;
							message.appId = task.appId;
							message.content = taskTransaction.attributes;
							this.interactionProtocolEngine.sendMessage(message.toJsonObject(), sent -> {

								if (sent.failed()) {

									final Throwable cause = validation.cause();
									Logger.debug(cause, "Cannot send message {}.", message);
									OperationReponseHandlers.responseFailedWith(resultHandler, Status.BAD_REQUEST, cause);

								} else {

									OperationReponseHandlers.responseOk(resultHandler, taskTransaction.toJsonObject());
								}

							});

						}

					});
				}

			});
		}

	}

}
