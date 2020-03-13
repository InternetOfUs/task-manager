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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import eu.internetofus.common.api.models.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * The definition of the web services to manage the {@link Task}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Path(Tasks.PATH)
@Tag(name = "Tasks")
@WebApiServiceGen
public interface Tasks {

	/**
	 * The path to the version resource.
	 */
	String PATH = "/tasks";

	/**
	 * The address of this service.
	 */
	String ADDRESS = "wenet_task_manager.api.tasks";

	/**
	 * The sub path to retrieve a task.
	 */
	String TASK_ID_PATH = "/{taskId}";

	/**
	 * Called when want to create an user task.
	 *
	 * @param body          the new task to create.
	 * @param context       of the request.
	 * @param resultHandler to inform of the response.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create a task", description = "Create a new WeNet user task")
	@RequestBody(
			description = "The new task to create",
			required = true,
			content = @Content(
					schema = @Schema(
							ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/571266f9402fb78cf01bf1d9cdb23d2989a7882a/sources/wenet-models.yaml#/components/schemas/Task")))
	@ApiResponse(
			responseCode = "200",
			description = "The created task",
			content = @Content(
					schema = @Schema(
							ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/571266f9402fb78cf01bf1d9cdb23d2989a7882a/sources/wenet-models.yaml#/components/schemas/Task")))
	@ApiResponse(
			responseCode = "400",
			description = "Bad task",
			content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
	void createTask(@Parameter(hidden = true, required = false) JsonObject body,
			@Parameter(hidden = true, required = false) OperationRequest context,
			@Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

	/**
	 * Called when want to get a task.
	 *
	 * @param taskId        identifier of the task to get.
	 * @param context       of the request.
	 * @param resultHandler to inform of the response.
	 */
	@GET
	@Path(TASK_ID_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Return a task associated to the identifier",
			description = "Allow to get a task associated to an identifier")
	@ApiResponse(
			responseCode = "200",
			description = "The task associated to the identifier",
			content = @Content(
					schema = @Schema(
							ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/571266f9402fb78cf01bf1d9cdb23d2989a7882a/sources/wenet-models.yaml#/components/schemas/Task")))
	@ApiResponse(
			responseCode = "404",
			description = "Not found task",
			content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
	void retrieveTask(
			@PathParam("taskId") @Parameter(
					description = "The identifier of the task to get",
					example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId,
			@Parameter(hidden = true, required = false) OperationRequest context,
			@Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

	/**
	 * Called when want to modify a task.
	 *
	 * @param taskId        identifier of the task to modify.
	 * @param body          the new task attributes.
	 * @param context       of the request.
	 * @param resultHandler to inform of the response.
	 */
	@PUT
	@Path(TASK_ID_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Modify a task", description = "Change the attributes of a task")
	@RequestBody(
			description = "The new values for the task",
			required = true,
			content = @Content(
					schema = @Schema(
							ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/571266f9402fb78cf01bf1d9cdb23d2989a7882a/sources/wenet-models.yaml#/components/schemas/Task")))
	@ApiResponse(
			responseCode = "200",
			description = "The updated task",
			content = @Content(
					schema = @Schema(
							ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/571266f9402fb78cf01bf1d9cdb23d2989a7882a/sources/wenet-models.yaml#/components/schemas/Task")))
	@ApiResponse(
			responseCode = "400",
			description = "Bad task",
			content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Not found task",
			content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
	void updateTask(
			@PathParam("taskId") @Parameter(
					description = "The identifier of the task to update",
					example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId,
			@Parameter(hidden = true, required = false) JsonObject body,
			@Parameter(hidden = true, required = false) OperationRequest context,
			@Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

	/**
	 * Called when want to delete a task.
	 *
	 * @param taskId        identifier of the task to delete.
	 * @param context       of the request.
	 * @param resultHandler to inform of the response.
	 */
	@DELETE
	@Path(TASK_ID_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Delete the task associated to the identifier",
			description = "Allow to delete a task associated to an identifier")
	@ApiResponse(responseCode = "204", description = "The task was deleted successfully")
	@ApiResponse(
			responseCode = "404",
			description = "Not found task",
			content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
	void deleteTask(@PathParam("taskId") @Parameter(description = "The identifier of the task to delete") String taskId,
			@Parameter(hidden = true, required = false) OperationRequest context,
			@Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

}
