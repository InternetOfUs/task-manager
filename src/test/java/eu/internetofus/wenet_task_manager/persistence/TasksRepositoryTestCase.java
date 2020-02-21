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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import eu.internetofus.wenet_task_manager.api.tasks.Task;
import eu.internetofus.wenet_task_manager.api.tasks.TaskTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;

/**
 * Generic test over the {@link TasksRepository}.
 *
 * @param <T> the repository to test.
 *
 * @see TasksRepository
 *
 * @author UDT-IA, IIIA-CSIC
 */
public abstract class TasksRepositoryTestCase<T extends TasksRepository> {

	/**
	 * The repository to do the tests.
	 */
	protected T repository;

	/**
	 * Verify that can not found a task if it is not defined.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotFoundUndefinedTask(VertxTestContext testContext) {

		this.repository.searchTask("undefined task identifier", testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can not found a task object if it is not defined.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#searchTaskObject(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotFoundUndefinedTaskObject(VertxTestContext testContext) {

		this.repository.searchTaskObject("undefined task identifier", testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can found a task.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTask(VertxTestContext testContext) {

		this.repository.storeTask(new Task(), testContext.succeeding(storedTask -> {

			this.repository.searchTask(storedTask.taskId, testContext.succeeding(foundTask -> testContext.verify(() -> {
				assertThat(foundTask).isEqualTo(storedTask);
				testContext.completeNow();
			})));

		}));

	}

	/**
	 * Verify that can found a task object.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#searchTaskObject(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldFoundTaskObject(VertxTestContext testContext) {

		this.repository.storeTask(new JsonObject(), testContext.succeeding(storedTask -> {

			this.repository.searchTaskObject(storedTask.getString("id"),
					testContext.succeeding(foundTask -> testContext.verify(() -> {
						assertThat(foundTask).isEqualTo(storedTask);
						testContext.completeNow();
					})));

		}));

	}

	/**
	 * Verify that can not store a task that can not be an object.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#updateTask(Task, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotStoreATaskThatCanNotBeAnObject(VertxTestContext testContext) {

		final Task task = new Task() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public JsonObject toJsonObject() {

				return null;
			}
		};
		task.taskId = "undefined task identifier";
		this.repository.storeTask(task, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can store a task.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTask(VertxTestContext testContext) {

		final Task task = new Task();
		this.repository.storeTask(task, testContext.succeeding(storedTask -> testContext.verify(() -> {

			assertThat(storedTask).isNotNull();
			assertThat(storedTask.taskId).isNotEmpty();
			testContext.completeNow();
		})));

	}

	/**
	 * Verify that can store a task object.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldStoreTaskObject(VertxTestContext testContext) {

		final long now = System.currentTimeMillis();
		this.repository.storeTask(new JsonObject(), testContext.succeeding(storedTask -> testContext.verify(() -> {

			assertThat(storedTask).isNotNull();
			final String id = storedTask.getString("id");
			assertThat(id).isNotEmpty();
			assertThat(storedTask.getLong("_creationTs", 0l)).isNotEqualTo(0).isGreaterThanOrEqualTo(now);
			assertThat(storedTask.getLong("_lastUpdateTs", 1l)).isNotEqualTo(1).isGreaterThanOrEqualTo(now);
			testContext.completeNow();
		})));

	}

	/**
	 * Verify that can not update a task if it is not defined.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#updateTask(Task, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateUndefinedTask(VertxTestContext testContext) {

		final Task task = new Task();
		task.taskId = "undefined task identifier";
		this.repository.updateTask(task, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can not update a task if it is not defined.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#updateTask(JsonObject, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateUndefinedTaskObject(VertxTestContext testContext) {

		final JsonObject task = new JsonObject().put("id", "undefined task identifier");
		this.repository.updateTask(task, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can not update a task if it is not defined.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#updateTask(Task, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotUpdateATaskThatCanNotBeAnObject(VertxTestContext testContext) {

		final Task task = new Task() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public JsonObject toJsonObject() {

				return null;
			}
		};
		task.taskId = "undefined task identifier";
		this.repository.updateTask(task, testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can update a task.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#updateTask(Task, io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateTask(VertxTestContext testContext) {

		final Task task = new Task();

		this.repository.storeTask(task, testContext.succeeding(stored -> testContext.verify(() -> {

			final long now = System.currentTimeMillis();
			final Task update = new TaskTest().createModelExample(23);
			update.taskId = stored.taskId;
			this.repository.updateTask(update, testContext.succeeding(updatedTask -> testContext.verify(() -> {

				assertThat(updatedTask).isNotNull();
				assertThat(updatedTask.taskId).isNotEmpty().isEqualTo(stored.taskId);
				assertThat(updatedTask).isEqualTo(update);
				this.repository.searchTask(stored.taskId, testContext.succeeding(foundTask -> testContext.verify(() -> {
					assertThat(foundTask).isEqualTo(updatedTask);
					testContext.completeNow();
				})));
			})));

		})));

	}

	/**
	 * Verify that update a defined task object.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#updateTask(JsonObject, io.vertx.core.Handler)
	 */
	@Test
	public void shouldUpdateTaskObject(VertxTestContext testContext) {

		this.repository.storeTask(new JsonObject().put("nationality", "Italian"),
				testContext.succeeding(stored -> testContext.verify(() -> {

					final String id = stored.getString("id");
					final JsonObject update = new JsonObject().put("id", id).put("occupation", "Unemployed");
					this.repository.updateTask(update, testContext.succeeding(updatedTask -> testContext.verify(() -> {

						assertThat(updatedTask).isNotNull();
						update.put("_lastUpdateTs", updatedTask.getLong("_lastUpdateTs"));
						assertThat(updatedTask).isEqualTo(update);
						this.repository.searchTaskObject(id, testContext.succeeding(foundTask -> testContext.verify(() -> {
							stored.put("_lastUpdateTs", updatedTask.getLong("_lastUpdateTs"));
							stored.put("occupation", "Unemployed");
							assertThat(foundTask).isEqualTo(stored);
							testContext.completeNow();
						})));
					})));

				})));

	}

	/**
	 * Verify that can not delete a task if it is not defined.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#searchTask(String, io.vertx.core.Handler)
	 */
	@Test
	public void shouldNotDeleteUndefinedTask(VertxTestContext testContext) {

		this.repository.deleteTask("undefined task identifier", testContext.failing(failed -> {
			testContext.completeNow();
		}));

	}

	/**
	 * Verify that can delete a task.
	 *
	 * @param testContext context that executes the test.
	 *
	 * @see TasksRepository#updateTask(JsonObject, io.vertx.core.Handler)
	 */
	@Test
	public void shouldDeleteTask(VertxTestContext testContext) {

		this.repository.storeTask(new JsonObject(), testContext.succeeding(stored -> {

			final String id = stored.getString("id");
			this.repository.deleteTask(id, testContext.succeeding(success -> {

				this.repository.searchTaskObject(id, testContext.failing(search -> {

					testContext.completeNow();

				}));

			}));

		}));

	}

}
