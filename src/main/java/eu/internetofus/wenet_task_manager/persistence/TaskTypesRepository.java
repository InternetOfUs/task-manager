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

package eu.internetofus.wenet_task_manager.persistence;

import eu.internetofus.common.components.Model;
import eu.internetofus.common.components.task_manager.TaskType;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * The service to manage the {@link TaskType} on the database.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ProxyGen
public interface TaskTypesRepository {

	/**
	 * The address of this service.
	 */
	String ADDRESS = "wenet_task_manager.persistence.taskTypes";

	/**
	 * Create a proxy of the {@link TaskTypesRepository}.
	 *
	 * @param vertx where the service has to be used.
	 *
	 * @return the taskType.
	 */
	static TaskTypesRepository createProxy(Vertx vertx) {

		return new TaskTypesRepositoryVertxEBProxy(vertx, TaskTypesRepository.ADDRESS);
	}

	/**
	 * Register this service.
	 *
	 * @param vertx that contains the event bus to use.
	 * @param pool  to create the database connections.
	 */
	static void register(Vertx vertx, MongoClient pool) {

		new ServiceBinder(vertx).setAddress(TaskTypesRepository.ADDRESS).register(TaskTypesRepository.class,
				new TaskTypesRepositoryImpl(pool));

	}

	/**
	 * Search for the task type with the specified identifier.
	 *
	 * @param id            identifier of the task type to search.
	 * @param searchHandler handler to manage the search.
	 */
	@GenIgnore
	default void searchTaskType(String id, Handler<AsyncResult<TaskType>> searchHandler) {

		this.searchTaskTypeObject(id, search -> {

			if (search.failed()) {

				searchHandler.handle(Future.failedFuture(search.cause()));

			} else {

				final JsonObject value = search.result();
				final TaskType taskType = Model.fromJsonObject(value, TaskType.class);
				if (taskType == null) {

					searchHandler.handle(Future.failedFuture("The stored taskType is not valid."));

				} else {

					searchHandler.handle(Future.succeededFuture(taskType));
				}
			}
		});
	}

	/**
	 * Search for the task type with the specified identifier.
	 *
	 * @param id            identifier of the task type to search.
	 * @param searchHandler handler to manage the search.
	 */
	void searchTaskTypeObject(String id, Handler<AsyncResult<JsonObject>> searchHandler);

	/**
	 * Store a task type.
	 *
	 * @param taskType     to store.
	 * @param storeHandler handler to manage the store.
	 */
	@GenIgnore
	default void storeTaskType(TaskType taskType, Handler<AsyncResult<TaskType>> storeHandler) {

		final JsonObject object = taskType.toJsonObject();
		if (object == null) {

			storeHandler.handle(Future.failedFuture("The taskType can not converted to JSON."));

		} else {

			this.storeTaskType(object, stored -> {
				if (stored.failed()) {

					storeHandler.handle(Future.failedFuture(stored.cause()));

				} else {

					final JsonObject value = stored.result();
					final TaskType storedTaskType = Model.fromJsonObject(value, TaskType.class);
					if (storedTaskType == null) {

						storeHandler.handle(Future.failedFuture("The stored taskType is not valid."));

					} else {

						storeHandler.handle(Future.succeededFuture(storedTaskType));
					}

				}
			});
		}
	}

	/**
	 * Store a task type.
	 *
	 * @param taskType     to store.
	 * @param storeHandler handler to manage the search.
	 */
	void storeTaskType(JsonObject taskType, Handler<AsyncResult<JsonObject>> storeHandler);

	/**
	 * Update a task type.
	 *
	 * @param taskType      to update.
	 * @param updateHandler handler to manage the update.
	 */
	@GenIgnore
	default void updateTaskType(TaskType taskType, Handler<AsyncResult<Void>> updateHandler) {

		final JsonObject object = taskType.toJsonObject();
		if (object == null) {

			updateHandler.handle(Future.failedFuture("The taskType can not converted to JSON."));

		} else {

			this.updateTaskType(object, updateHandler);
		}

	}

	/**
	 * Update a task type.
	 *
	 * @param taskType      to update.
	 * @param updateHandler handler to manage the update result.
	 */
	void updateTaskType(JsonObject taskType, Handler<AsyncResult<Void>> updateHandler);

	/**
	 * Delete a task type.
	 *
	 * @param id            identifier of the task type to delete.
	 * @param deleteHandler handler to manage the delete result.
	 */
	void deleteTaskType(String id, Handler<AsyncResult<Void>> deleteHandler);

}
