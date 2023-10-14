package dev.macrohq.macroframework.command.arguments;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import dev.macrohq.macroframework.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerArgumentParser extends ArgumentParser<GameProfile> {
    private static final HashMap<String, UUID> uuidCache = new HashMap<>();
    @Nullable
    @Override
    public GameProfile parse(@NotNull String arg) {
        List<GameProfile> matchingPlayers = getMatchingPlayers(arg, false);
        for (GameProfile profile : matchingPlayers) {
            return profile;
        }
        return new GameProfile(getUUID(arg), arg);
    }

    private static UUID getUUID(String name) {
        try {
            if (uuidCache.containsKey(name)) {
                return uuidCache.get(name);
            }
            JsonObject json = NetworkUtil.getJsonElement("https://api.mojang.com/users/profiles/minecraft/" + name).getAsJsonObject();
            if (json.has("error")) {
                return null;
            }
            UUID uuid = UUID.fromString(json.get("id").getAsString().replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"
            ));
            uuidCache.put(name, uuid);
            return uuid;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<GameProfile> getMatchingPlayers(String arg, boolean startWith) {
        if (Minecraft.getMinecraft().theWorld == null) return Lists.newArrayList();
        return Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream().map(NetworkPlayerInfo::getGameProfile).filter(gameProfile -> {
            String name = gameProfile.getName().toLowerCase();
            if (name.startsWith("!")) {
                return false;
            } else {
                return startWith ? name.startsWith(arg.toLowerCase()) : name.equals(arg.toLowerCase());
            }
        }).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public List<String> complete(String current, Parameter parameter) {
        return getMatchingPlayers(current, true).stream().map(GameProfile::getName).collect(Collectors.toList());
    }
}
