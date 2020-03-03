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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * A container to start the WeNet profile manager.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class WeNetProfileManagerContainer extends GenericContainer<WeNetProfileManagerContainer> {

	/**
	 * The name of the WeNet profile manager docker container to use.
	 */
	private static final String WENET_PROFILE_MANAGER_DOCKER_NAME = "wenet/profile-manager:0.11.0";

	/**
	 * The post that export the API.
	 */
	private static final int EXPORT_API_PORT = 8080;

	/**
	 * The name of the MongoDB to use by the profile manager.
	 */
	public static final String WENET_PROFILE_MANAGER_DB_NAME = "wenetProfileManagerDB";

	/**
	 * The container with the database used by the profile manager.
	 */
	protected MongoContainer mongoContainer;

	/**
	 * Create a new WeNet profile manager container.
	 */
	public WeNetProfileManagerContainer() {

		super(WENET_PROFILE_MANAGER_DOCKER_NAME);

		final Network network = Network.newNetwork();
		this.withStartupAttempts(1);
		this.waitingFor(Wait.forListeningPort());
		this.withNetwork(network);
		this.mongoContainer = new MongoContainer(WENET_PROFILE_MANAGER_DB_NAME);
		this.mongoContainer.withNetwork(network);
		this.mongoContainer.withNetworkAliases("mongodb");
		this.withEnv("DB_HOST", "mongodb");

	}

	/**
	 * Return the host where the WeNet profile manager API is running.
	 *
	 * @return the name of the host where the API is binded.
	 */
	public String getApiHost() {

		return this.getContainerIpAddress();
	}

	/**
	 * Return the port where the WeNet profile manager API is running.
	 *
	 * @return the name of the port where the API is binded.
	 */
	public int getApiPort() {

		return this.getMappedPort(EXPORT_API_PORT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {

		this.mongoContainer.start();
		super.start();
	}
}
