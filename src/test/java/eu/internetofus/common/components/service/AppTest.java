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

package eu.internetofus.common.components.service;

import java.util.ArrayList;

import eu.internetofus.common.components.ModelTestCase;
import io.vertx.core.json.JsonObject;

/**
 * Test the {@link App}.
 *
 * @see App
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class AppTest extends ModelTestCase<App> {

  /**
   * {@inheritDoc}
   */
  @Override
  public App createModelExample(final int index) {

    final var model = new App();
    model.appId = "appId_" + index;
    model.appToken = "token_" + index;
    model.messageCallbackUrl = "https://app.endpoint.com/messages/" + index;
    model.metadata = new JsonObject().put("index", index);
    model.allowedPlatforms = new ArrayList<>();
    model.allowedPlatforms.add(new JsonObject().put("platform", index));
    return model;

  }

}
