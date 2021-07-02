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

import eu.internetofus.common.model.ModelTestCase;
import eu.internetofus.common.components.models.TaskTransactionTest;
import eu.internetofus.wenet_task_manager.api.tasks.TasksPage;
import java.util.ArrayList;

/**
 * Test the {@link TaskTransactionsPage}.
 *
 * @see TasksPage
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class TaskTransactionsPageTest extends ModelTestCase<TaskTransactionsPage> {

  /**
   * {@inheritDoc}
   */
  @Override
  public TaskTransactionsPage createModelExample(final int index) {

    final var model = new TaskTransactionsPage();
    model.offset = index;
    model.total = 100 + index;
    model.transactions = new ArrayList<>();
    model.transactions.add(new TaskTransactionTest().createModelExample(index));
    return model;
  }

}