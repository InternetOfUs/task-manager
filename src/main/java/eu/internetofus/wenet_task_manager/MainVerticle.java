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

import eu.internetofus.common.vertx.AbstractMainVerticle;
import eu.internetofus.wenet_task_manager.api.APIVerticle;
import eu.internetofus.wenet_task_manager.persistence.PersistenceVerticle;
import eu.internetofus.wenet_task_manager.services.ServicesVerticle;
import io.vertx.core.AbstractVerticle;

/**
 * The Main verticle that deploy the necessary verticles for the WeNet task
 * manager.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class MainVerticle extends AbstractMainVerticle {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends AbstractVerticle>[] getVerticleClassesToDeploy() {

		return new Class[] { ServicesVerticle.class, PersistenceVerticle.class, APIVerticle.class };
	}

}
