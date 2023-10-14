package dev.macrohq.macroframework.command.arguments;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class ArgumentParser<T> {
    private final TypeToken<T> type = new TypeToken<T>(getClass()) {
    };
    public final Class<?> typeClass = type.getRawType();

    @Nullable
    public abstract T parse(@NotNull String arg) throws Exception;

    @NotNull
    public List<String> complete(String current, Parameter parameter) {
        return Collections.emptyList();
    }

    public static class DoubleParser extends ArgumentParser<Double> {
        @Nullable
        @Override
        public Double parse(@NotNull String arg) {
            return Double.parseDouble(arg);
        }
    }

    public static class IntegerParser extends ArgumentParser<Integer> {
        @Nullable
        @Override
        public Integer parse(@NotNull String arg) {
            return Integer.parseInt(arg);
        }
    }

    public static class FloatParser extends ArgumentParser<Float> {
        @Nullable
        @Override
        public Float parse(@NotNull String arg) {
            return Float.parseFloat(arg);
        }
    }

    public static class StringParser extends ArgumentParser<String> {
        @Nullable
        @Override
        public String parse(@NotNull String arg) {
            return arg;
        }
    }

    public static class BooleanParser extends ArgumentParser<Boolean> {
        private static final Map<String, List<String>> VALUES =
                Maps.newHashMap();

        static {
            VALUES.put("true", Lists.newArrayList("on", "yes", "y", "enabled", "enable", "1"));
            VALUES.put("false", Lists.newArrayList("off", "no", "n", "disabled", "disable", "0"));
        }

        @Override
        public @Nullable Boolean parse(@NotNull String s) {
            return Boolean.parseBoolean(
                    VALUES.entrySet().stream()
                            .filter(it -> it.getKey().equalsIgnoreCase(s)
                                    || it.getValue().contains(s.toLowerCase()))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    s + " is not any of: "
                                            + String.join(", ", VALUES.keySet())
                            ))
            );
        }

        @NotNull
        @Override
        public List<String> complete(String current, Parameter parameter) {
            if (current != null && !current.trim().isEmpty()) {
                for (String v : VALUES.keySet()) {
                    if (v.startsWith(current.toLowerCase(Locale.ENGLISH))) {
                        return Lists.newArrayList(v);
                    }
                }
            }
            return new ArrayList<>(VALUES.keySet());
        }
    }
}
