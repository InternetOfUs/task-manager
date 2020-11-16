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

import http from 'k6/http';
import { group, check } from 'k6';

/**
 * Test to check the performance of the task manager.
 */
export default function() {

	var taskManagerApi = 'https://wenet.u-hopper.com/dev/task_manager';
	if (typeof __ENV.TASK_MANAGER_API === 'string') {

		taskManagerApi = __ENV.TASK_MANAGER_API;
	}

	group('task manager performance', function() {

		let taskType;
		group('create task type', function() {

			var taskTypeToCreate = {
				name: 'k6 task type test',
				transactions: [
					{
						label: 'k6_transaction_label'
					}
				]
			};
			var createPayload = JSON.stringify(taskTypeToCreate);
			var createParams = {
				headers: {
					'Content-Type': 'application/json',
				}
			};
			var createResponse = http.post(taskManagerApi + '/tasks', createPayload, createParams);
			check(createResponse, {
				'created task': (r) => r.status === 201,
				'obtain created task': (r) => {
					taskType = r.json();
					return taskType !== undefined;
				}
			});
		});

		group('retrieve task types', function() {

			var retrieveResponse = http.get(taskManagerApi + '/taskTypes/' + taskType.id);
			check(retrieveResponse, {
				'retrieved task type': (r) => r.status === 200,
				'validate task type': (r) => {
					var received = JSON.stringify(r.json()).split('').sort().join('');
					var expected = JSON.stringify(taskType).split('').sort().join('');
					return received == expected;
				}
			});
			var retrievePageResponse = http.get(taskManagerApi + '/tasks');
			check(retrievePageResponse, {
				'retrieved page': (r) => r.status === 200,
				'validate page': (r) => {

					var page = r.json();
					if (page.total == 0) {

						return false;

					} else if (page.total > page.taskTypes.length) {

						return true;

					} else {

						var expected = JSON.stringify(taskType).split('').sort().join('');
						for (var i = page.taskTypes.length - 1; i >= 0; i--) {

							var received = JSON.stringify(page.taskTypes[i]).split('').sort().join('');
							if (received == expected) {

								return true;
							}

						}

						return false;
					}
				}
			});

		});

		group('delete task type', function() {

			var deleteResponse = http.del(taskManagerApi + '/taskTypes/' + taskType.id);
			check(deleteResponse, {
				'deleted task type': (r) => r.status === 204
			});
		});
	});

}