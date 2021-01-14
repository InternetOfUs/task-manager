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
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
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

import eu.internetofus.common.components.ErrorMessage;
import eu.internetofus.common.components.task_manager.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
   * The path to the tasks resource.
   */
  String PATH = "/tasks";

  /**
   * The address of this service.
   */
  String ADDRESS = "wenet_task_manager.api.tasks";

  /**
   * The path to the task transactions resource.
   */
  String TRANSACTIONS_PATH = "/transactions";

  /**
   * The path to the task messages resource.
   */
  String MESSAGES_PATH = "/messages";

  /**
   * Called when want to create a task.
   *
   * @param body          the new task to create.
   * @param request       of the query.
   * @param resultHandler to inform of the response.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Create a task", description = "Create a new task")
  @RequestBody(description = "The new task to create", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "200", description = "The created task", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "400", description = "Bad task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void createTask(@Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to get a task.
   *
   * @param taskId        identifier of the task to get.
   * @param request       of the query.
   * @param resultHandler to inform of the response.
   */
  @GET
  @Path("/{taskId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return a task", description = "Allow to get a task with an specific identifier")
  @ApiResponse(responseCode = "200", description = "The task associated to the identifier", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTask(
      @PathParam("taskId") @Parameter(description = "The identifier of the task to get", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to get the information of some tasks.
   *
   * @param appId           application identifier to match for the tasks to
   *                        return.
   * @param requesterId     requester identifier to match for the tasks to return.
   * @param taskTypeId      task type identifier to match for the tasks to return.
   * @param goalName        pattern to match with the goal name of the tasks to
   *                        return.
   * @param goalDescription pattern to match with the goal description of the
   *                        tasks to return.
   * @param creationFrom    minimal creation time stamp of the tasks to return.
   * @param creationTo      maximal creation time stamp of the tasks to return.
   * @param updateFrom      minimal update time stamp of the tasks to return.
   * @param updateTo        maximal update time stamp of the tasks to return.
   * @param hasCloseTs      this is {@code true} if the tasks to return need to
   *                        have a {@link Task#closeTs}
   * @param closeFrom       minimal close time stamp of the tasks to return.
   * @param closeTo         maximal close time stamp of the tasks to return.
   * @param order           of the tasks to return.
   * @param offset          index of the first task to return.
   * @param limit           number maximum of tasks to return.
   * @param request         of the query.
   * @param resultHandler   to inform of the response.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Search for some tasks", description = "Allow to get a page of task with the specified query parameters.")
  @ApiResponse(responseCode = "200", description = "The page with the matching tasks", content = @Content(schema = @Schema(implementation = TasksPage.class)))
  @ApiResponse(responseCode = "400", description = "If any of the search pattern is not valid", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTasksPage(
      @QueryParam(value = "appId") @Parameter(description = "An application identifier to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the application identifier of the tasks to return if you write between '/'. For example to get the tasks for the aplications '1' and '2' you must pass as 'appId' '/^[1|2]$/'.", example = "1", required = false) String appId,
      @QueryParam(value = "requesterId") @Parameter(description = "An user identifier to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the requester identifier of the tasks to return if you write between '/'. For example to get the tasks for the requesters '1' and '2' you must pass as 'requesterId' '/^[1|2]$/'.", example = "1e346fd440", required = false) String requesterId,
      @QueryParam(value = "taskTypeId") @Parameter(description = "A task type identifier to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the task type identifier of the tasks to return if you write between '/'. For example to get the tasks for the types '1' and '2' you must pass as 'taskTypeId' '/^[1|2]$/'.", example = "1e346fd440", required = false) String taskTypeId,
      @QueryParam(value = "goalName") @Parameter(description = "A goal name to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the goal name of the tasks to return if you write between '/'. For example to get the tasks with a goal name with the word 'eat' you must pass as 'goalName' '/.*eat.*/'", example = "/.*eat.*/", required = false) String goalName,
      @QueryParam(value = "goalDescription") @Parameter(description = "A goal description to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the goal description of the tasks to return if you write between '/'. For example to get the tasks with a goal description with the word 'eat' you must pass as 'goalDescription' '/.*eat.*/'", example = "/.*eat.*/", required = false) String goalDescription,
      @QueryParam(value = "creationFrom") @Parameter(description = "The difference, measured in seconds, between the minimum creation time stamp of the task and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long creationFrom,
      @QueryParam(value = "creationTo") @Parameter(description = "The difference, measured in seconds, between the maximum creation time stamp of the task and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long creationTo,
      @QueryParam(value = "updateFrom") @Parameter(description = "The difference, measured in seconds, between the minimum update time stamp of the task and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long updateFrom,
      @QueryParam(value = "updateTo") @Parameter(description = "The difference, measured in seconds, between the maximum update time stamp of the task and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long updateTo,
      @QueryParam(value = "hasCloseTs") @Parameter(description = "This is 'true' if the task to return has defined a 'closeTs', or 'false' if this fiels is not defined. In other words, get the closed or open tasks.", example = "false", required = false) Boolean hasCloseTs,
      @QueryParam(value = "closeFrom") @Parameter(description = "The difference, measured in seconds, between the minimum close time stamp of the task and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long closeFrom,
      @QueryParam(value = "closeTo") @Parameter(description = "The difference, measured in seconds, between the maximum close time stamp of the task and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long closeTo,
      @QueryParam(value = "order") @Parameter(description = "The order in witch the task has to be returned. For each field it has be separated by a ',' and each field can start with '+' (or without it) to order on ascending order, or with the prefix '-' to do on descendant order.", example = "goal.name,-goal.description,+appId", required = false, style = ParameterStyle.FORM, explode = Explode.FALSE) String order,
      @DefaultValue("0") @QueryParam(value = "offset") @Parameter(description = "The index of the first task to return.", example = "4", required = false) int offset,
      @DefaultValue("10") @QueryParam(value = "limit") @Parameter(description = "The number maximum of tasks to return", example = "100", required = false) int limit,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to update a task.
   *
   * @param taskId        identifier of the task to modify.
   * @param body          the new task attributes.
   * @param request       of the query.
   * @param resultHandler to inform of the response.
   */
  @PUT
  @Path("/{taskId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify a task", description = "Change a task")
  @RequestBody(description = "The new values for the task", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "200", description = "The updated task", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "400", description = "Bad task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void updateTask(
      @PathParam("taskId") @Parameter(description = "The identifier of the task to update", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to modify a task.
   *
   * @param taskId        identifier of the task to modify.
   * @param body          the new task attributes.
   * @param request       of the query.
   * @param resultHandler to inform of the response.
   */
  @PATCH
  @Path("/{taskId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify partially a task", description = "Change some attributes of a task")
  @RequestBody(description = "The new values for the task", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "200", description = "The merged task", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "400", description = "Bad task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void mergeTask(
      @PathParam("taskId") @Parameter(description = "The identifier of the task to merge", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to delete a task.
   *
   * @param taskId        identifier of the task to delete.
   * @param request       of the query.
   * @param resultHandler to inform of the response.
   */
  @DELETE
  @Path("/{taskId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Delete the task associated to the identifier", description = "Allow to delete a task associated to an identifier")
  @ApiResponse(responseCode = "204", description = "The task was deleted successfully")
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void deleteTask(@PathParam("taskId") @Parameter(description = "The identifier of the task to delete") String taskId,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to do a transaction over a task.
   *
   * @param body          the new task transaction to do.
   * @param request       of the query.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Transactions")
  @POST
  @Path(TRANSACTIONS_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Do a task transaction", description = "Called when when to do an action over a task")
  @RequestBody(description = "The task transaction to do", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/TaskTransaction")))
  @ApiResponse(responseCode = "200", description = "The started task transaction", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/TaskTransaction")))
  @ApiResponse(responseCode = "400", description = "Bad task transaction", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void doTaskTransaction(@Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Add a task transaction into a task.
   *
   * @param taskId        identifier of the task to add the transaction.
   * @param body          the task transaction to add.
   * @param request       of the query.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Transactions")
  @POST
  @Path("/{taskId}" + TRANSACTIONS_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Add a transaction into a task", description = "Called to add a transaction into a task")
  @RequestBody(description = "The task transaction to add", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/TaskTransaction")))
  @ApiResponse(responseCode = "201", description = "The added task transaction", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/TaskTransaction")))
  @ApiResponse(responseCode = "400", description = "Bad task transaction", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task to add the transaction", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void addTransactionIntoTask(
      @PathParam("taskId") @Parameter(description = "The identifier of the task to add the transaction") String taskId,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Add a message into task transaction.
   *
   * @param taskId            identifier of the task where is the transaction.
   * @param taskTransactionId identifier of the task transaction to add the
   *                          message.
   * @param body              the message to add.
   * @param request           of the query.
   * @param resultHandler     to inform of the response.
   */
  @Tag(name = "Task Transactions")
  @POST
  @Path("/{taskId}" + TRANSACTIONS_PATH + "/{taskTransactionId}" + MESSAGES_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Add a message into a transaction", description = "Called to add a message into a transaction")
  @RequestBody(description = "The message to add", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Message")))
  @ApiResponse(responseCode = "201", description = "The added message", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c28427ce0c05596ef9001ffa8a08f8eb125611f/sources/wenet-models-openapi.yaml#/components/schemas/Message")))
  @ApiResponse(responseCode = "400", description = "Bad message", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task or transaction", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void addMessageIntoTransaction(
      @PathParam("taskId") @Parameter(description = "The identifier of the task where is the transaction") String taskId,
      @PathParam("taskTransactionId") @Parameter(description = "The identifier of the transaction to add the message") String taskTransactionId,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

}
