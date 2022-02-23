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

package eu.internetofus.wenet_task_manager.api.task_transactions;

import eu.internetofus.common.components.models.TaskTransaction;
import eu.internetofus.common.model.Model;
import eu.internetofus.common.model.ReflectionModel;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Contains the found transactions.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Schema(name = "TaskTransactionsPage", description = "Contains a set of tasks")
public class TaskTransactionsPage extends ReflectionModel implements Model {

  /**
   * The index of the first task transaction returned.
   */
  @Schema(description = "The index of the first transactions returned.", example = "0")
  public int offset;

  /**
   * The number total of task transaction that satisfies the search.
   */
  @Schema(description = "The number total of transactions that satisfies the search.", example = "100")
  public long total;

  /**
   * The found transactions.
   */
  @ArraySchema(schema = @Schema(ref = "https://raw.githubusercontent.com/InternetOfUs/components-documentation/MODELS_2.1.0/sources/wenet-models-openapi.yaml#/components/schemas/TaskTransaction"), arraySchema = @Schema(description = "The set of task transactions found"))
  public List<TaskTransaction> transactions;

}
