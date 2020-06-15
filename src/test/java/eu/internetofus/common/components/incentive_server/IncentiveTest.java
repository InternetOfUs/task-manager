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

package eu.internetofus.common.components.incentive_server;

import eu.internetofus.common.components.ModelTestCase;

/**
 * Test the {@link Incentive}.
 *
 * @see Incentive
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class IncentiveTest extends ModelTestCase<Incentive>{

  /**
   * {@inheritDoc}
   */
  @Override
  public Incentive createModelExample(final int index) {

    final Incentive model = new Incentive();
    model.AppID = "AppID"+index;
    model.UserId = "UserId"+index;
    model.IncentiveType = "incentive type"+index;
    model.Issuer = "Issuer"+index;
    model.Message = new MessageTest().createModelExample(index);
    model.Badge = new BadgeTest().createModelExample(index);
    return model;
  }

}