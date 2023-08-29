package dev.slne.surf.shop.server.api.buffer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

public class ItemBuffer {

    /**
     * A private constructor to prevent instantiation
     */
    private ItemBuffer() {
    }

    /**
     * Uses {@link #writeConfigurationSerializable(AttributeBuffer,
     * ConfigurationSerializable)} to generate a {@link Base64} char sequence
     *
     * @param configurationSerializable the {@link ConfigurationSerializable}
     * @return
     */
    public static String toString(ConfigurationSerializable configurationSerializable) {
        AttributeBuffer buffer = new AttributeBuffer();
        writeConfigurationSerializable(buffer, configurationSerializable);

        return Base64.getEncoder().encodeToString(buffer.asArray());
    }

    /**
     * Uses {@link #readConfigurationSerializable(AttributeBuffer)} to generate
     * a {@link ConfigurationSerializable} from a {@link Base64} char sequence
     *
     * @param string the base64 char sequence
     * @return the {@link ConfigurationSerializable}
     */
    public static <T extends ConfigurationSerializable> T fromString(String string) {
        return readConfigurationSerializable(new AttributeBuffer(Base64.getDecoder().decode(string)));
    }

    /**
     * Writes the {@link ConfigurationSerializable} to the {@link
     * AttributeBuffer}
     *
     * @param buffer                    the buffer
     * @param configurationSerializable the {@link ConfigurationSerializable}
     */
    public static void writeConfigurationSerializable(AttributeBuffer buffer,
            ConfigurationSerializable configurationSerializable) {
        buffer.writeBoolean(configurationSerializable != null);
        if (configurationSerializable == null) {
            return;
        }

        Map<String, Object> itemMap = configurationSerializable.serialize();
        Set<Entry<String, Object>> entries = itemMap.entrySet();

        buffer.writeString(ConfigurationSerialization.getAlias(configurationSerializable.getClass()));
        buffer.writeInt(entries.size());

        for (Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            Object object = entry.getValue();
            buffer.writeString(key);

            if (object instanceof ConfigurationSerializable) {
                buffer.writeByte(3);
                writeConfigurationSerializable(buffer, (ConfigurationSerializable) object);
            } else if (object instanceof Serializable) {
                // using this at last
                buffer.writeByte(4);
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new ByteBufOutputStream(buffer))) {
                    objectOutputStream.writeObject(object);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } else {
                buffer.writeByte(0);
            }
        }
    }

    /**
     * Reads the {@link ConfigurationSerializable} from the {@link
     * AttributeBuffer}
     *
     * @param buf the buffer
     * @return the {@link ConfigurationSerializable}
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConfigurationSerializable> T readConfigurationSerializable(AttributeBuffer buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        String className = buf.readStringFromBuffer(1024);
        int size = buf.readInt();

        HashMap<String, Object> map = new LinkedHashMap<>();

        for (int i = 0; i < size; i++) {
            String key = buf.readStringFromBuffer(1024);
            Object value = null;

            byte type = buf.readByte();
            if (type == 1) {
                // this step is no longer used
                // only for other deserialization

                // cast to int because lagacy items
                // only uses ints?
                value = (int) buf.readLong();
            } else if (type == 2) {
                // this step is no longer used
                // only for other deserialization
                value = buf.readStringOrNull(1024);
            } else if (type == 3) {
                value = readConfigurationSerializable(buf);
            } else if (type == 4) {
                // read with object input streams

                try (ByteBufInputStream byteBufInputStream = new ByteBufInputStream(buf);
                        ObjectInputStream objectInput = new ObjectInputStream(byteBufInputStream)) {
                    value = objectInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            map.put(key, value);
        }

        return (T) ConfigurationSerialization.deserializeObject(map,
                ConfigurationSerialization.getClassByAlias(className));
    }

}
