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

import java.util.List;

import eu.internetofus.common.api.models.Model;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This describe the attributes and norms that describe the generic goal of a
 * task.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@Schema(description = "This describe the generic goal of a task.")
public class TaskType extends Model {

	/**
	 * The name of the task type.
	 */
	@Schema(description = "The name that identify the task type.", example = "Help me")
	public String name;

	/**
	 * The description of the task type.
	 */
	@Schema(description = "The text that describe the task type.", example = "Help an user to do something")
	public String description;

	/**
	 * The norms that describe the interaction of the users to do the tasks.
	 */
	@ArraySchema(
			schema = @Schema(
					ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c0512480f89ae267d6fc0dcf42db0f3a50d01e8/sources/wenet-models.yaml#/components/schemas/Norm"),
			arraySchema = @Schema(description = "The norms that describe the interaction of the users to do the tasks."))
	public List<Norm> constantNorms;

	/**
	 * The norms that describe the interaction of the users to do the tasks that can
	 * be modified by the requester.
	 */
	@ArraySchema(
			schema = @Schema(
					ref = "https://bitbucket.org/wenet/wenet-components-documentation/raw/5c0512480f89ae267d6fc0dcf42db0f3a50d01e8/sources/wenet-models.yaml#/components/schemas/Norm"),
			arraySchema = @Schema(
					description = "The norms that describe the interaction of the users to do the tasks that can be modified by the requester."))
	public List<Norm> norms;

	// /**
	// * The attribute that has to be instantiated when create the task.
	// */
	// @ArraySchema(
	// schema = @Schema(implementation = TaskAttributeType.class),
	// arraySchema = @Schema(description = "The attribute that has to be
	// instantiated when create the task."))
	// public List<TaskAttributeType> attributes;
	//
	// /**
	// * The constant attributes that define the task.
	// */
	// @ArraySchema(
	// schema = @Schema(implementation = TaskAttribute.class),
	// arraySchema = @Schema(description = "The constant attributes that define the
	// task."))
	// public List<TaskAttribute> constantAttributes;

}
