/*
 * -----------------------------------------------------------------------------
 *
 * Copyright 2019 - 2022 UDT-IA, IIIA-CSIC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -----------------------------------------------------------------------------
 */

package eu.internetofus.wenet_task_manager.api.task_types;

import eu.internetofus.common.components.task_manager.TaskTypesPage;
import eu.internetofus.common.model.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
  @RequestBody(description = "The new task type to create", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "200", description = "The created task type", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
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
  @Path("/{taskTypeId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return a task type associated to the identifier", description = "Allow to get a task type associated to an identifier")
  @ApiResponse(responseCode = "200", description = "The task type associated to the identifier", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
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
  @Path("/{taskTypeId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify a task type", description = "Change the attributes of a task type")
  @RequestBody(description = "The new values for the task type", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "200", description = "The updated task type", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
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
  @Path("/{taskTypeId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Modify a task type", description = "Change the attributes of a task type")
  @RequestBody(description = "The new values for the task type", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
  @ApiResponse(responseCode = "200", description = "The merged task type", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/TaskType")))
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
  @Path("/{taskTypeId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Delete the task type associated to the identifier", description = "Allow to delete a task type associated to an identifier")
  @ApiResponse(responseCode = "204", description = "The task type was deleted successfully")
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void deleteTaskType(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to delete") String taskTypeId,
      @Parameter(hidden = true, required = false) ServiceRequest context,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to get the norms from a task type.
   *
   * @param taskTypeId    identifier of the task type to get all norms.
   * @param request       of the operation.
   * @param resultHandler to inform of the response.
   */
  @GET
  @Path("/{taskTypeId}/norms")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return the norms from a task type", description = "Allow to get all the norms defined into a task type")
  @ApiResponse(responseCode = "200", description = "The norms defined into the task type", content = @Content(array = @ArraySchema(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm"))))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @Tag(name = "Norms")
  void retrieveTaskTypeNorms(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type where the norms are defined", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Called when want to get a norm from a task type.
   *
   * @param taskTypeId    identifier of the task type where the norm is defined.
   * @param index         of the norm to get.
   * @param request       of the operation.
   * @param resultHandler to inform of the response.
   */
  @GET
  @Path("/{taskTypeId}/norms/{index}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return a norm from a tasktype", description = "Allow to get a norm defined into a tasktype")
  @ApiResponse(responseCode = "200", description = "The norm defined into the tasktype", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "404", description = "Not found tasktype or norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @Tag(name = "Norms")
  void retrieveTaskTypeNorm(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type where the norm is defined", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @PathParam("index") @Parameter(description = "The index of the norm to get", example = "1") int index,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Add a norm into a task type.
   *
   * @param taskTypeId    identifier of the task type to add the norm.
   * @param body          the norm to add.
   * @param request       context of the request.
   * @param resultHandler to inform of the response.
   */
  @POST
  @Path("/{taskTypeId}/norms")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Add a norm into a task type", description = "Add a new norm into the norms of a task type")
  @RequestBody(description = "The new norm to add", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "200", description = "The added norm", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "400", description = "Bad norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @Tag(name = "Norms")
  void addTaskTypeNorm(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to add a norm", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Update a norm from a task type.
   *
   * @param taskTypeId    identifier of the task type to put a norm.
   * @param index         identifier of the norm to update.
   * @param body          the norm to update.
   * @param request       context of the request.
   * @param resultHandler to inform of the response.
   */
  @PUT
  @Path("/{taskTypeId}/norms/{index}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Updated a norm from a task type", description = "Update a norm defined in a task type")
  @RequestBody(description = "The new values to update a norm", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "200", description = "The updated norm", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "400", description = "Bad norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type or norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @Tag(name = "Norms")
  void updateTaskTypeNorm(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to update a norm", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @PathParam("index") @Parameter(description = "The identifier of the norm to update", example = "1") int index,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Merge a norm from a task type.
   *
   * @param taskTypeId    identifier of the task type to put a norm.
   * @param index         identifier of the norm to merge.
   * @param body          the norm to merge.
   * @param request       context of the request.
   * @param resultHandler to inform of the response.
   */
  @PATCH
  @Path("/{taskTypeId}/norms/{index}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Merged a norm from a task type", description = "Merge a norm defined in a task type")
  @RequestBody(description = "The new values to merge a norm", required = true, content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "200", description = "The merged norm", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "400", description = "Bad norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type or norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @Tag(name = "Norms")
  void mergeTaskTypeNorm(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to merge a norm", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @PathParam("index") @Parameter(description = "The identifier of the norm to merge", example = "1") int index,
      @Parameter(hidden = true, required = false) JsonObject body,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

  /**
   * Delete a norm from a task type.
   *
   * @param taskTypeId    identifier of the task type to delete the norm.
   * @param index         identifier of the norm to delete.
   * @param request       context of the request.
   * @param resultHandler to inform of the response.
   */
  @DELETE
  @Path("/{taskTypeId}/norms/{index}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Add a norm into a task type", description = "Add a new norm into the norms of a task type")
  @ApiResponse(responseCode = "200", description = "The task type where has added the norm", content = @Content(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/940e9403246417419c8dcce9f3b19c5bb754028b/sources/wenet-models-openapi.yaml#/components/schemas/ProtocolNorm")))
  @ApiResponse(responseCode = "400", description = "Bad norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found task type or norm", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  @Tag(name = "Norms")
  void deleteTaskTypeNorm(
      @PathParam("taskTypeId") @Parameter(description = "The identifier of the task type to delete a norm", example = "15837028-645a-4a55-9aaf-ceb846439eba") String taskTypeId,
      @PathParam("index") @Parameter(description = "The identifier of the norm to delete", example = "1") int index,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

}
