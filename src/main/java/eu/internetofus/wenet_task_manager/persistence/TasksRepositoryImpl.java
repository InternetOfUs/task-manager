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

import eu.internetofus.common.TimeManager;
import eu.internetofus.common.persitences.Repository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

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
	 * Create a new service.
	 *
	 * @param pool to create the connections.
	 */
	public TasksRepositoryImpl(MongoClient pool) {

		super(pool);
		// pool.listIndexes(collection, resultHandler)
		// final IndexOptions options = new IndexOptions();
		// options.unique(true);
		// pool.createIndexWithOptions(TASKS_COLLECTION, new JsonObject().put("id", 1),
		// options, resultHandler -> {
		//
		// if (resultHandler.failed()) {
		//
		// Logger.error(resultHandler.cause(), "Can not create index for the tasks
		// collection");
		// } else {
		//
		// Logger.debug(resultHandler.cause(), "created index for task collection");
		// }
		//
		// });

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void searchTaskObject(String id, Handler<AsyncResult<JsonObject>> searchHandler) {

		final JsonObject query = new JsonObject().put("_id", id);
		this.findOneDocument(TASKS_COLLECTION, query, null, found -> {
			final String _id = (String) found.remove("_id");
			return found.put("id", _id);
		}, searchHandler);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeTask(JsonObject task, Handler<AsyncResult<JsonObject>> storeHandler) {

		final String id = (String) task.remove("id");
		if (id != null) {

			task.put("_id", id);
		}
		final long now = TimeManager.now();
		task.put("_creationTs", now);
		task.put("_lastUpdateTs", now);
		this.storeOneDocument(TASKS_COLLECTION, task, stored -> {

			final String _id = (String) stored.remove("_id");
			return stored.put("id", _id);

		}, storeHandler);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTask(JsonObject task, Handler<AsyncResult<Void>> updateHandler) {

		final Object id = task.remove("id");
		final JsonObject query = new JsonObject().put("_id", id);
		final long now = TimeManager.now();
		task.put("_lastUpdateTs", now);
		this.updateOneDocument(TASKS_COLLECTION, query, task, updateHandler);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteTask(String id, Handler<AsyncResult<Void>> deleteHandler) {

		final JsonObject query = new JsonObject().put("_id", id);
		this.deleteOneDocument(TASKS_COLLECTION, query, deleteHandler);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retrieveTasksPageObject(JsonObject query, int offset, int limit,
			Handler<AsyncResult<JsonObject>> searchHandler) {

		final FindOptions options = new FindOptions();
		options.setSkip(offset);
		options.setLimit(limit);
		this.searchPageObject(TASKS_COLLECTION, query, options, "tasks", searchHandler);
		//
		// final , query, options, "tasks", task -> task.put("id", task.remove("_id")),
		// searchHandler);

	}

}
