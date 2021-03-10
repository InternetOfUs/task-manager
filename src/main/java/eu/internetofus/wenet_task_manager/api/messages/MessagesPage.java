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

package eu.internetofus.wenet_task_manager.api.messages;

import eu.internetofus.common.components.Model;
import eu.internetofus.common.components.ReflectionModel;
import eu.internetofus.common.components.service.Message;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Contains the found messages.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Schema(name = "MessagesPage", description = "Contains a set of messages")
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
  @ArraySchema(schema = @Schema(ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/9fa82be7ae8b46fd2190d8531ee14ff3c1cacf3f/sources/wenet-models-openapi.yaml#/components/schemas/Message"), arraySchema = @Schema(description = "The set of messages found"))
  public List<Message> messages;

}
