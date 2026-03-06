package net.portalmod.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.math.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PortalCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("portal")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("open")
                .then(Commands.argument("uuid", UUIDArgument.uuid()).suggests(PortalCommand::suggestUUIDs)
                .then(Commands.argument("end", LowercaseEnumArgument.enumArgument(PortalEnd.Safe.class))
                .then(Commands.argument("color", LowercaseEnumArgument.enumArgument(PortalColors.class))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                .then(Commands.argument("face", LowercaseEnumArgument.enumArgument(AttachFace.class))
                .then(Commands.argument("direction", LowercaseEnumArgument.enumArgument(Direction.class))
                .executes(command -> openPortal(
                        command.getSource(),
                        UUIDArgument.getUuid(command, "uuid"),
                        command.getArgument("end", PortalEnd.Safe.class).getOriginal(),
                        command.getArgument("color", PortalColors.class),
                        Vec3Argument.getVec3(command, "pos"),
                        command.getArgument("face", AttachFace.class),
                        command.getArgument("direction", Direction.class)
                ))))))))
            )
            .then(Commands.literal("close")
                .then(Commands.argument("uuid", UUIDArgument.uuid()).suggests(PortalCommand::suggestUUIDs)
                .executes(command -> closePortal(
                        command.getSource(),
                        UUIDArgument.getUuid(command, "uuid"),
                        PortalEnd.NONE
                ))
            ))
            .then(Commands.literal("close")
                .then(Commands.argument("uuid", UUIDArgument.uuid()).suggests(PortalCommand::suggestUUIDs)
                .then(Commands.argument("end", LowercaseEnumArgument.enumArgument(PortalEnd.Safe.class))
                .executes(command -> closePortal(
                        command.getSource(),
                        UUIDArgument.getUuid(command, "uuid"),
                        command.getArgument("end", PortalEnd.Safe.class).getOriginal()
                ))))
            )
        );
    }

    private static CompletableFuture<Suggestions> suggestUUIDs(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        List<UUID> uuids = new ArrayList<>();
        uuids.add(new UUID(0, 0));
        uuids.addAll(PortalManager.getInstance().getPortalMap().keySet());
        return ISuggestionProvider.suggest(uuids.stream().map(UUID::toString), builder);
    }

    private static int openPortal(CommandSource source, UUID uuid, PortalEnd end, PortalColors color, Vector3d position, AttachFace face, Direction direction) throws CommandSyntaxException {
        boolean wall = face == AttachFace.WALL;

        PortalEntity result = PortalPlacer.placePortal(
                source.getLevel(),
                end,
                color.name().toLowerCase(),
                uuid,
                new Vec3(position),
                wall ? direction : face == AttachFace.FLOOR ? Direction.UP : Direction.DOWN,
                wall ? Direction.UP : direction,
                false,
                null
        );

        if(result == null) {
            throw new SimpleCommandExceptionType(getText("open.failed")).create();
        }

        source.sendSuccess(getText("open.success"), true);
        return 1;
    }

    private static int closePortal(CommandSource source, UUID uuid, PortalEnd end) throws CommandSyntaxException {
        PortalPair pair = PortalManager.getInstance().getPair(uuid);

        if(pair == null) {
            throw new SimpleCommandExceptionType(getText("close." + (end == PortalEnd.NONE ? "double" : "single") + ".failed.null")).create();
        }

        if(end == PortalEnd.NONE) {
            if(pair.has(PortalEnd.PRIMARY))
                pair.get(PortalEnd.PRIMARY).remove();
            if(pair.has(PortalEnd.SECONDARY))
                pair.get(PortalEnd.SECONDARY).remove();
            source.sendSuccess(getText("close.double.success"), true);
        } else {
            if(pair.has(end)) {
                pair.get(end).remove();
                source.sendSuccess(getText("close.single.success"), true);
            } else {
                throw new SimpleCommandExceptionType(getText("close.single.failed.null")).create();
            }
        }

        return 1;
    }

    private static TranslationTextComponent getText(String key) {
        return new TranslationTextComponent("commands." + PortalMod.MODID + ".portal." + key);
    }
}