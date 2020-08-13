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
import eu.internetofus.common.components.service.App;
import eu.internetofus.common.components.service.WeNetService;
import eu.internetofus.common.components.service.WeNetServiceMocker;
import eu.internetofus.common.components.service.WeNetServiceSimulator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * Test the {@link CommunityProfile}.
 *
 * @see CommunityProfile
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ExtendWith(VertxExtension.class)
public class CommunityProfileTest extends ModelTestCase<CommunityProfile> {

  /**
   * The profile manager mocked server.
   */
  protected static WeNetProfileManagerMocker profileManagerMocker;

  /**
   * The profile manager mocked server.
   */
  protected static WeNetServiceMocker serviceMocker;

  /**
   * Start the mocker server.
   */
  @BeforeAll
  public static void startMocker() {

    profileManagerMocker = WeNetProfileManagerMocker.start();
    serviceMocker = WeNetServiceMocker.start();
  }

  /**
   * Stop the mocker server.
   */
  @AfterAll
  public static void stopMockers() {

    profileManagerMocker.stop();
    serviceMocker.stop();
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
    final JsonObject serviceConf = serviceMocker.getComponentConfiguration();
    WeNetService.register(vertx, client, serviceConf);
    WeNetServiceSimulator.register(vertx, client, serviceConf);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommunityProfile createModelExample(final int index) {

    final CommunityProfile model = new CommunityProfile();
    model.appId = "AppId_" + index;
    model.id = "Id_" + index;
    model.keywords = new ArrayList<>();
    model.keywords.add("keyword" + index);
    model.members = new ArrayList<>();
    model.members.add(new CommunityMemberTest().createModelExample(index));
    model.name = "Name_" + index;
    model.norms = new ArrayList<>();
    model.norms.add(new NormTest().createModelExample(index));
    model.socialPractices = new ArrayList<>();
    model.socialPractices.add(new SocialPracticeTest().createModelExample(index));
    return model;
  }

  /**
   * Check that the {@link #createModelExample(int)} is not valid.
   *
   * @param index       to verify
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @ParameterizedTest(name = "The model example {0} has to be valid")
  @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
  public void shouldBasicExampleNotBeValid(final int index, final Vertx vertx, final VertxTestContext testContext) {

    final CommunityProfile model = this.createModelExample(index);
    assertIsNotValid(model, "appId", vertx, testContext);

  }

  /**
   * Create an example model that has the specified index.
   *
   * @param index         to use in the example.
   * @param vertx         event bus to use.
   * @param testContext   test context to use.
   * @param createHandler the component that will manage the created model.
   */
  public void createModelExample(final int index, final Vertx vertx, final VertxTestContext testContext, final Handler<AsyncResult<CommunityProfile>> createHandler) {

    StoreServices.storeApp(new App(), vertx, testContext, testContext.succeeding(storedApp -> {

      new CommunityMemberTest().createModelExample(index, vertx, testContext, testContext.succeeding(member -> {

        final CommunityProfile model = this.createModelExample(index);
        model.appId = storedApp.appId;
        model.members.clear();
        model.members.add(member);
        createHandler.handle(Future.succeededFuture(model));

      }));
    }));

  }

  /**
   * Check that the {@link #createModelExample(int, Vertx, VertxTestContext, Handler)} is valid.
   *
   * @param index       to verify
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @ParameterizedTest(name = "The model example {0} has to be valid")
  @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
  public void shouldExampleBeValid(final int index, final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(index, vertx, testContext, testContext.succeeding(model -> {

      assertIsValid(model, vertx, testContext);

    }));

  }

  /**
   * Check that it is not valid if the identifier exist.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithADefinedId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      StoreServices.storeCommunityExample(200, vertx, testContext, testContext.succeeding(storedModel -> {

        model.id = storedModel.id;
        assertIsNotValid(model, "id", vertx, testContext);

      }));
    }));
  }

  /**
   * Check that a {@code null} app identifier is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithANullAppId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.appId = null;
      assertIsNotValid(model, "appId", vertx, testContext);

    }));
  }

  /**
   * Check that an undefined app identifier is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithUndefinedAppId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.appId = "Undefined identifier";
      assertIsNotValid(model, "appId", vertx, testContext);

    }));
  }

  /**
   * Check that an invalid name is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithInvalidName(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.name = ValidationsTest.STRING_256;
      assertIsNotValid(model, "name", vertx, testContext);

    }));
  }

  /**
   * Check that an invalid description is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithInvalidDescription(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.description = ValidationsTest.STRING_1024;
      assertIsNotValid(model, "description", vertx, testContext);

    }));
  }

  /**
   * Check that an invalid keyword is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithInvalidKeyword(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.keywords.add(ValidationsTest.STRING_256);
      assertIsNotValid(model, "keywords[1]", vertx, testContext);

    }));
  }

  /**
   * Check that an invalid member is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithInvalidMember(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.members.add(new CommunityMemberTest().createModelExample(2));
      assertIsNotValid(model, "members[1].userId", vertx, testContext);

    }));
  }

  /**
   * Check that an invalid social practice is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithInvalidSocialPractice(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.socialPractices.add(new SocialPracticeTest().createModelExample(2));
      model.socialPractices.get(1).label = ValidationsTest.STRING_256;
      assertIsNotValid(model, "socialPractices[1].label", vertx, testContext);

    }));
  }

  /**
   * Check that an invalid norm is not valid.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#validate(String, Vertx)
   */
  @Test
  public void shouldNotBeValidWithInvalidNorm(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(model -> {

      model.norms.add(new NormTest().createModelExample(2));
      model.norms.get(1).attribute = ValidationsTest.STRING_256;
      assertIsNotValid(model, "norms[1].attribute", vertx, testContext);

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
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudMergeExamples(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      target._creationTs = 10000;
      target._lastUpdateTs = TimeManager.now();
      this.createModelExample(2, vertx, testContext, testContext.succeeding(source -> {

        assertCanMerge(target, source, vertx, testContext, merged -> {
          assertThat(merged).isNotEqualTo(target).isNotEqualTo(source);
          source.id = target.id;
          source._creationTs = target._creationTs;
          source._lastUpdateTs = target._lastUpdateTs;
          assertThat(merged).isEqualTo(source);
        });
      }));
    }));

  }

  /**
   * Should not merge with a bad application identifier.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadAppId(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      final CommunityProfile source = new CommunityProfile();
      source.appId = "Undefined application id";
      assertCannotMerge(target, source, "appId", vertx, testContext);
    }));

  }

  /**
   * Should not merge with a bad name.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadName(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      final CommunityProfile source = new CommunityProfile();
      source.name = ValidationsTest.STRING_256;
      assertCannotMerge(target, source, "name", vertx, testContext);
    }));

  }

  /**
   * Should not merge with a bad description.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadDescription(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      final CommunityProfile source = new CommunityProfile();
      source.description = ValidationsTest.STRING_1024;
      assertCannotMerge(target, source, "description", vertx, testContext);
    }));

  }

  /**
   * Should not merge with a bad keyword.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadKeywords(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      final CommunityProfile source = new CommunityProfile();
      source.keywords = new ArrayList<>(target.keywords);
      source.keywords.add(ValidationsTest.STRING_256);
      assertCannotMerge(target, source, "keywords[1]", vertx, testContext);
    }));

  }

  /**
   * Should not merge with a bad social practice.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadSocialPractices(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      final CommunityProfile source = new CommunityProfile();
      source.socialPractices = new ArrayList<>(target.socialPractices);
      source.socialPractices.add(new SocialPracticeTest().createModelExample(2));
      source.socialPractices.get(1).label = ValidationsTest.STRING_256;
      assertCannotMerge(target, source, "socialPractices[1].label", vertx, testContext);
    }));

  }

  /**
   * Should not merge with a bad norm.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadNorms(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      final CommunityProfile source = new CommunityProfile();
      source.norms = new ArrayList<>(target.norms);
      source.norms.add(new NormTest().createModelExample(2));
      source.norms.get(1).attribute = ValidationsTest.STRING_256;
      assertCannotMerge(target, source, "norms[1].attribute", vertx, testContext);
    }));

  }

  /**
   * Should not merge with a bad member.
   *
   * @param vertx       event bus to use.
   * @param testContext context to test.
   *
   * @see CommunityProfile#merge(CommunityProfile, String, Vertx)
   */
  @Test
  public void shoudNotMergeWithBadMembers(final Vertx vertx, final VertxTestContext testContext) {

    this.createModelExample(1, vertx, testContext, testContext.succeeding(target -> {
      final CommunityProfile source = new CommunityProfile();
      source.members = new ArrayList<>(target.members);
      source.members.add(new CommunityMemberTest().createModelExample(2));
      assertCannotMerge(target, source, "members[1].userId", vertx, testContext);
    }));

  }

}
