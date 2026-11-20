package net.portalmod.common.commands;

import net.minecraft.network.chat.Component;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class LowercaseEnumArgument<T extends Enum<T>> implements ArgumentType<T> {
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
            (found, constants) -> Component.translatable("commands.forge.arguments.enum.invalid", constants, found));

    private final Class<T> enumClass;
    private final Map<String, T> options;

    public static <R extends Enum<R>> LowercaseEnumArgument<R> enumArgument(Class<R> enumClass) {
        return new LowercaseEnumArgument<>(enumClass);
    }

    private LowercaseEnumArgument(final Class<T> enumClass) {
        this.enumClass = enumClass;
        this.options = new HashMap<>();
        this.options.putAll(Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(option -> option.name().toLowerCase(), option -> option)));
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        String argument = reader.readUnquotedString();

        if(this.options.containsKey(argument)) {
            return this.options.get(argument);
        } else {
            throw INVALID_ENUM.createWithContext(reader, argument, Arrays.toString(this.options.keySet().toArray()));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(this.options.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return this.options.keySet();
    }

    /** Sends the enum class name to clients (1.19+ ArgumentTypeInfo replaces ArgumentSerializer). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class Serializer implements ArgumentTypeInfo<LowercaseEnumArgument<?>, Serializer.Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            buffer.writeUtf(template.enumClass.getName());
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            try {
                return new Template((Class) Class.forName(buffer.readUtf()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unknown enum for lowercase_enum argument", e);
            }
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            json.addProperty("enum", template.enumClass.getName());
        }

        @Override
        public Template unpack(LowercaseEnumArgument<?> argument) {
            return new Template((Class) argument.enumClass);
        }

        public class Template implements ArgumentTypeInfo.Template<LowercaseEnumArgument<?>> {
            final Class<? extends Enum> enumClass;

            Template(Class<? extends Enum> enumClass) {
                this.enumClass = enumClass;
            }

            @Override
            public LowercaseEnumArgument<?> instantiate(CommandBuildContext context) {
                return new LowercaseEnumArgument(enumClass);
            }

            @Override
            public ArgumentTypeInfo<LowercaseEnumArgument<?>, ?> type() {
                return Serializer.this;
            }
        }
    }
}
