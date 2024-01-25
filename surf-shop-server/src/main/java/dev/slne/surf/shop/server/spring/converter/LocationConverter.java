package dev.slne.surf.shop.server.spring.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.slne.surf.shop.api.ShopApi;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Converter
public class LocationConverter implements AttributeConverter<Location, String> {


    @Override
    public String convertToDatabaseColumn(Location location) {
        final UUID worldUuid = location.getWorld().getUID();
        final ObjectMapper objectMapper = ShopApi.getContext().getBean(ObjectMapper.class);

        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("x", location.getX());
            node.put("y", location.getY());
            node.put("z", location.getZ());
            node.put("yaw", location.getYaw());
            node.put("pitch", location.getPitch());
            node.put("world_uuid_most", worldUuid.getMostSignificantBits());
            node.put("world_uuid_least", worldUuid.getLeastSignificantBits());

            return objectMapper.writeValueAsString(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Location convertToEntityAttribute(String s) {
        final ObjectMapper objectMapper = ShopApi.getContext().getBean(ObjectMapper.class);

        try {
            final JsonNode jsonNode = objectMapper.readTree(s.getBytes(StandardCharsets.UTF_8));
            final double x = jsonNode.get("x").asDouble();
            final double y = jsonNode.get("y").asDouble();
            final double z = jsonNode.get("z").asDouble();
            final float yaw = jsonNode.get("yaw").floatValue();
            final float pitch = jsonNode.get("pitch").floatValue();
            final long worldUuidMost = jsonNode.get("world_uuid_most").asLong();
            final long worldUuidLeast = jsonNode.get("world_uuid_least").asLong();

            return new Location(Bukkit.getWorld(new UUID(worldUuidMost, worldUuidLeast)), x, y, z, yaw, pitch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
