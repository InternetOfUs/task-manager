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

import eu.internetofus.common.persitences.Repository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Implementation of the {@link TaskTypesRepository}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TaskTypesRepositoryImpl extends Repository implements TaskTypesRepository {

	/**
	 * The name of the collection that contains the taskTypes.
	 */
	public static final String TASK_TYPES_COLLECTION = "taskTypes";

	/**
	 * Create a new service.
	 *
	 * @param pool to create the connections.
	 */
	public TaskTypesRepositoryImpl(MongoClient pool) {

		super(pool);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void searchTaskTypeObject(String id, Handler<AsyncResult<JsonObject>> searchHandler) {

		final JsonObject query = new JsonObject().put("_id", id);
		this.findOneDocument(TASK_TYPES_COLLECTION, query, null, found -> {
			final String _id = (String) found.remove("_id");
			return found.put("id", _id);
		}, searchHandler);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeTaskType(JsonObject taskType, Handler<AsyncResult<JsonObject>> storeHandler) {

		final String id = (String) taskType.remove("id");
		if (id != null) {

			taskType.put("_id", id);
		}
		// final long now = TimeManager.now();
		// taskType.put("_creationTs", now);
		// taskType.put("_lastUpdateTs", now);
		this.storeOneDocument(TASK_TYPES_COLLECTION, taskType, stored -> {

			final String _id = (String) stored.remove("_id");
			return stored.put("id", _id);

		}, storeHandler);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTaskType(JsonObject taskType, Handler<AsyncResult<Void>> updateHandler) {

		final Object id = taskType.remove("id");
		final JsonObject query = new JsonObject().put("_id", id);
		// final long now = TimeManager.now();
		// taskType.put("_lastUpdateTs", now);
		this.updateOneDocument(TASK_TYPES_COLLECTION, query, taskType, updateHandler);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteTaskType(String id, Handler<AsyncResult<Void>> deleteHandler) {

		final JsonObject query = new JsonObject().put("_id", id);
		this.deleteOneDocument(TASK_TYPES_COLLECTION, query, deleteHandler);

	}

}
