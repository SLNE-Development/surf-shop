package dev.slne.surf.shop.server.util;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.util.ApiUtils;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.util.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ShopUtils extends ApiUtils {

    public static final PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();
    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexCharacter('#')
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static final GsonComponentSerializer GSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();

    public static final MiniMessage DEFAULT_MINI_MESSAGE = MiniMessage.miniMessage();

    public static final MiniMessage.Builder DEFAULT_MINI_MESSAGE_BUILDER = MiniMessage.builder()
            .tags(TagResolver.standard())
            .strict(false)
            .debug(s -> ComponentLogger.logger("MiniMessage Debug").info(s))
            .preProcessor(s -> DEFAULT_MINI_MESSAGE.serialize(LEGACY_COMPONENT_SERIALIZER.deserialize(s)))
            .postProcessor(Component::compact);

    /**
     * The surrounding block faces
     */
    public static final BlockFace[] SURROUNDING_BLOCK_FACES;

    /**
     * Gets the surrounding block states
     *
     * @param block           the block
     * @param blockStateClass the block state class
     * @param <T>             the block state type
     * @return the surrounding block states
     */
    public static @NotNull <T extends BlockState> List<T> getSurroundingBlockStates(Block block, Class<T> blockStateClass) {
        final List<T> surroundingBlockStates = new ArrayList<>();

        for (BlockFace surroundingBlockFace : SURROUNDING_BLOCK_FACES) {
            final Block relative = block.getRelative(surroundingBlockFace);

            if (blockStateClass.isInstance(relative.getState())) {
                surroundingBlockStates.add((T) relative.getState());
            }
        }

        return surroundingBlockStates;
    }

    public static @NotNull ItemStack constructCreationItem() {
        final ItemStack creationItem = ItemUtils.item(
                Material.CHEST,
                1,
                3,
                Component.text("Shop", MessageManager.PRIMARY)
        );

        creationItem.editMeta(meta -> meta.getPersistentDataContainer().set(Shop.CREATION_ITEM_KEY, PersistentDataType.BYTE, (byte) 1));

        return creationItem;
    }

    public static boolean hasComponentText(Component component) {
        return StringUtils.hasText(PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(component));
    }


    static {
        // Initialize the surrounding block faces
        SURROUNDING_BLOCK_FACES = new BlockFace[]{
                BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST,
                BlockFace.UP,
                BlockFace.DOWN
        };
    }
}
