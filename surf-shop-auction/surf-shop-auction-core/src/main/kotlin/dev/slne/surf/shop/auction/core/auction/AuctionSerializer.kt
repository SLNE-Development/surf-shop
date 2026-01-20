package dev.slne.surf.shop.auction.core.auction

import dev.slne.surf.shop.auction.api.auction.bid.AuctionBid
import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.OffsetDateTimeSerializer
import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.JavaUUIDSerializer
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

object AuctionSerializer : KSerializer<AuctionImpl> {
    override val descriptor = buildClassSerialDescriptor("Auction") {
        element("uuid", JavaUUIDSerializer.descriptor)
        element("ownerUuid", JavaUUIDSerializer.descriptor)
        element<String>("itemData")
        element<Int>("startingBid")
        element<Boolean>("instantBuyEnabled")
        element<Int>("instantBuyPrice", isOptional = true)
        element("startsAt", OffsetDateTimeSerializer.descriptor)
        element("endsAt", OffsetDateTimeSerializer.descriptor)
        element<String>("serverName")
        element("bids", ListSerializer(String.serializer()).descriptor)
    }

    override fun serialize(
        encoder: Encoder,
        value: AuctionImpl
    ) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(
                descriptor,
                0,
                JavaUUIDSerializer,
                value.uuid
            )
            encodeSerializableElement(
                descriptor,
                1,
                JavaUUIDSerializer,
                value.ownerUuid
            )
            encodeStringElement(
                descriptor,
                2,
                value.itemData
            )
            encodeIntElement(
                descriptor,
                3,
                value.startingBid
            )
            encodeBooleanElement(
                descriptor,
                4,
                value.instantBuyEnabled
            )
            if (value.instantBuyPrice != null) {
                encodeIntElement(
                    descriptor,
                    5,
                    value.instantBuyPrice
                )
            }
            encodeSerializableElement(
                descriptor,
                6,
                OffsetDateTimeSerializer,
                value.startsAt
            )
            encodeSerializableElement(
                descriptor,
                7,
                OffsetDateTimeSerializer,
                value.endsAt
            )
            encodeStringElement(
                descriptor,
                8,
                value.serverName
            )
            encodeSerializableElement(
                descriptor,
                9,
                ListSerializer(AuctionBid.serializer()),
                value.bids
            )
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): AuctionImpl {
        return decoder.decodeStructure(descriptor) {
            var uuid: SerializableUUID? = null
            var ownerUuid: SerializableUUID? = null
            var itemData: String? = null
            var startingBid: Int? = null
            var instantBuyEnabled: Boolean? = null
            var instantBuyPrice: Int? = null
            var startsAt: SerializableOffsetDateTime? = null
            var endsAt: SerializableOffsetDateTime? = null
            var serverName: String? = null
            var bids: List<AuctionBid> = emptyList()

            if (decodeSequentially()) {
                uuid = decodeSerializableElement(descriptor, 0, JavaUUIDSerializer)
                ownerUuid = decodeSerializableElement(descriptor, 1, JavaUUIDSerializer)
                itemData = decodeStringElement(descriptor, 2)
                startingBid = decodeIntElement(descriptor, 3)
                instantBuyEnabled = decodeBooleanElement(descriptor, 4)
                if (true /* instantBuyPrice is present */) {
                    instantBuyPrice = decodeIntElement(descriptor, 5)
                }
                startsAt = decodeSerializableElement(descriptor, 6, OffsetDateTimeSerializer)
                endsAt = decodeSerializableElement(descriptor, 7, OffsetDateTimeSerializer)
                serverName = decodeStringElement(descriptor, 8)
                bids = decodeSerializableElement(
                    descriptor,
                    9,
                    ListSerializer(AuctionBid.serializer())
                )
            } else while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> uuid = decodeSerializableElement(descriptor, 0, JavaUUIDSerializer)
                    1 -> ownerUuid = decodeSerializableElement(descriptor, 1, JavaUUIDSerializer)
                    2 -> itemData = decodeStringElement(descriptor, 2)
                    3 -> startingBid = decodeIntElement(descriptor, 3)
                    4 -> instantBuyEnabled = decodeBooleanElement(descriptor, 4)
                    5 -> instantBuyPrice = decodeIntElement(descriptor, 5)
                    6 -> startsAt =
                        decodeSerializableElement(descriptor, 6, OffsetDateTimeSerializer)

                    7 -> endsAt = decodeSerializableElement(descriptor, 7, OffsetDateTimeSerializer)
                    8 -> serverName = decodeStringElement(descriptor, 8)
                    9 -> bids = decodeSerializableElement(
                        descriptor,
                        9,
                        ListSerializer(AuctionBid.serializer())
                    )

                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            AuctionImpl(
                uuid = uuid ?: error("Missing value for uuid"),
                ownerUuid = ownerUuid ?: error("Missing value for ownerUuid"),
                itemData = itemData ?: error("Missing value for itemData"),
                startingBid = startingBid ?: error("Missing value for startingBid"),
                instantBuyEnabled = instantBuyEnabled
                    ?: error("Missing value for instantBuyEnabled"),
                instantBuyPrice = instantBuyPrice,
                startsAt = startsAt ?: error("Missing value for startsAt"),
                endsAt = endsAt ?: error("Missing value for endsAt"),
                serverName = serverName ?: error("Missing value for serverName"),
                bids = bids
            )
        }
    }
}