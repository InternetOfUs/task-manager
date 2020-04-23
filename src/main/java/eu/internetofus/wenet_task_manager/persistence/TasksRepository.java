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

import eu.internetofus.common.api.models.Model;
import eu.internetofus.common.api.models.wenet.Task;
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
 * The service to manage the {@link Task} on the database.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ProxyGen
public interface TasksRepository {

	/**
	 * The address of this service.
	 */
	String ADDRESS = "wenet_task_manager.persistence.tasks";

	/**
	 * Create a proxy of the {@link TasksRepository}.
	 *
	 * @param vertx where the service has to be used.
	 *
	 * @return the task.
	 */
	static TasksRepository createProxy(Vertx vertx) {

		return new TasksRepositoryVertxEBProxy(vertx, TasksRepository.ADDRESS);
	}

	/**
	 * Register this service.
	 *
	 * @param vertx that contains the event bus to use.
	 * @param pool  to create the database connections.
	 */
	static void register(Vertx vertx, MongoClient pool) {

		new ServiceBinder(vertx).setAddress(TasksRepository.ADDRESS).register(TasksRepository.class,
				new TasksRepositoryImpl(pool));

	}
	
	/**
	 * Search for the task with the specified identifier.
	 *
	 * @param id            identifier of the task to search.
	 * @param searchHandler handler to manage the search.
	 */
	@GenIgnore
	default void searchTask(String id, Handler<AsyncResult<Task>> searchHandler) {

		this.searchTaskObject(id, search -> {

			if (search.failed()) {

				searchHandler.handle(Future.failedFuture(search.cause()));

			} else {

				final JsonObject value = search.result();
				final Task task = Model.fromJsonObject(value, Task.class);
				if (task == null) {

					searchHandler.handle(Future.failedFuture("The stored task is not valid."));

				} else {

					searchHandler.handle(Future.succeededFuture(task));
				}
			}
		});
	}

	/**
	 * Search for the task with the specified identifier.
	 *
	 * @param id            identifier of the task to search.
	 * @param searchHandler handler to manage the search.
	 */
	void searchTaskObject(String id, Handler<AsyncResult<JsonObject>> searchHandler);


	/**
	 * Store a task.
	 *
	 * @param task         to store.
	 * @param storeHandler handler to manage the store.
	 */
	@GenIgnore
	default void storeTask(Task task, Handler<AsyncResult<Task>> storeHandler) {

		final JsonObject object = task.toJsonObject();
		if (object == null) {

			storeHandler.handle(Future.failedFuture("The task can not converted to JSON."));

		} else {

			this.storeTask(object, stored -> {
				if (stored.failed()) {

					storeHandler.handle(Future.failedFuture(stored.cause()));

				} else {

					final JsonObject value = stored.result();
					final Task storedTask = Model.fromJsonObject(value, Task.class);
					if (storedTask == null) {

						storeHandler.handle(Future.failedFuture("The stored task is not valid."));

					} else {

						storeHandler.handle(Future.succeededFuture(storedTask));
					}

				}
			});
		}
	}

	/**
	 * Store a task.
	 *
	 * @param task         to store.
	 * @param storeHandler handler to manage the search.
	 */
	void storeTask(JsonObject task, Handler<AsyncResult<JsonObject>> storeHandler);

	/**
	 * Update a task.
	 *
	 * @param task          to update.
	 * @param updateHandler handler to manage the update.
	 */
	@GenIgnore
	default void updateTask(Task task, Handler<AsyncResult<Void>> updateHandler) {

		final JsonObject object = task.toJsonObject();
		if (object == null) {

			updateHandler.handle(Future.failedFuture("The task can not converted to JSON."));

		} else {

			this.updateTask(object, updateHandler);
		}
		
	}

	/**
	 * Update a task.
	 *
	 * @param task          to update.
	 * @param updateHandler handler to manage the update result.
	 */
	void updateTask(JsonObject task, Handler<AsyncResult<Void>> updateHandler);

	/**
	 * Delete a task.
	 *
	 * @param id            identifier of the task to delete.
	 * @param deleteHandler handler to manage the delete result.
	 */
	void deleteTask(String id, Handler<AsyncResult<Void>> deleteHandler);

}
