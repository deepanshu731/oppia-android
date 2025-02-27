package org.oppia.android.domain.locale

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.app.model.SupportedRegions
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LanguageConfigRetriever]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class LanguageConfigRetrieverTest {
  @Inject
  lateinit var languageConfigRetriever: LanguageConfigRetriever

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testOppiaLanguage_hasSupportForSixLanguages() {
    // While it's a bit strange to test a proto, and particularly in this file, this suite is
    // generally responsible for verifying language & region configuration sanity. Part of that
    // requires verifying that all languages are tested below. Note that '8' is because the base
    // 6 languages are supported + LANGUAGE_UNSPECIFIED and UNRECOGNIZED (auto-generated by
    // Protobuf). Finally, note that the values themselves are not checked since it doesn't provide
    // any benefit (being able to reference an enum constant without a compiler error is sufficient
    // proof that constant is available).
    assertThat(OppiaLanguage.values()).hasLength(8)
  }

  @Test
  fun testOppiaRegion_hasSupportForSixLanguages() {
    // See above test for context on why this test is here & for why the number is correct.
    assertThat(OppiaRegion.values()).hasLength(5)
  }

  @Test
  fun testLoadSupportedLanguages_loadsNonDefaultProtoFromAssets() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    assertThat(supportedLanguages).isNotEqualToDefaultInstance()
  }

  @Test
  fun testLoadSupportedLanguages_hasSixSupportedLanguages() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    // Change detector test to ensure changes to the configuration are reflected in tests since
    // changes to the configuration can have a major impact on the app (and may require additional
    // work to be done to support the changes).
    assertThat(supportedLanguages.languageDefinitionsCount).isEqualTo(6)
  }

  @Test
  fun testLoadSupportedLanguages_arabic_isSupportedForAppContentAudioTranslations() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.ARABIC)
    assertThat(definition.hasAppStringId()).isTrue()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isTrue()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.LANGUAGE_UNSPECIFIED)
    assertThat(definition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("ar")
    assertThat(definition.appStringId.androidResourcesLanguageId.languageCode).isEqualTo("ar")
    assertThat(definition.appStringId.androidResourcesLanguageId.regionCode).isEmpty()
    assertThat(definition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("ar")
    assertThat(definition.audioTranslationId.ietfBcp47Id.ietfLanguageTag).isEqualTo("ar")
  }

  @Test
  fun testLoadSupportedLanguages_english_isSupportedForAppContentAudioTranslations() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.ENGLISH)
    assertThat(definition.hasAppStringId()).isTrue()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isTrue()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.LANGUAGE_UNSPECIFIED)
    assertThat(definition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(definition.appStringId.androidResourcesLanguageId.languageCode).isEqualTo("en")
    assertThat(definition.appStringId.androidResourcesLanguageId.regionCode).isEmpty()
    assertThat(definition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(definition.audioTranslationId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
  }

  @Test
  fun testLoadSupportedLanguages_hindi_isSupportedForAppContentAudioTranslations() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.HINDI)
    assertThat(definition.hasAppStringId()).isTrue()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isTrue()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.LANGUAGE_UNSPECIFIED)
    assertThat(definition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
    assertThat(definition.appStringId.androidResourcesLanguageId.languageCode).isEqualTo("hi")
    assertThat(definition.appStringId.androidResourcesLanguageId.regionCode).isEmpty()
    assertThat(definition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
    assertThat(definition.audioTranslationId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
  }

  @Test
  fun testLoadSupportedLanguages_hinglish_isSupportedForContentAndAudioTranslationsWithFallback() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.HINGLISH)
    assertThat(definition.hasAppStringId()).isFalse()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isTrue()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.ENGLISH)
    assertThat(definition.contentStringId.macaronicId.combinedLanguageCode).isEqualTo("hi-en")
    assertThat(definition.audioTranslationId.macaronicId.combinedLanguageCode).isEqualTo("hi-en")
  }

  @Test
  fun testLoadSupportedLanguages_portuguese_hasOnlyContentStringSupport() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.PORTUGUESE)
    assertThat(definition.hasAppStringId()).isFalse()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isFalse()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.LANGUAGE_UNSPECIFIED)
  }

  @Test
  fun testLoadSupportedLangs_brazilianPortuguese_supportsAppContentAudioTranslationsWithFallback() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.BRAZILIAN_PORTUGUESE)
    assertThat(definition.hasAppStringId()).isTrue()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isTrue()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.PORTUGUESE)
    assertThat(definition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
    assertThat(definition.appStringId.androidResourcesLanguageId.languageCode).isEqualTo("pt")
    assertThat(definition.appStringId.androidResourcesLanguageId.regionCode).isEqualTo("BR")
    assertThat(definition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
    assertThat(definition.audioTranslationId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
  }

  @Test
  fun testLoadSupportedRegions_loadsNonDefaultProtoFromAssets() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    assertThat(supportedRegions).isNotEqualToDefaultInstance()
  }

  @Test
  fun testLoadSupportedRegions_hasThreeSupportedRegions() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    // Change detector test to ensure changes to the configuration are reflected in tests since
    // changes to the configuration can have a major impact on the app (and may require additional
    // work to be done to support the changes).
    assertThat(supportedRegions.regionDefinitionsCount).isEqualTo(3)
  }

  @Test
  fun testLoadSupportedRegions_brazil_hasCorrectRegionIdAndSupportedLanguages() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    val definition = supportedRegions.lookUpRegion(OppiaRegion.BRAZIL)
    assertThat(definition.regionId.ietfRegionTag).isEqualTo("BR")
    assertThat(definition.languagesList)
      .containsExactly(OppiaLanguage.PORTUGUESE, OppiaLanguage.BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testLoadSupportedRegions_india_hasCorrectRegionIdAndSupportedLanguages() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    val definition = supportedRegions.lookUpRegion(OppiaRegion.INDIA)
    assertThat(definition.regionId.ietfRegionTag).isEqualTo("IN")
    assertThat(definition.languagesList)
      .containsExactly(OppiaLanguage.HINDI, OppiaLanguage.HINGLISH)
  }

  @Test
  fun testLoadSupportedRegions_unitedStates_hasCorrectRegionIdAndSupportedLanguages() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    val definition = supportedRegions.lookUpRegion(OppiaRegion.UNITED_STATES)
    assertThat(definition.regionId.ietfRegionTag).isEqualTo("US")
    assertThat(definition.languagesList).containsExactly(OppiaLanguage.ENGLISH)
  }

  private fun SupportedLanguages.lookUpLanguage(
    language: OppiaLanguage
  ): LanguageSupportDefinition {
    val definition = languageDefinitionsList.find { it.language == language }
    // Sanity check.
    assertThat(definition).isNotNull()
    return definition!!
  }

  private fun SupportedRegions.lookUpRegion(region: OppiaRegion): RegionSupportDefinition {
    val definition = regionDefinitionsList.find { it.region == region }
    // Sanity check.
    assertThat(definition).isNotNull()
    return definition!!
  }

  private fun setUpTestApplicationComponent() {
    DaggerLanguageConfigRetrieverTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LoggerModule::class, TestDispatcherModule::class, RobolectricModule::class,
      AssetModule::class, LocaleProdModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(languageConfigRetrieverTest: LanguageConfigRetrieverTest)
  }
}
