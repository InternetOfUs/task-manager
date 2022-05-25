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

package eu.internetofus.wenet_task_manager;

import eu.internetofus.common.protocols.Ask4HelpV3ProtocolThatExpiresWhenReachedDateITC;
import eu.internetofus.common.protocols.DefaultProtocols;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Check the {@link DefaultProtocols#ASK_4_HELP_V3} protocol. ATTENTION: This
 * test is sequential and maintains the state between methods. In other words,
 * you must to run the entire test methods on the specified order to work.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(WeNetTaskManagerIntegrationExtension.class)
public class Ask4HelpV3ProtocolThatExpiresWhenReachedDateIT extends Ask4HelpV3ProtocolThatExpiresWhenReachedDateITC {

}
