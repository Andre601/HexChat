package com.andre601.hexchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class ReflectionHelper{

    private static final String version;
    private static Constructor<?> chatComponentText;

    private static Class<?> packetPlayOutChat;
    private static Field packetPlayOutChatComponent;
    private static Field packetPlayOutChatMessageType;
    private static Field packetPlayOutChatUuid;
    private static Object enumChatMessageTypeMessage;
    private static Object enumChatMessageTypeActionbar;

    private static Field connection;
    private static MethodHandle GET_HANDLE;
    private static MethodHandle SEND_PACKET;
    private static MethodHandle STRING_TO_CHAT;
    private static boolean SETUP;
    private static int MAJOR_VER = -1;

    static {
        String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        version = split[split.length - 1];

        try {
            MAJOR_VER = Integer.parseInt(version.split("_")[1]);

            final Class<?> craftPlayer = getClass("{obc}.entity.CraftPlayer");
            Method getHandle = craftPlayer.getMethod("getHandle");
            connection = getHandle.getReturnType().getField("playerConnection");
            Method sendPacket = connection.getType().getMethod("sendPacket", getClass("{nms}.Packet"));

            chatComponentText = getClass("{nms}.ChatComponentText").getConstructor(String.class);

            Method stringToChat;

            if (MAJOR_VER < 8) {
                stringToChat = getClass("{nms}.ChatSerializer").getMethod("a", String.class);
            } else {
                stringToChat = getClass("{nms}.IChatBaseComponent$ChatSerializer").getMethod("a", String.class);
            }

            GET_HANDLE = MethodHandles.lookup().unreflect(getHandle);
            SEND_PACKET = MethodHandles.lookup().unreflect(sendPacket);
            STRING_TO_CHAT = MethodHandles.lookup().unreflect(stringToChat);

            packetPlayOutChat = getClass("{nms}.PacketPlayOutChat");
            packetPlayOutChatComponent = getField(packetPlayOutChat, "a");
            packetPlayOutChatMessageType = getField(packetPlayOutChat, "b");
            packetPlayOutChatUuid = MAJOR_VER >= 16 ? getField(packetPlayOutChat, "c") : null;

            if (MAJOR_VER >= 12) {
                Method getChatMessageType = getClass("{nms}.ChatMessageType").getMethod("a", byte.class);

                enumChatMessageTypeMessage = getChatMessageType.invoke(null, (byte) 1);
                enumChatMessageTypeActionbar = getChatMessageType.invoke(null, (byte) 2);
            }

            SETUP = true;
        } catch (Exception e) {
            e.printStackTrace();
            SETUP = false;
        }
    }

    public static void sendPacket(Object packet, Player... players) {
        assertIsSetup();

        if (packet == null) {
            return;
        }

        for (Player player : players) {
            try {
                SEND_PACKET.bindTo(connection.get(GET_HANDLE.bindTo(player).invoke())).invoke(packet);
            } catch (Throwable e) {
                System.err.println("Failed to send packet");
                e.printStackTrace();
            }
        }

    }

    public static Object createTextPacket(String message) {
        assertIsSetup();

        try {
            Object packet = packetPlayOutChat.getDeclaredConstructor().newInstance();
            setFieldValue(packetPlayOutChatComponent, packet, fromJson(message));
            setFieldValue(packetPlayOutChatUuid, packet, UUID.randomUUID());
            setType(packet);
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void setType(Object chatPacket) {
        assertIsSetup();

        if (MAJOR_VER < 12) {
            setFieldValue(packetPlayOutChatMessageType, chatPacket, 1);
            return;
        }

        setFieldValue(packetPlayOutChatMessageType, chatPacket, enumChatMessageTypeMessage);
    }

    /**
     * Creates a ChatComponentText from plain text
     *
     * @param message The text to convert to a chat component
     * @return The chat component
     */
    static Object componentText(String message) {
        assertIsSetup();

        try {
            return chatComponentText.newInstance(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Attempts to convert a String representing a JSON message into a usable object
     *
     * @param json The JSON to attempt to parse
     * @return The object representing the text in JSON form, or <code>null</code> if something went wrong converting the String to JSON data
     */
    static Object fromJson(String json) {
        assertIsSetup();

        if (!json.trim().startsWith("{")) {
            return componentText(json);
        }

        try {
            return STRING_TO_CHAT.invoke(json);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void assertIsSetup() {
        if (!SETUP) {
            throw new IllegalStateException("JSONMessage.ReflectionHelper is not set up yet!");
        }
    }

    private static Class<?> getClass(String path) throws ClassNotFoundException {
        return Class.forName(path.replace("{nms}", "net.minecraft.server." + version).replace("{obc}", "org.bukkit.craftbukkit." + version));
    }

    private static void setFieldValue(Field field, Object instance, Object value) {
        if (field == null) {
            // useful for fields that might not exist
            return;
        }

        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field getField(Class<?> classObject, String fieldName) {
        try {
            Field field = classObject.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }
}
