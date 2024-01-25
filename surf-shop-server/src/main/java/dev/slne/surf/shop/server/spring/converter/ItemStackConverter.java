package dev.slne.surf.shop.server.spring.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.inventory.ItemStack;

@Converter
public class ItemStackConverter implements AttributeConverter<ItemStack, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        return itemStack.serializeAsBytes();
    }

    @Override
    public ItemStack convertToEntityAttribute(byte[] data) {
        if (data == null) {
            return null;
        }

        return ItemStack.deserializeBytes(data);
    }
}
