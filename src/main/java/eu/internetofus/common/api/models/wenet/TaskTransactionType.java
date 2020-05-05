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

package eu.internetofus.common.api.models.wenet;

import java.util.List;

import eu.internetofus.common.api.models.Model;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The description of a type of task transaction.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Schema(hidden = true, name = "TaskTransactionType", description = "Describe a possible task transaction.")
public class TaskTransactionType extends Model {

	/**
	 * A label that identify the type.
	 */
	@Schema(description = "A label that identify the transaction.", example = "acceptVolunteer")
	public String label;

	/**
	 * A name that identify the type.
	 */
	@Schema(
			description = "A human readable description of the task transaction type.",
			example = "Accept to be volunteer of a task")
	public String description;

	/**
	 * The attribute that has to be instantiated when create the task transaction of
	 * this type.
	 */
	@ArraySchema(
			schema = @Schema(implementation = TaskAttributeType.class),
			arraySchema = @Schema(
					description = "The attribute that has to be instantiated when create the task transaction of this type."))
	public List<TaskAttributeType> attributes;

}
