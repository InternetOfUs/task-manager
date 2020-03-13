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

import eu.internetofus.common.api.models.Model;
import eu.internetofus.common.api.models.ValidationErrorException;
import eu.internetofus.common.api.models.Validations;
import eu.internetofus.common.services.WeNetProfileManagerService;
import eu.internetofus.wenet_task_manager.Merges;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * The task done by a WeneT user.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Schema(hidden = true, name = "Task", description = "The task done by a WeNet user.")
public class Task extends Model {

	/**
	 * The identifier of the task.
	 */
	@Schema(
			description = "The unique identifier of the task",
			example = "b129e550-9c9b-43cf-8559-2b79ad986610",
			accessMode = AccessMode.READ_ONLY)
	public String taskId;

	/**
	 * The identifier of the task.
	 */
	@Schema(
			description = "The timestamp representing the task creation instant",
			example = "1563800000",
			accessMode = AccessMode.READ_ONLY,
			nullable = true)
	public long creationTs;

	/**
	 * The current state of a task
	 */
	@Schema(description = "The current state of a task.", example = "Open", nullable = true)
	public TaskState state;

	/**
	 * The identifier of the WeNet user who created the task.
	 */
	@Schema(
			description = "The identifier of the WeNet user who created the task",
			example = "15837028-645a-4a55-9aaf-ceb846439eba",
			nullable = true)
	public String requesterUserId;

	/**
	 * The time the task should be started.
	 */
	@Schema(
			description = "The timestamp representing the time the task should be started",
			example = "1563800000",
			nullable = true)
	public long startTs;

	/**
	 * The time the task should be completed by.
	 */
	@Schema(
			description = "The timestamp representing the time the task should be completed by",
			example = "1563930000",
			nullable = true)
	public long endTs;

	/**
	 * The deadline time for offering help.
	 */
	@Schema(
			description = "The timestamp representing the deadline time for offering help",
			example = "1563920000",
			nullable = true)
	public long deadlineTs;

	/**
	 * The individual norms of the user
	 */
	@ArraySchema(
			schema = @Schema(implementation = Norm.class),
			arraySchema = @Schema(description = "The individual norms of the user"))
	public List<Norm> norms;

	/**
	 * Validate that the values of the model are right.
	 *
	 * @param codePrefix     prefix for the error code.
	 * @param profileService used to verify the user profile identifiers
	 *
	 * @return a future that inform if the model is valid.
	 */
	public Future<Void> validate(String codePrefix, WeNetProfileManagerService profileService) {

		try {

			this.taskId = Validations.validateNullableStringField(codePrefix, "taskId", 255, this.taskId);
			if (this.taskId != null) {

				return Future.failedFuture(new ValidationErrorException(codePrefix + ".taskId",
						"You can not specify the identifier of the task to create"));

			} else if (this.startTs > 0 && this.creationTs > this.startTs) {

				return Future.failedFuture(
						new ValidationErrorException(codePrefix + ".startTs", "The task cannot start before the creation time."));

			} else if (this.endTs > 0 && this.startTs > this.endTs) {

				return Future.failedFuture(
						new ValidationErrorException(codePrefix + ".endTs", "The task cannot end before the start time."));

			} else if (this.deadlineTs > 0 && this.deadlineTs > this.startTs) {

				return Future.failedFuture(new ValidationErrorException(codePrefix + ".deadlineTs",
						"The task deadline cannot be after the start time."));

			} else if (this.norms != null && !this.norms.isEmpty()) {

				final String codeNorms = codePrefix + ".norms";
				for (int index = 0; index < this.norms.size(); index++) {

					final Norm norm = this.norms.get(index);
					norm.validate(codeNorms + "[" + index + "]");
				}

			}

			final Promise<Void> promise = Promise.promise();
			if (this.requesterUserId != null) {

				profileService.retrieveProfile(this.requesterUserId, retrieve -> {

					if (retrieve.failed()) {

						promise.fail(new ValidationErrorException(codePrefix + ".requesterUserId",
								"The '" + this.requesterUserId + "' does not represents a WeNet user."));

					} else {

						promise.complete();
					}
				});

			} else {

				promise.complete();
			}

			return promise.future();

		} catch (final ValidationErrorException exception) {

			return Future.failedFuture(exception);
		}

	}

	/**
	 * Merge this model with another and check that is valid.
	 *
	 * @param source         model to get the values to merge.
	 * @param codePrefix     prefix for the error code.
	 * @param profileService used to verify the user profile identifiers
	 *
	 * @return a future that provide the merged model or the error that explains why
	 *         can not be merged.
	 */
	public Future<Task> merge(Task source, String codePrefix, WeNetProfileManagerService profileService) {

		try {

			final Task merged = new Task();

			// the creation time can not be changed.
			merged.creationTs = this.creationTs;

			merged.state = source.state;
			if (merged.state == null) {

				merged.state = this.state;
			}

			merged.requesterUserId = source.requesterUserId;
			if (merged.requesterUserId == null) {

				merged.requesterUserId = this.requesterUserId;
			}

			merged.startTs = source.startTs;
			if (merged.startTs == 0) {

				merged.startTs = this.startTs;
			}

			merged.endTs = source.endTs;
			if (merged.endTs == 0) {

				merged.endTs = this.endTs;
			}

			merged.deadlineTs = source.deadlineTs;
			if (merged.deadlineTs == 0) {

				merged.deadlineTs = this.deadlineTs;
			}

			final List<Norm> mergedNorms = Merges.mergeListOfNorms(this.norms, source.norms, codePrefix + ".norms");

			final Promise<Task> promise = Promise.promise();
			final Future<Task> future = promise.future();
			merged.validate(codePrefix, profileService).setHandler(validate -> {

				if (validate.failed()) {

					promise.fail(validate.cause());

				} else {

					merged.taskId = this.taskId;
					merged.norms = mergedNorms;
					promise.complete(merged);

				}

			});
			return future;

		} catch (final ValidationErrorException exception) {

			return Future.failedFuture(exception);
		}
	}

}
