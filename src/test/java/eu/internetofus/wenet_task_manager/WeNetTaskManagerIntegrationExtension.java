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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.Network;

import eu.internetofus.common.AbstractMain;
import eu.internetofus.common.AbstractWeNetModuleIntegrationExtension;
import eu.internetofus.common.Containers;
import eu.internetofus.common.WeNetModuleContext;
import eu.internetofus.wenet_task_manager.persistence.TasksRepository;

/**
 * Extension used to run integration tests over the WeNet task manager.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class WeNetTaskManagerIntegrationExtension extends AbstractWeNetModuleIntegrationExtension {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		final Class<?> type = parameterContext.getParameter().getType();
		return super.supportsParameter(parameterContext, extensionContext) || type == TasksRepository.class;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		final Class<?> type = parameterContext.getParameter().getType();
		if (type == TasksRepository.class) {

			return extensionContext.getStore(ExtensionContext.Namespace.create(this.getClass().getName()))
					.getOrComputeIfAbsent(TasksRepository.class.getName(), key -> {

						final WeNetModuleContext context = this.getContext();
						return TasksRepository.createProxy(context.vertx);
					}, TasksRepository.class);

		} else {

			return super.resolveParameter(parameterContext, extensionContext);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] createMainStartArguments() {

		final Network network = Network.newNetwork();

		final int profileManagerApiPort = Containers.nextFreePort();
		final int taskManagerApiPort = Containers.nextFreePort();
		final int interactionProtocolEngineApiPort = Containers.nextFreePort();
		final int serviceApiPort = Containers.nextFreePort();
		Testcontainers.exposeHostPorts(profileManagerApiPort, taskManagerApiPort, interactionProtocolEngineApiPort,
				serviceApiPort);

		Containers.createAndStartContainersForProfileManager(profileManagerApiPort, taskManagerApiPort,
				interactionProtocolEngineApiPort, network);
		Containers.createAndStartContainersForInteractionProtocolEngine(interactionProtocolEngineApiPort,
				profileManagerApiPort, taskManagerApiPort, serviceApiPort, network);
		Containers.createAndStartServiceApiSimulator(serviceApiPort);

		return Containers.createTaskManagerContainersToStartWith(taskManagerApiPort, profileManagerApiPort,
				interactionProtocolEngineApiPort, serviceApiPort, network);

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see Main
	 */
	@Override
	protected AbstractMain createMain() {

		return new Main();
	}

}
