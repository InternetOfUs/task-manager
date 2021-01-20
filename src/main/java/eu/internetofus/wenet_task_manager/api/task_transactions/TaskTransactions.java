/*
 * -----------------------------------------------------------------------------
 *
 * Copyright (c) 1994 - 2021 UDT-IA, IIIA-CSIC
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
package eu.internetofus.wenet_task_manager.api.task_transactions;

import eu.internetofus.common.components.ErrorMessage;
import eu.internetofus.common.components.task_manager.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * The definition of the web services to manage the {@link TaskTransactions}.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Path(TaskTransactions.PATH)
@Tag(name = "Task Transactions")
@WebApiServiceGen
public interface TaskTransactions {

  /**
   * The path to the tasks resource.
   */
  String PATH = "/taskTransactions";

  /**
   * The address of this service.
   */
  String ADDRESS = "wenet_task_manager.api.task_transactions";

  /**
   * Called when want to get the information of some tasks.
   *
   * @param appId            application identifier to match for the tasks where
   *                         are the transactions to return.
   * @param requesterId      requester identifier to match for the tasks where are
   *                         the transactions to return.
   * @param taskTypeId       task type identifier to match for the tasks where are
   *                         the transactions to return.
   * @param goalName         pattern to match with the goal name of the tasks
   *                         where are the transactions to return.
   * @param goalDescription  pattern to match with the goal description of the
   *                         tasks where are the transactions to return.
   * @param taskCreationFrom minimal creation time stamp of the tasks where are
   *                         the transactions to return.
   * @param taskCreationTo   maximal creation time stamp of the tasks where are
   *                         the transactions to return.
   * @param taskUpdateFrom   minimal update time stamp of the tasks where are the
   *                         transactions to return.
   * @param taskUpdateTo     maximal update time stamp of the tasks where are the
   *                         transactions to return.
   * @param hasCloseTs       this is {@code true} if the tasks to return need to
   *                         have a {@link Task#closeTs}
   * @param closeFrom        minimal close time stamp of the tasks where are the
   *                         transactions to return.
   * @param closeTo          maximal close time stamp of the tasks where are the
   *                         transactions to return.
   * @param taskId           identifier of the task where are the transactions to
   *                         return.
   * @param label            of the transactions to return.
   * @param actioneerId      identifier of the user that done the transactions to
   *                         return.
   * @param creationFrom     minimal creation time stamp of the transactions to
   *                         return.
   * @param creationTo       maximal creation time stamp of the transactions to
   *                         return.
   * @param updateFrom       minimal update time stamp of the transactions to
   *                         return.
   * @param updateTo         maximal update time stamp of the transactions to
   *                         return.
   * @param order            of the tasks to return.
   * @param offset           index of the first task to return.
   * @param limit            number maximum of tasks to return.
   * @param request          of the query.
   * @param resultHandler    to inform of the response.
   */
  @Tag(name = "Tasks")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Search for some transactions", description = "Allow to get a page of transactions with the specified query parameters.")
  @ApiResponse(responseCode = "200", description = "The page with the matching transactions", content = @Content(schema = @Schema(implementation = TaskTransactionsPage.class)))
  @ApiResponse(responseCode = "400", description = "If any of the search pattern is not valid", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  void retrieveTaskTransactionsPage(
      @QueryParam(value = "appId") @Parameter(description = "An application identifier to be equals on the task where are the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the application identifier of the tasks if you write between '/'. For example to get the transactions on the tasks for the applications '1' and '2' you must pass as 'appId' '/^[1|2]$/'.", example = "1", required = false) String appId,
      @QueryParam(value = "requesterId") @Parameter(description = "An user identifier to be equals on the task where are the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the requester identifier of the tasks if you write between '/'. For example to get the transactions on the tasks for the requesters '1' and '2' you must pass as 'requesterId' '/^[1|2]$/'.", example = "1e346fd440", required = false) String requesterId,
      @QueryParam(value = "taskTypeId") @Parameter(description = "A task type identifier to be equals on the task where are the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the task type identifier of the tasks if you write between '/'. For example to get the transactions on the tasks for the types '1' and '2' you must pass as 'taskTypeId' '/^[1|2]$/'.", example = "1e346fd440", required = false) String taskTypeId,
      @QueryParam(value = "goalName") @Parameter(description = "A goal name to be equals on the task where are the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the goal name of the tasks if you write between '/'. For example to get the transactions on the tasks with a goal name with the word 'eat' you must pass as 'goalName' '/.*eat.*/'", example = "/.*eat.*/", required = false) String goalName,
      @QueryParam(value = "goalDescription") @Parameter(description = "A goal description to be equals on the task where are the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the goal description of the tasks if you write between '/'. For example to get the transactions on the tasks with a goal description with the word 'eat' you must pass as 'goalDescription' '/.*eat.*/'", example = "/.*eat.*/", required = false) String goalDescription,
      @QueryParam(value = "taskCreationFrom") @Parameter(description = "The difference, measured in seconds, between the minimum creation time stamp of the task where are the transaction to return and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long taskCreationFrom,
      @QueryParam(value = "taskCreationTo") @Parameter(description = "The difference, measured in seconds, between the maximum creation time stamp of the task where are the transaction to return and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long taskCreationTo,
      @QueryParam(value = "taskUpdateFrom") @Parameter(description = "The difference, measured in seconds, between the minimum update time stamp of the task where are the transaction to return and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long taskUpdateFrom,
      @QueryParam(value = "taskUpdateTo") @Parameter(description = "The difference, measured in seconds, between the maximum update time stamp of the task where are the transaction to return and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long taskUpdateTo,
      @QueryParam(value = "hasCloseTs") @Parameter(description = "This is 'true' if the task where are the transaction to return has defined a 'closeTs', or 'false' if this fiels is not defined. In other words, get the transaction from closed or open tasks.", example = "false", required = false) Boolean hasCloseTs,
      @QueryParam(value = "closeFrom") @Parameter(description = "The difference, measured in seconds, between the minimum close time stamp of the task where are the transaction to return and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long closeFrom,
      @QueryParam(value = "closeTo") @Parameter(description = "The difference, measured in seconds, between the maximum close time stamp of the task where are the transaction to return and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long closeTo,
      @QueryParam(value = "taskId") @Parameter(description = "A task identifier to be equals on the task where are the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the task identifier of the tasks if you write between '/'. For example to get the transactions for the tasks '1' and '2' you must pass as 'taskId' '/^[1|2]$/'.", example = "1e346fd440", required = false) String taskId,
      @QueryParam(value = "label") @Parameter(description = "A label to be equals on the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the label on the transactions to return if you write between '/'. For example to get the transactions with the labels 'accept' and 'decline' you must pass as 'label' '/^[accept|decline]$/'.", example = "accept", required = false) String label,
      @QueryParam(value = "actioneerId") @Parameter(description = "A user identifier that has done the transactions to return. You can use a Perl compatible regular expressions (PCRE) that has to match the actioneer if you write between '/'. For example to get the transactions that has done the users '1' and '2' you must pass as 'actioneerId' '/^[1|2]$/'.", example = "accept", required = false) String actioneerId,
      @QueryParam(value = "creationFrom") @Parameter(description = "The difference, measured in seconds, between the minimum creation time stamp of the transactions to return and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long creationFrom,
      @QueryParam(value = "creationTo") @Parameter(description = "The difference, measured in seconds, between the maximum creation time stamp of the transactions to return and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long creationTo,
      @QueryParam(value = "updateFrom") @Parameter(description = "The difference, measured in seconds, between the minimum update time stamp of the transactions to return and midnight, January 1, 1970 UTC.", example = "1457166440", required = false) Long updateFrom,
      @QueryParam(value = "updateTo") @Parameter(description = "The difference, measured in seconds, between the maximum update time stamp of the transactions to return and midnight, January 1, 1970 UTC.", example = "1571664406", required = false) Long updateTo,
      @QueryParam(value = "order") @Parameter(description = "The order in witch the task transactions have to be returned. For each field it has be separated by a ',' and each field can start with '+' (or without it) to order on ascending order, or with the prefix '-' to do on descendant order.", example = "goal.name,-goal.description,+appId", required = false, style = ParameterStyle.FORM, explode = Explode.FALSE) String order,
      @DefaultValue("0") @QueryParam(value = "offset") @Parameter(description = "The index of the first transaction to return.", example = "4", required = false) int offset,
      @DefaultValue("10") @QueryParam(value = "limit") @Parameter(description = "The number maximum of transactions to return", example = "100", required = false) int limit,
      @Parameter(hidden = true, required = false) ServiceRequest request,
      @Parameter(hidden = true, required = false) Handler<AsyncResult<ServiceResponse>> resultHandler);

}
