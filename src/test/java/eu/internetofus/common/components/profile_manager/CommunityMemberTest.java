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

package eu.internetofus.common.components.profile_manager;

import static eu.internetofus.common.components.MergesTest.assertCanMerge;
import static eu.internetofus.common.components.MergesTest.assertCannotMerge;
import static eu.internetofus.common.components.ValidationsTest.assertIsNotValid;
import static eu.internetofus.common.components.ValidationsTest.assertIsValid;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.internetofus.common.TimeManager;
import eu.internetofus.common.components.ModelTestCase;
import eu.internetofus.common.components.StoreServices;
import eu.internetofus.common.components.ValidationsTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link CommunityMember}.
 *
 * @see CommunityMember
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class CommunityMemberTest extends ModelTestCase<CommunityMember> {

  /**
   * The profile manager mocked server.
   */
  protected static WeNetProfileManagerMocker profileManagerMocker;

  /**
   * Start the mocker server.
   */
  @BeforeAll
  public static void startMocker() {

    profileManagerMocker = WeNetProfileManagerMocker.start();
  }

  /**
   * Register the necessary services before to test.
   *
   * @param vertx event bus to register the necessary services.
   */
  @BeforeEach
  public void registerServices(final Vertx vertx) {

    final WebClient client = WebClient.create(vertx);
    final JsonObject profileManagerConf = profileManagerMocker.getComponentConfiguration();
    WeNetProfileManager.register(vertx, client, profileManagerConf);

  }

  /**
   * Stop the mocker server.
   */
  @AfterAll
  public static void stopMockers() {

    profileManagerMocker.stop();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommunityMember createModelExample(final int index) {

    final CommunityMember model = new CommunityMember();
    model.userId = "User_" + index;
    model.privileges = new ArrayList<>();
    model.privileges.add("privilege_" + index);
    return model;
  }

  /**
   * Create an example model that has the specified index.
   *
   * @param index         to use in the example.
   * @param vertx         event bus to use.
   * @param testContext   test context to use.
   * @param createHandler the component that will manage the created model.
   */
  public void createModelExample(final int index, final Vertx vertx, final VertxTestContext testContext, final Handler<AsyncResult<CommunityMember>> createHandler) {

    StoreServices.storeProfile(new WeNetUserProfile(), vertx, testContext, testContext.succeeding(stored -> {

      final CommunityMember model = this.createModelExample(index);
      model.userId = stored.id;
      createHandler.handle(Future.succeededFuture(model));

    }));

  }

  /**
   * Check that an empty model is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#validate(String, Vertx)
   */
  @Test
  public void shouldEmptyModelNotBeValid(final Vertx vertx, final VertxTestContext testContext) {

    final CommunityMember model = new CommunityMember();
    assertIsNotValid(model, "userId", vertx, testContext);

  }

  /**
   * Check that the {@link #createModelExample(int)} is not valid.
   *
   * @param index       to verify
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#validate(String, Vertx)
   */
  @ParameterizedTest(name = "The model example {0} has to be valid")
  @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
  public void shouldBasicExampleNotBeValid(final int index, final Vertx vertx, final VertxTestContext testContext) {

    final CommunityMember model = this.createModelExample(index);
    assertIsNotValid(model, "userId", vertx, testContext);

  }

  /**
   * Check that the {@link #createModelExample(int, Vertx, VertxTestContext, Handler)} is valid.
   *
   * @param index       to verify
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#validate(String, Vertx)
   */
  @ParameterizedTest(name = "The model example {0} has to be valid")
  @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
  public void shouldExampleBeValid(final int index, final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(index, vertx, testContext, testContext.succeeding(model -> {

      assertIsValid(model, vertx, testContext);

    }));

  }

  /**
   * Check that a {@code null} user identifier is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithANullUserId(final Vertx vertx, final VertxTestContext testContext) {

    final CommunityMember model = this.createModelExample(1);
    model.userId = null;
    assertIsNotValid(model, "userId", vertx, testContext);
  }

  /**
   * Check that an undefined user identifier is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithAnUdefinedUserId(final Vertx vertx, final VertxTestContext testContext) {

    final CommunityMember model = this.createModelExample(1);
    model.userId = UUID.randomUUID().toString();
    assertIsNotValid(model, "userId", vertx, testContext);

  }

  /**
   * Check that an undefined user identifier is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithABadPrivilege(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.privileges.add(ValidationsTest.STRING_256);
      assertIsNotValid(model, "privileges[1]", vertx, testContext);

    }));

  }

  /**
   * Should merge with {@code null}
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudMergeWithNull(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {

      assertCanMerge(target, null, vertx, testContext, merged -> {
        assertThat(merged).isSameAs(target);
      });
    }));

  }

  /**
   * Should merge two examples.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#merge(CommunityMember, String, Vertx)
   */
  @Test
  public void shoudMergeExamples(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      target._creationTs = 10000;
      target._lastUpdateTs = TimeManager.now();
      this.createModelExample(2, vertx, testContext, testContext.succeeding(source -> {

        assertCanMerge(target, source, vertx, testContext, merged -> {
          assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
          source._creationTs = target._creationTs;
          source._lastUpdateTs = target._lastUpdateTs;
          assertThat(merged).isEqualTo(source);
        });
      }));
    }));

  }

  /**
   * Should not merge with a bad user identifier.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#merge(CommunityMember, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadUserId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {

      final CommunityMember source = new CommunityMember();
      source.userId = "Undefined user identifier";
      assertCannotMerge(target, source, "userId", vertx, testContext);
    }));

  }

  /**
   * Should not merge with a bad privilege.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityMember#merge(CommunityMember, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadPrivilege(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {

      final CommunityMember source = new CommunityMember();
      source.privileges = new ArrayList<>(target.privileges);
      source.privileges.add(ValidationsTest.STRING_256);
      assertCannotMerge(target, source, "privileges[1]", vertx, testContext);
    }));

  }

}