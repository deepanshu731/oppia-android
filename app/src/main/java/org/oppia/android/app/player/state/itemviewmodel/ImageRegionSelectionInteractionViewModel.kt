package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.ClickOnImage
import org.oppia.android.app.model.ImageWithRegions
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.DefaultRegionClickedEvent
import org.oppia.android.app.utility.NamedRegionClickedEvent
import org.oppia.android.app.utility.OnClickableAreaClickedListener
import org.oppia.android.app.utility.RegionClickedEvent

/** [StateItemViewModel] for image region selection. */
class ImageRegionSelectionInteractionViewModel(
  val entityId: String,
  val hasConversationView: Boolean,
  interaction: Interaction,
  private val errorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler
) : StateItemViewModel(ViewType.IMAGE_REGION_SELECTION_INTERACTION),
  InteractionAnswerHandler,
  OnClickableAreaClickedListener {
  var answerText: CharSequence = ""
  val selectableRegions: List<ImageWithRegions.LabeledRegion> by lazy {
    val schemaObject = interaction.customizationArgsMap["imageAndRegions"]
    schemaObject?.customSchemaValue?.imageWithRegions?.labelRegionsList ?: listOf()
  }

  val imagePath: String by lazy {
    val schemaObject = interaction.customizationArgsMap["imageAndRegions"]
    schemaObject?.customSchemaValue?.imageWithRegions?.imagePath ?: ""
  }

  val isAnswerAvailable = ObservableField<Boolean>(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          errorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError = null,
            inputAnswerAvailable = answerText.isNotEmpty()
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  override fun onClickableAreaTouched(region: RegionClickedEvent) {
    when (region) {
      is DefaultRegionClickedEvent -> {
        answerText = ""
        isAnswerAvailable.set(false)
      }
      is NamedRegionClickedEvent -> {
        answerText = region.regionLabel
        isAnswerAvailable.set(true)
      }
    }
  }

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    val answerTextString = answerText.toString()
    answer = InteractionObject.newBuilder().apply {
      clickOnImage = parseClickOnImage(answerTextString)
    }.build()
    plainAnswer = resourceHandler.getStringInLocaleWithWrapping(
      R.string.image_interaction_answer_text,
      answerTextString
    )
    this.writtenTranslationContext =
      this@ImageRegionSelectionInteractionViewModel.writtenTranslationContext
  }.build()

  private fun parseClickOnImage(answerTextString: String): ClickOnImage {
    val region = selectableRegions.find { it.label == answerTextString }
    return ClickOnImage.newBuilder()
      // The object supports multiple regions in an answer, but neither web nor Android supports this.
      .addClickedRegions(region?.label ?: "")
      .build()
  }
}
