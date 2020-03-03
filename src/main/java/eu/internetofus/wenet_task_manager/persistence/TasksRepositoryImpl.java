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

import eu.internetofus.wenet_task_manager.TimeManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

/**
 * Implementation of the {@link TasksRepository}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TasksRepositoryImpl extends Repository implements TasksRepository {

	/**
	 * The name of the collection that contains the tasks.
	 */
	public static final String TASKS_COLLECTION = "tasks";

	/**
	 * The name of the collection that contains the historic tasks.
	 */
	public static final String HISTORIC_TASKS_COLLECTION = "historicTasks";

	/**
	 * Create a new service.
	 *
	 * @param pool to create the connections.
	 */
	public TasksRepositoryImpl(MongoClient pool) {

		super(pool);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void searchTaskObject(String id, Handler<AsyncResult<JsonObject>> searchHandler) {

		final JsonObject query = new JsonObject().put("_id", id);
		this.pool.findOne(TASKS_COLLECTION, query, null, search -> {

			if (search.failed()) {

				searchHandler.handle(Future.failedFuture(search.cause()));

			} else {

				final JsonObject task = search.result();
				if (task == null) {

					searchHandler.handle(Future.failedFuture("Does not exist a task with the identifier '" + id + "'."));

				} else {

					task.put("taskId", task.remove("_id"));
					searchHandler.handle(Future.succeededFuture(task));
				}
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeTask(JsonObject task, Handler<AsyncResult<JsonObject>> storeHandler) {

		final long now = TimeManager.now();
		task.put("creationTs", now);
		this.pool.save(TASKS_COLLECTION, task, store -> {

			if (store.failed()) {

				storeHandler.handle(Future.failedFuture(store.cause()));

			} else {

				final String id = store.result();
				task.put("taskId", id);
				task.remove("_id");
				storeHandler.handle(Future.succeededFuture(task));
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTask(JsonObject task, Handler<AsyncResult<JsonObject>> updateHandler) {

		final Object id = task.remove("taskId");
		final JsonObject query = new JsonObject().put("_id", id);
		final JsonObject updateTask = new JsonObject().put("$set", task);
		final UpdateOptions options = new UpdateOptions().setMulti(false);
		this.pool.updateCollectionWithOptions(TASKS_COLLECTION, query, updateTask, options, update -> {

			if (update.failed()) {

				updateHandler.handle(Future.failedFuture(update.cause()));

			} else if (update.result().getDocModified() != 1) {

				updateHandler.handle(Future.failedFuture("Not Found task to update"));

			} else {

				task.put("taskId", id);
				task.remove("_id");
				updateHandler.handle(Future.succeededFuture(task));
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteTask(String id, Handler<AsyncResult<Void>> deleteHandler) {

		final JsonObject query = new JsonObject().put("_id", id);
		this.pool.removeDocument(TASKS_COLLECTION, query, remove -> {

			if (remove.failed()) {

				deleteHandler.handle(Future.failedFuture(remove.cause()));

			} else if (remove.result().getRemovedCount() != 1) {

				deleteHandler.handle(Future.failedFuture("Not Found task to delete"));

			} else {

				deleteHandler.handle(Future.succeededFuture());
			}
		});

	}

}
