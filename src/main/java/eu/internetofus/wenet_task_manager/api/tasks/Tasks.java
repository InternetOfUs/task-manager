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

import java.util.List;

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

import eu.internetofus.common.components.ErrorMessage;
import eu.internetofus.common.components.task_manager.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
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
   * The path to the tasks resource.
   */
  String PATH = "/tasks";

  /**
   * The address of this service.
   */
  String ADDRESS = "wenet_task_manager.api.tasks";

  /**
   * The sub path to retrieve a task.
   */
  String TASK_ID_PATH = "/{taskId:^(?![types|transactions])}";

  /**
   * The path to the task types resource.
   */
  String TYPES_PATH = "/types";

  /**
   * The path to the task transactions resource.
   */
  String TRANSACTIONS_PATH = "/transactions";

  /**
   * The sub path to retrieve a task type.
   */
  String TASK_TYPE_ID_PATH = "/{taskTypeId}";

  /**
   * Called when want to create a task.
   *
   * @param body          the new task to create.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Create a task", description = "Create a new task")
  @RequestBody(
      description = "The new task to create",
      required = true,
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(
      responseCode = "200",
      description = "The created task",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "400", description = "Bad task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void createTask(@Parameter(hidden = true, required = false) JsonObject body, @Parameter(hidden = true, required = false) OperationRequest context,
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
  @Operation(summary = "Return a task", description = "Allow to get a task with an specific identifier")
  @ApiResponse(
      responseCode = "200",
      description = "The task associated to the identifier",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTask(@PathParam("taskId") @Parameter(description = "The identifier of the task to get", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId, @Parameter(hidden = true, required = false) OperationRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to get the information of some tasks.
   *
   * @param appId           application identifier to match for the tasks to return.
   * @param requesterId     requester identifier to match for the tasks to return.
   * @param taskTypeId      task type identifier to match for the tasks to return.
   * @param goalName        pattern to match with the goal name of the tasks to return.
   * @param goalDescription pattern to match with the goal description of the tasks to return.
   * @param startFrom       minimal start time stamp of the tasks to return.
   * @param startTo         maximal start time stamp of the tasks to return.
   * @param endFrom         minimal end time stamp of the tasks to return.
   * @param endTo           maximal end time stamp of the tasks to return.
   * @param deadlineFrom    minimal deadline time stamp of the tasks to return.
   * @param deadlineTo      maximal deadline time stamp of the tasks to return.
   * @param hasCloseTs      this is {@code true} if the tasks to return need to have a {@link Task#closeTs}
   * @param closeFrom       minimal close time stamp of the tasks to return.
   * @param closeTo         maximal close time stamp of the tasks to return.
   * @param order           of the tasks to return.
   * @param offset          index of the first task to return.
   * @param limit           number maximum of tasks to return.
   * @param context         of the request.
   * @param resultHandler   to inform of the response.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Search for some tasks", description = "Allow to get a page of task with the specified query parameters.")
  @ApiResponse(responseCode = "200", description = "The task associated to the identifier", content = @Content(schema = @Schema(implementation = TasksPage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTasksPage(
      @QueryParam(value = "appId") @Parameter(
          description = "An application identifier to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the application identifier of the tasks to return if you write between '/'. For example to get the tasks for the aplications '1' and '2' you must pass as 'appId' '/^[1|2]$/'.",
          example = "1",
          required = false) String appId,
      @QueryParam(value = "requesterId") @Parameter(
          description = "An user identifier to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the requester identifier of the tasks to return if you write between '/'. For example to get the tasks for the requesters '1' and '2' you must pass as 'requesterId' '/^[1|2]$/'.",
          example = "1e346fd440",
          required = false) String requesterId,
      @QueryParam(value = "taskTypeId") @Parameter(
          description = "A task type identifier to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the task type identifier of the tasks to return if you write between '/'. For example to get the tasks for the types '1' and '2' you must pass as 'taskTypeId' '/^[1|2]$/'.",
          example = "1e346fd440",
          required = false) String taskTypeId,
      @QueryParam(value = "goalName") @Parameter(
          description = "A goal name to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the goal name of the tasks to return if you write between '/'. For example to get the tasks with a goal name with the word 'eat' you must pass as 'goalName' '/.*eat.*/'",
          example = "/.*eat.*/",
          required = false) String goalName,
      @QueryParam(value = "goalDescription") @Parameter(
          description = "A goal description to be equals on the tasks to return. You can use a Perl compatible regular expressions (PCRE) that has to match the goal description of the tasks to return if you write between '/'. For example to get the tasks with a goal description with the word 'eat' you must pass as 'goalDescription' '/.*eat.*/'",
          example = "/.*eat.*/",
          required = false) String goalDescription,
      @QueryParam(value = "startFrom") @Parameter(
          description = "The difference, measured in seconds, between the minimum start time stamp of the task and midnight, January 1, 1970 UTC.",
          example = "1457166440",
          required = false) Long startFrom,
      @QueryParam(value = "startTo") @Parameter(
          description = "The difference, measured in seconds, between the maximum start time stamp of the task and midnight, January 1, 1970 UTC.",
          example = "1571664406",
          required = false) Long startTo,
      @QueryParam(value = "endFrom") @Parameter(description = "The difference, measured in seconds, between the minimum end time stamp of the task and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long endFrom,
      @QueryParam(value = "endTo") @Parameter(description = "The difference, measured in seconds, between the maximum end time stamp of the task and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long endTo,
      @QueryParam(value = "deadlineFrom") @Parameter(
          description = "The difference, measured in seconds, between the minimum deadline time stamp of the task and midnight, January 1, 1970 UTC.",
          example = "1457166440",
          required = false) Long deadlineFrom,
      @QueryParam(value = "deadlineTo") @Parameter(
          description = "The difference, measured in seconds, between the maximum deadline time stamp of the task and midnight, January 1, 1970 UTC.",
          example = "1571664406",
          required = false) Long deadlineTo,
      @QueryParam(value = "hasCloseTs") @Parameter(
          description = "This is 'true' if the task to return has defined a 'closeTs', or 'false' if this fiels is not defined. In other words, get the closed or open tasks.",
          example = "false",
          required = false) Boolean hasCloseTs,
      @QueryParam(value = "closeFrom") @Parameter(
          description = "The difference, measured in seconds, between the minimum close time stamp of the task and midnight, January 1, 1970 UTC.",
          example = "1457166440",
          required = false) Long closeFrom,
      @QueryParam(value = "closeTo") @Parameter(
          description = "The difference, measured in seconds, between the maximum close time stamp of the task and midnight, January 1, 1970 UTC.",
          example = "1571664406",
          required = false) Long closeTo,
      @QueryParam(value = "order") @Parameter(
          description = "The order in witch the task has to be returned. For each filed it has be separated by a ',' and each field can start with '+' (or without it) to order on ascending order, or with the prefix '-' to do on descendant order.",
          example = "goal.name,-goal.description,+appId",
          required = false,
          explode = Explode.FALSE) List<String> order,
      @DefaultValue("0") @QueryParam(value = "offset") @Parameter(description = "The index of the first task to return.", example = "4", required = false) int offset,
      @DefaultValue("10") @QueryParam(value = "limit") @Parameter(description = "The number maximum of tasks to return", example = "100", required = false) int limit, @Parameter(hidden = true, required = false) OperationRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to update a task.
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
  @Operation(summary = "Modify a task", description = "Change a task")
  @RequestBody(
      description = "The new values for the task",
      required = true,
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(
      responseCode = "200",
      description = "The updated task",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "400", description = "Bad task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void updateTask(@PathParam("taskId") @Parameter(description = "The identifier of the task to update", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId, @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) OperationRequest context, @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to modify a task.
   *
   * @param taskId        identifier of the task to modify.
   * @param body          the new task attributes.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @PATCH
  @Path(TASK_ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify partially a task", description = "Change some attributes of a task")
  @RequestBody(
      description = "The new values for the task",
      required = true,
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(
      responseCode = "200",
      description = "The merged task",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/Task")))
  @ApiResponse(responseCode = "400", description = "Bad task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void mergeTask(@PathParam("taskId") @Parameter(description = "The identifier of the task to merge", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskId, @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) OperationRequest context, @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

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
  @Operation(summary = "Delete the task associated to the identifier", description = "Allow to delete a task associated to an identifier")
  @ApiResponse(responseCode = "204", description = "The task was deleted successfully")
  @ApiResponse(responseCode = "404", description = "Not found task", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void deleteTask(@PathParam("taskId") @Parameter(description = "The identifier of the task to delete") String taskId, @Parameter(hidden = true, required = false) OperationRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to create a task type.
   *
   * @param body          the new task type to create.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Types")
  @POST
  @Path(TYPES_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Create a task type", description = "Create a new task type")
  @RequestBody(
      description = "The new task type to create",
      required = true,
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(
      responseCode = "200",
      description = "The created task type",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "400", description = "Bad task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void createTaskType(@Parameter(hidden = true, required = false) JsonObject body, @Parameter(hidden = true, required = false) OperationRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to get a task type.
   *
   * @param taskTypeId    identifier of the task type type to get.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Types")
  @GET
  @Path(TYPES_PATH + TASK_TYPE_ID_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return a task type associated to the identifier", description = "Allow to get a task type associated to an identifier")
  @ApiResponse(
      responseCode = "200",
      description = "The task type associated to the identifier",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTaskType(@PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to get", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) OperationRequest context, @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to search for some task types.
   *
   * @param name          the pattern to match with the names of the task types.
   * @param description   the pattern to match with the descriptions of the task types.
   * @param keywords      the patterns to match with the keywords of the task types.
   * @param order         to return the found task types.
   * @param offset        index of the first task type to return.
   * @param limit         number maximum of task types to return.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Types")
  @GET
  @Path(TYPES_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return a task type associated to the identifier", description = "Allow to get a task type associated to an identifier")
  @ApiResponse(responseCode = "200", description = "The task types that match the search.", content = @Content(schema = @Schema(implementation = TaskTypesPage.class)))
  @ApiResponse(responseCode = "400", description = "If any of the search pattern is not valid", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTaskTypesPage(
      @QueryParam(value = "name") @Parameter(
          description = "A name to be equals on the task types to return. You can use a Perl compatible regular expressions (PCRE) that has to match the name of the task types to return if you write between '/'. For example to get the task types with a name with the word 'eat' you must pass as 'goalName' '/.*eat.*/'",
          example = "/.*eat.*/",
          required = false) String name,
      @QueryParam(value = "description") @Parameter(
          description = "A description to be equals on the task types to return. You can use a Perl compatible regular expressions (PCRE) that has to match the description of the task types to return if you write between '/'. For example to get the task types with a description with the word 'eat' you must pass as 'goalDescription' '/.*eat.*/'",
          example = "/.*eat.*/",
          required = false) String description,
      @QueryParam(value = "keywords") @Parameter(
          description = "A set of keywords to be defined on the task types to be returned. For each keyword is separated by a ',' and each field keyword can be between '/' to use a Perl compatible regular expressions (PCRE) instead the exact value.",
          example = "key1,/.*eat.*/,key3",
          required = false,
          explode = Explode.FALSE) List<String> keywords,
      @QueryParam(value = "order") @Parameter(
          description = "The order in witch the task types has to be returned. For each filed it has be separated by a ',' and each field can start with '+' (or without it) to order on ascending order, or with the prefix '-' to do on descendant order.",
          example = "name,-description",
          required = false,
          explode = Explode.FALSE) List<String> order,
      @QueryParam("offset") @Parameter(description = "The index of the first task type to return") int offset, @QueryParam("limit") @Parameter(description = "The number maximum of task types to return") int limit,
      @Parameter(hidden = true, required = false) OperationRequest context, @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to modify a task type.
   *
   * @param taskTypeId    identifier of the task type to modify.
   * @param body          the new task attributes.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Types")
  @PUT
  @Path(TYPES_PATH + TASK_TYPE_ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify a task type", description = "Change the attributes of a task type")
  @RequestBody(
      description = "The new values for the task type",
      required = true,
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(
      responseCode = "200",
      description = "The updated task type",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "400", description = "Bad task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void updateTaskType(@PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to update", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) JsonObject body, @Parameter(hidden = true, required = false) OperationRequest context, @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to modify a task type.
   *
   * @param taskTypeId    identifier of the task type to modify.
   * @param body          the new task attributes.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Types")
  @PATCH
  @Path(TYPES_PATH + TASK_TYPE_ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify a task type", description = "Change the attributes of a task type")
  @RequestBody(
      description = "The new values for the task type",
      required = true,
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(
      responseCode = "200",
      description = "The merged task type",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "400", description = "Bad task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void mergeTaskType(@PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to merge", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) JsonObject body, @Parameter(hidden = true, required = false) OperationRequest context, @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to delete a task type.
   *
   * @param taskTypeId    identifier of the task type to delete.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Types")
  @DELETE
  @Path(TYPES_PATH + TASK_TYPE_ID_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Delete the task type associated to the identifier", description = "Allow to delete a task type associated to an identifier")
  @ApiResponse(responseCode = "204", description = "The task type was deleted successfully")
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void deleteTaskType(@PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to delete") String taskTypeId, @Parameter(hidden = true, required = false) OperationRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

  /**
   * Called when want to do a transaction over a task.
   *
   * @param body          the new task transaction to do.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @Tag(name = "Task Transactions")
  @POST
  @Path(TRANSACTIONS_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Do a task transaction", description = "Called when when to do an action over a task")
  @RequestBody(
      description = "The task transaction to do",
      required = true,
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskTransaction")))
  @ApiResponse(
      responseCode = "200",
      description = "The started task transaction",
      content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-models-openapi.yaml#/components/schemas/TaskTransaction")))
  @ApiResponse(responseCode = "400", description = "Bad task transaction", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void doTaskTransaction(@Parameter(hidden = true, required = false) JsonObject body, @Parameter(hidden = true, required = false) OperationRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<OperationResponse>> resultHandler);

}
