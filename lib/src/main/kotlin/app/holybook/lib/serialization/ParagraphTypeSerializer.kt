package app.holybook.lib.serialization

import app.holybook.lib.models.ParagraphType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ParagraphTypeSerializer : KSerializer<ParagraphType> {

  override val descriptor = PrimitiveSerialDescriptor("ParagraphType", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): ParagraphType {
    val value = decoder.decodeString()
    return ParagraphType.fromValue(value)
      ?: throw IllegalArgumentException("Unknown paragraph type: $value")
  }

  override fun serialize(encoder: Encoder, value: ParagraphType) {
    encoder.encodeString(value.value)
  }
}
