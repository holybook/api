package app.holybook.lib.serialization

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DateSerializer : KSerializer<LocalDate> {

  override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): LocalDate =
    LocalDate.parse(decoder.decodeString(), DateTimeFormatter.ISO_DATE)

  override fun serialize(encoder: Encoder, value: LocalDate) {
    encoder.encodeString(value.format(DateTimeFormatter.ISO_DATE))
  }
}
