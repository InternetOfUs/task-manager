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

package eu.internetofus.wenet_task_manager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import eu.internetofus.common.api.models.ValidationErrorException;
import eu.internetofus.wenet_task_manager.api.tasks.Norm;

/**
 * The utility components to merge values.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public interface Merges {

	/**
	 * A function used to merge to models.
	 *
	 * @param <T> model to merge.
	 *
	 * @author UDT-IA, IIIA-CSIC
	 */
	public interface MergeFunction<T> {

		/**
		 * Merge both elements.
		 *
		 * @param target     to merge.
		 * @param source     to merge.
		 * @param codePrefix prefix for the error code.
		 *
		 * @return the merged element.
		 *
		 * @throws ValidationErrorException if the model is not right.
		 */
		T apply(T target, T source, String codePrefix) throws ValidationErrorException;
	}

	/**
	 * A function used to validate a model.
	 *
	 * @param <T> model to merge.
	 *
	 * @author UDT-IA, IIIA-CSIC
	 */
	public interface ValidationFunction<T> {

		/**
		 * Check if a model is right.
		 *
		 * @param model      to validate.
		 * @param codePrefix prefix for the error code.
		 *
		 * @throws ValidationErrorException if the model is not right.
		 */
		void test(T model, String codePrefix) throws ValidationErrorException;
	}

	/**
	 * Merge to lists.
	 *
	 * @param target           list to merge.
	 * @param source           list to merge.
	 * @param codePrefix       prefix for the error code.
	 * @param hasIdentifier    function to check if a model has identifier.
	 * @param equalsIdentifier function to check if two models have the same
	 *                         identifier.
	 * @param merge            function to merge two models.
	 * @param validate         function to validate a model.
	 *
	 * @return the merged list.
	 *
	 * @throws ValidationErrorException if the model is not right.
	 */
	static <T> List<T> mergeList(List<T> target, List<T> source, String codePrefix, Predicate<T> hasIdentifier,
			BiPredicate<T, T> equalsIdentifier, MergeFunction<T> merge, ValidationFunction<T> validate)
			throws ValidationErrorException {

		if (source != null) {

			final List<T> original = new ArrayList<>();
			if (target != null) {

				original.addAll(target);
			}
			final List<T> merged = new ArrayList<>();
			INDEX: for (int index = 0; index < source.size(); index++) {

				final String codeElement = codePrefix + "[" + index + "]";
				final T sourceElement = source.get(index);
				// search if it modify any original model
				if (hasIdentifier.test(sourceElement)) {

					for (int j = 0; j < original.size(); j++) {

						final T targetElement = original.get(j);
						if (equalsIdentifier.test(targetElement, sourceElement)) {

							original.remove(j);
							final T mergedElement = merge.apply(targetElement, sourceElement, codeElement);
							merged.add(mergedElement);
							continue INDEX;
						}

					}
				}

				// not found original model with the same id => check it as new
				validate.test(sourceElement, codeElement);
				merged.add(sourceElement);
			}

			return merged;

		} else {

			return target;
		}
	}

	/**
	 * Merge two list of norms.
	 *
	 * @param targetNorms target norms to merge.
	 * @param sourceNorms source norms to merge.
	 * @param codePrefix  prefix for the error code.
	 *
	 * @throws ValidationErrorException if the model is not right.
	 *
	 * @return the merged norms.
	 */
	static List<Norm> mergeListOfNorms(List<Norm> targetNorms, List<Norm> sourceNorms, String codePrefix)
			throws ValidationErrorException {

		return Merges.mergeList(targetNorms, sourceNorms, codePrefix, norm -> norm.id != null,
				(targetNorm, sourceNorm) -> targetNorm.id.equals(sourceNorm.id),
				(targetNorm, sourceNorm, codeNormPrefix) -> targetNorm.merge(sourceNorm, codeNormPrefix),
				(norm, codeNormPrefix) -> norm.validate(codeNormPrefix));

	}

}
