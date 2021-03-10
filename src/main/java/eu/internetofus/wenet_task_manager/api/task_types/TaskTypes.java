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

package eu.internetofus.wenet_task_manager.api.task_types;

import eu.internetofus.common.components.ErrorMessage;
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
 * The definition of the web services to manage the {@link TaskTypes}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Path(TaskTypes.PATH)
@Tag(name = "Task Types")
@WebApiServiceGen
public interface TaskTypes {

  /**
   * The path to the tasks resource.
   */
  String PATH = "/taskTypes";

  /**
   * The address of this service.
   */
  String ADDRESS = "wenet_task_manager.api.task_types";

  /**
   * The sub path to retrieve a task type.
   */
  String TASK_TYPE_ID_PATH = "/{taskTypeId}";

  /**
   * Called when want to create a task type.
   *
   * @param body          the new task type to create.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Create a task type", description = "Create a new task type")
  @RequestBody(description = "The new task type to create", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/99249b00800807c94cb973b08c265e0a37f820ab/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "200", description = "The created task type", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/99249b00800807c94cb973b08c265e0a37f820ab/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "400", description = "Bad task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void createTaskType(@Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to get a task type.
   *
   * @param taskTypeId    identifier of the task type type to get.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @GET
  @Path(TASK_TYPE_ID_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return a task type associated to the identifier", description = "Allow to get a task type associated to an identifier")
  @ApiResponse(responseCode = "200", description = "The task type associated to the identifier", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/99249b00800807c94cb973b08c265e0a37f820ab/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTaskType(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to get", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) ServiceRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to search for some task types.
   *
   * @param name          the pattern to match with the names of the task types.
   * @param description   the pattern to match with the descriptions of the task
   *                      types.
   * @param keywords      the patterns to match with the keywords of the task
   *                      types.
   * @param order         to return the found task types.
   * @param offset        index of the first task type to return.
   * @param limit         number maximum of task types to return.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Search for some task types", description = "Allow to get a task type with the specified query parameters")
  @ApiResponse(responseCode = "200", description = "The page with the matching task types.", content = @Content(schema = @Schema(implementation = TaskTypesPage.class)))
  @ApiResponse(responseCode = "400", description = "If any of the search pattern is not valid", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTaskTypesPage(
      @QueryParam(value = "name") @Parameter(description = "A name to be equals on the task types to return. You can use a Perl compatible regular expressions (PCRE) that has to match the name of the task types to return if you write between '/'. For example to get the task types with a name with the word 'eat' you must pass as 'goalName' '/.*eat.*/'", example = "/.*eat.*/", required = false) String name,
      @QueryParam(value = "description") @Parameter(description = "A description to be equals on the task types to return. You can use a Perl compatible regular expressions (PCRE) that has to match the description of the task types to return if you write between '/'. For example to get the task types with a description with the word 'eat' you must pass as 'goalDescription' '/.*eat.*/'", example = "/.*eat.*/", required = false) String description,
      @QueryParam(value = "keywords") @Parameter(description = "A set of keywords to be defined on the task types to be returned. For each keyword is separated by a ',' and each field keyword can be between '/' to use a Perl compatible regular expressions (PCRE) instead the exact value.", example = "key1,/.*eat.*/,key3", required = false, style = ParameterStyle.FORM, explode = Explode.FALSE) String keywords,
      @QueryParam(value = "order") @Parameter(description = "The order in witch the task types has to be returned. For each field it has be separated by a ',' and each field can start with '+' (or without it) to order on ascending order, or with the prefix '-' to do on descendant order.", example = "name,-description", required = false, style = ParameterStyle.FORM, explode = Explode.FALSE) String order,
      @DefaultValue("0") @QueryParam(value = "offset") @Parameter(description = "The index of the first task type to return.", example = "4", required = false) int offset,
      @DefaultValue("10") @QueryParam(value = "limit") @Parameter(description = "The number maximum of task types to return", example = "100", required = false) int limit,
      @Parameter(hidden = true, required = false) ServiceRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to modify a task type.
   *
   * @param taskTypeId    identifier of the task type to modify.
   * @param body          the new task attributes.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @PUT
  @Path(TASK_TYPE_ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify a task type", description = "Change the attributes of a task type")
  @RequestBody(description = "The new values for the task type", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/99249b00800807c94cb973b08c265e0a37f820ab/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "200", description = "The updated task type", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/99249b00800807c94cb973b08c265e0a37f820ab/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "400", description = "Bad task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void updateTaskType(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to update", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to modify a task type.
   *
   * @param taskTypeId    identifier of the task type to modify.
   * @param body          the new task attributes.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @PATCH
  @Path(TASK_TYPE_ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify a task type", description = "Change the attributes of a task type")
  @RequestBody(description = "The new values for the task type", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/99249b00800807c94cb973b08c265e0a37f820ab/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "200", description = "The merged task type", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/99249b00800807c94cb973b08c265e0a37f820ab/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "400", description = "Bad task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void mergeTaskType(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to merge", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to delete a task type.
   *
   * @param taskTypeId    identifier of the task type to delete.
   * @param context       of the request.
   * @param resultHandler to inform of the response.
   */
  @DELETE
  @Path(TASK_TYPE_ID_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Delete the task type associated to the identifier", description = "Allow to delete a task type associated to an identifier")
  @ApiResponse(responseCode = "204", description = "The task type was deleted successfully")
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void deleteTaskType(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to delete") String taskTypeId,
      @Parameter(hidden = true, required = false) ServiceRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

}
