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

package eu.internetofus.wenet_task_manager.api.messages;

import eu.internetofus.common.model.Model;
import eu.internetofus.common.model.ReflectionModel;
import eu.internetofus.common.components.models.Message;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Contains the found messages.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Schema(title = "MessagesPage", description = "Contains a set of messages")
public class MessagesPage extends ReflectionModel implements Model {

  /**
   * The index of the first task returned.
   */
  @Schema(description = "The index of the first message returned.", example = "0")
  public int offset;

  /**
   * The number total of task that satisfies the search.
   */
  @Schema(description = "The number total of messages that satisfies the search.", example = "100")
  public long total;

  /**
   * The found profiles.
   */
  @ArraySchema(schema = @Schema(ref = "https://raw.githubusercontent.com/InternetOfUs/components-documentation/MODELS_2.4.0/sources/wenet-models-openapi.yaml#/components/schemas/Message"), arraySchema = @Schema(description = "The set of messages found"))
  public List<Message> messages;

}
