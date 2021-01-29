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

package eu.internetofus.common.components.incentive_server;

import static eu.internetofus.common.components.AbstractComponentMocker.createClientWithDefaultSession;
import static eu.internetofus.common.components.ValidationsTest.assertIsNotValid;
import static eu.internetofus.common.components.ValidationsTest.assertIsValid;

import eu.internetofus.common.components.ModelTestCase;
import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.ValidationsTest;
import eu.internetofus.common.components.profile_manager.WeNetProfileManager;
import eu.internetofus.common.components.profile_manager.WeNetProfileManagerMocker;
import eu.internetofus.common.components.profile_manager.WeNetUserProfile;
import eu.internetofus.common.components.service.App;
import eu.internetofus.common.components.service.WeNetService;
import eu.internetofus.common.components.service.WeNetServiceSimulator;
import eu.internetofus.common.components.service.WeNetServiceSimulatorMocker;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test the {@link Incentive}.
 *
 * @see Incentive
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class IncentiveTest extends ModelTestCase<Incentive> {

  /**
   * The profile manager mocked server.
   */
  protected static WeNetProfileManagerMocker profileManagerMocker;

  /**
   * The service mocked server.
   */
  protected static WeNetServiceSimulatorMocker serviceMocker;

  /**
   * Start the mocker server.
   */
  @BeforeAll
  public static void startMockers() {

    profileManagerMocker = WeNetProfileManagerMocker.start();
    serviceMocker = WeNetServiceSimulatorMocker.start();
  }

  /**
   * Stop the mocker server.
   */
  @AfterAll
  public static void stopMockers() {

    profileManagerMocker.stopServer();
    serviceMocker.stopServer();
  }

  /**
   * Register the necessary services before to test.
   *
   * @param vertx event bus to register the necessary services.
   */
  @BeforeEach
  public void registerServices(final Vertx vertx) {

    final var client = createClientWithDefaultSession(vertx);
    final var profileConf = profileManagerMocker.getComponentConfiguration();
    WeNetProfileManager.register(vertx, client, profileConf);

    final var conf = serviceMocker.getComponentConfiguration();
    WeNetService.register(vertx, client, conf);
    WeNetServiceSimulator.register(vertx, client, conf);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Incentive createModelExample(final int index) {

    final var model = new Incentive();
    model.AppID = "AppID" + index;
    model.UserId = "UserId" + index;
    model.IncentiveType = "incentive type" + index;
    model.Issuer = "Issuer" + index;
    model.Message = new MessageTest().createModelExample(index);
    model.Badge = new BadgeTest().createModelExample(index);
    return model;
  }

  /**
   * Create an example model that has the specified index.
   *
   * @param index       to use in the example.
   * @param vertx       event bus to use.
   * @param testContext test context to use.
   *
   * @return the created incentive.
   */
  public Future<Incentive> createModelExample(final int index, final Vertx vertx, final VertxTestContext testContext) {

    return testContext
        .assertComplete(StoreServices.storeProfile(new WeNetUserProfile(), vertx, testContext).compose(profile -> {

          return StoreServices.storeApp(new App(), vertx, testContext).compose(app -> {

            final var model = this.createModelExample(index);
            model.UserId = profile.id;
            model.AppID = app.appId;
            return Future.succeededFuture(model);

          });

        }));

  }

  /**
   * Check that the {@link #createModelExample(int)} is not valid.
   *
   * @param index       to verify
   * @param vertx       event bus to use.
   * @param testContext test context to use.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @ParameterizedTest(name = "The model example {0} has to be valid")
  @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
  public void shouldExampleNoBeValid(final int index, final Vertx vertx, final VertxTestContext testContext) {

    final var model = this.createModelExample(index);
    assertIsNotValid(model, vertx, testContext);

  }

  /**
   * Check that the {@link #createModelExample(int, Vertx, VertxTestContext)} is
   * valid.
   *
   * @param index       to verify
   *
   * @param vertx       event bus to use.
   * @param testContext test context to use.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @ParameterizedTest(name = "The model example {0} has to be valid")
  @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
  public void shouldExampleBeValid(final int index, final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(index, vertx, testContext).onSuccess(model -> assertIsValid(model, vertx, testContext));

  }

  /**
   * Check that not accept a large appId.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithALargeAppId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.AppID = ValidationsTest.STRING_256;
      assertIsNotValid(model, "AppID", vertx, testContext);
    });

  }

  /**
   * Check that not accept an undefined appId.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithAnUndefinedAppId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.AppID = "Undefined";
      assertIsNotValid(model, "AppID", vertx, testContext);
    });

  }

  /**
   * Check that not accept a large userId.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithALargeUserId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.UserId = ValidationsTest.STRING_256;
      assertIsNotValid(model, "UserId", vertx, testContext);
    });

  }

  /**
   * Check that not accept an undefined userId.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithAnUndefinedUserId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.UserId = "Undefined";
      assertIsNotValid(model, "UserId", vertx, testContext);
    });

  }

  /**
   * Check that not accept a large issuer.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithALargeIssuer(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.Issuer = ValidationsTest.STRING_256;
      assertIsNotValid(model, "Issuer", vertx, testContext);
    });

  }

  /**
   * Check that not accept a large message.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithALargeMessage(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.Message.content = ValidationsTest.STRING_1024;
      assertIsNotValid(model, "Message.content", vertx, testContext);
    });

  }

  /**
   * Check that not accept a large badge.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithALargeBadge(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.Badge.Message = ValidationsTest.STRING_1024;
      assertIsNotValid(model, "Badge.Message", vertx, testContext);
    });

  }

  /**
   * Check that not accept without message and badge.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see Incentive#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithoutMesageAndBadge(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext).onSuccess(model -> {

      model.Message = null;
      model.Badge = null;
      assertIsNotValid(model, "Message", vertx, testContext);
    });

  }

}
