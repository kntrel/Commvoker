package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.bukkit.provided.annotation.BlockCoordinates;
import com.kntrel.mc.commvoker.bukkit.provided.annotation.Sender;
import com.kntrel.mc.commvoker.bukkit.provided.annotation.SenderWorld;
import com.kntrel.mc.commvoker.bukkit.provided.assemblers.*;
import com.kntrel.util.Constants;
import com.kntrel.util.Priority;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.Axis;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.kntrel.mc.commvoker.argument.binder.ArgumentBinder.*;

public class BukkitArgumentBindings {


    public static final ArgumentBinding<? super CommandSender, ?, ?>
        WORLD = argumentAssembler(WorldAssembler::world)
            .toClass(World.class)
            .withPriority(Priority.LOW)
            .bind(),
        SENDER_WORLD = implicit(ctx -> ((Entity) ctx.source()).getWorld())
            .toClass(World.class)
            .toAnnotation(Sender.class)
            .requires(s -> s instanceof Entity)
            .withPriority(Priority.above(WORLD.priority()))
            .bind(),
        BLOCK_VECTOR = argumentAssembler(BlockVectorAssembler::blockVector)
            .toClass(BlockVector.class)
            .withPriority(Priority.LOW)
            .bind(),
        VECTOR = argumentAssembler(VectorAssembler::vector)
            .toClass(Vector.class)
            .withPriority(Priority.LOW)
            .bind(),
        LOCATION = ArgumentBinder.<CommandSender, Location>argumentAssembler(ctx -> ctx.isAnnotationPresent(SenderWorld.class)
                    ? LocationAssembler.locationInferredWorld()
                    : LocationAssembler.location()
            )
            .toClass(Location.class)
            .withPriority(Priority.LOW)
            .bind(),
        BLOCK_LOCATION = ArgumentBinder.<CommandSender, Location>argumentAssembler(ctx -> ctx.isAnnotationPresent(SenderWorld.class)
                    ? BlockLocationAssembler.locationInferredWorld()
                    : BlockLocationAssembler.location()
            )
            .toClass(Location.class)
            .toAnnotation(BlockCoordinates.class)
            .withPriority(Priority.above(LOCATION.priority()))
            .bind(),
        SENDER_LOCATION = implicit(ctx -> ((Entity) ctx.source()).getLocation())
            .toClass(Location.class)
            .toAnnotation(Sender.class)
            .requires(s -> s instanceof Entity)
            .withPriority(Priority.above(LOCATION.priority()))
            .bind(),
        ENTITY = ArgumentBinder.<CommandSender, Entity>argumentAssembler(ctx -> {
                    Class<?> c = (Class<?>) ctx.type();
                    @SuppressWarnings("unchecked")
                    Class<? extends Entity> ec = (Class<? extends Entity>) c;
                    return (Assembler<CommandSender, Entity>) EntityAssembler.requiredEntity(ec);
                })
                .toClass(Entity.class)
                .withPriority(Priority.LOW)
                .bind(),
        OPTIONAL_ENTITY = ArgumentBinder.<CommandSender, Optional<? extends Entity>>argumentAssembler(ctx -> {
                    ParameterizedType pt = (ParameterizedType) ctx.type();
                    Type arg = pt.getActualTypeArguments()[0];
                    Class<?> ec = (Class<?>) arg;
                    @SuppressWarnings("unchecked")
                    Assembler<CommandSender, Optional<? extends Entity>> asm = (Assembler<CommandSender, Optional<? extends Entity>>) (Assembler<?, ?>) EntityAssembler.entity((Class<? extends Entity>) ec);
                    return asm;
                })
                .toClass((Class<Optional<? extends Entity>>) (Class<?>) Optional.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    Type arg = pt.getActualTypeArguments()[0];
                    return (arg instanceof Class<?> ac) && Entity.class.isAssignableFrom(ac);
                })
                .withPriority(Priority.LOW)
                .bind(),
        ENTITIES = ArgumentBinder.<CommandSender, List<? extends Entity>>argumentAssembler(ctx -> {
                    ParameterizedType pt = (ParameterizedType) ctx.type();
                    Type arg = pt.getActualTypeArguments()[0];
                    Class<?> ec = (Class<?>) arg;
                    @SuppressWarnings("unchecked")
                    Assembler<CommandSender, List<? extends Entity>> asm = (Assembler<CommandSender, List<? extends Entity>>) (Assembler<?, ?>) EntityAssembler.entities((Class<? extends Entity>) ec);
                    return asm;
                })
                .toClass((Class<List<? extends Entity>>) (Class<?>) List.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    Type arg = pt.getActualTypeArguments()[0];
                    return (arg instanceof Class<?> ac) && Entity.class.isAssignableFrom(ac);
                })
                .withPriority(Priority.HIGH)
                .bind(),
        SENDER_ENTITY = implicit(ctx -> (Entity) ctx.source())
                .toClass(Entity.class)
                .toAnnotation(Sender.class)
                .requires(s -> s instanceof Entity)
                .toCondition(ctx -> ctx.type() instanceof Class<?> c && c.equals(Entity.class))
                .withPriority(Priority.above(ENTITY.priority()))
                .bind(),
        PLAYER = argumentAssembler(EntityAssembler::requiredPlayer)
                .toClass(Player.class)
                .withPriority(Priority.above(ENTITY.priority()))
                .bind(),
        PLAYER_PROFILE = argumentAssembler(GameProfileAssembler::requiredProfile)
                .toClass(PlayerProfile.class)
                .withPriority(Priority.LOW)
                .bind(),
        OPTIONAL_PLAYER_PROFILE = argumentAssembler(GameProfileAssembler::profile)
                .toClass((Class<Optional<PlayerProfile>>) (Class<?>) Optional.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    Type arg = pt.getActualTypeArguments()[0];
                    return arg instanceof Class<?> c && c.equals(PlayerProfile.class);
                })
                .withPriority(Priority.LOW)
                .bind(),
        PLAYER_PROFILES = argumentAssembler(GameProfileAssembler::profiles)
                .toClass((Class<List<PlayerProfile>>) (Class<?>) List.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    return pt.getActualTypeArguments()[0].equals(PlayerProfile.class);
                })
                .withPriority(Priority.HIGH)
                .bind(),
        OFFLINE_PLAYER = argumentAssembler(GameProfileAssembler::requiredOfflinePlayer)
                .toClass(OfflinePlayer.class)
                .withPriority(Priority.LOW)
                .bind(),
        OPTIONAL_OFFLINE_PLAYER = argumentAssembler(GameProfileAssembler::offlinePlayer)
                .toClass((Class<Optional<OfflinePlayer>>) (Class<?>) Optional.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    Type arg = pt.getActualTypeArguments()[0];
                    return arg instanceof Class<?> c && c.equals(OfflinePlayer.class);
                })
                .withPriority(Priority.LOW)
                .bind(),
        OFFLINE_PLAYERS = argumentAssembler(GameProfileAssembler::offlinePlayers)
                .toClass((Class<List<OfflinePlayer>>) (Class<?>) List.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    return pt.getActualTypeArguments()[0].equals(OfflinePlayer.class);
                })
                .withPriority(Priority.HIGH)
                .bind(),
        OPTIONAL_PLAYER = argumentAssembler(EntityAssembler::player)
                .toClass((Class<Optional<Player>>) (Class<?>) Optional.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    Type arg = pt.getActualTypeArguments()[0];
                    return arg instanceof Class<?> c && c.equals(Player.class);
                })
                .withPriority(Priority.above(OPTIONAL_ENTITY.priority()))
                .bind(),
        PLAYERS = argumentAssembler(EntityAssembler::players)
                .toClass((Class<List<Player>>) (Class<?>) List.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    return pt.getActualTypeArguments()[0].equals(Player.class);
                })
                .withPriority(Priority.above(ENTITIES.priority()))
                .bind(),
        SENDER_PLAYER = implicit(ctx -> (Player) ctx.source())
                .toClass(Player.class)
                .toAnnotation(Sender.class)
                .requires(s -> s instanceof Player)
                .withPriority(Priority.above(SENDER_ENTITY.priority()))
                .bind(),
        COMMAND_SENDER = ArgumentBinder.<CommandSender, CommandSender>implicit(ExecutionContext::source)
                .toClass(CommandSender.class)
                .bind(),
        COMMAND_CONTEXT = ArgumentBinder.<CommandSender, CommandContext<CommandSender>>implicit(ctx -> (CommandContext<CommandSender>) ctx.commandContext())
                .toClass((Class<CommandContext<CommandSender>>) (Class<?>) CommandContext.class)
                .toCondition(ctx -> ctx.type() instanceof ParameterizedType pt && pt.getActualTypeArguments()[0] instanceof Class<?> c && c.equals(CommandSender.class))
                .bind(),
        BLOCK_DATA = argumentAssembler(BlockDataAssembler::blockData)
                .toClass(BlockData.class)
                .withPriority(Priority.LOW)
                .bind(),
        ITEM_STACK = argumentAssembler(ItemStackAssembler::itemStack)
                .toClass(ItemStack.class)
                .withPriority(Priority.LOW)
                .bind(),
        CHAT_COLOR = argumentAssembler(ChatColorAssembler::chatColor)
                .toClass(ChatColor.class)
                .withPriority(Priority.LOW)
                .bind(),
        BUKKIT_COLOR = argumentAssembler(ColorAssembler::color)
                .toClass(Color.class)
                .withPriority(Priority.LOW)
                .bind(),
        OBJECTIVE = argumentAssembler(ObjectiveAssembler::objective)
                .toClass(Objective.class)
                .withPriority(Priority.LOW)
                .bind(),
        CRITERIA = argumentAssembler(CriteriaAssembler::criteria)
                .toClass(Criteria.class)
                .withPriority(Priority.LOW)
                .bind(),
        DISPLAY_SLOT = argumentAssembler(DisplaySlotAssembler::displaySlot)
                .toClass(DisplaySlot.class)
                .withPriority(Priority.LOW)
                .bind(),
        AXES = argumentAssembler(SwizzleAssembler::swizzle)
                .toClass((Class<EnumSet<Axis>>) (Class<?>) EnumSet.class)
                .toCondition(ctx -> {
                    if (!(ctx.type() instanceof ParameterizedType pt)) return false;
                    return pt.getRawType().equals(EnumSet.class) && pt.getActualTypeArguments()[0].equals(Axis.class);
                })
                .withPriority(Priority.LOW)
                .bind(),
        TEAM = argumentAssembler(TeamAssembler::team)
                .toClass(Team.class)
                .withPriority(Priority.LOW)
                .bind(),
        NAMESPACED_KEY = argumentAssembler(NamespacedKeyAssembler::namespacedKey)
                .toClass(NamespacedKey.class)
                .withPriority(Priority.LOW)
                .bind(),
        UNIQUE_ID = argumentAssembler(UUIDAssembler::uuid)
                .toClass(UUID.class)
                .withPriority(Priority.LOW)
                .bind(),
        GAME_MODE = argumentAssembler(GameModeAssembler::gameMode)
                .toClass(GameMode.class)
                .withPriority(Priority.LOW)
                .bind(),
        HEIGHT_MAP = argumentAssembler(HeightMapAssembler::heightMap)
                .toClass(HeightMap.class)
                .withPriority(Priority.LOW)
                .bind(),
        MIRROR = argumentAssembler(MirrorAssembler::mirror)
                .toClass(Mirror.class)
                .withPriority(Priority.LOW)
                .bind(),
        STRUCTURE_ROTATION = argumentAssembler(StructureRotationAssembler::structureRotation)
                .toClass(StructureRotation.class)
                .withPriority(Priority.LOW)
                .bind(),
        BOUNDING_BOX = argumentAssembler(BoundingBoxAssembler::boundingBox)
                .toClass(BoundingBox.class)
                .withPriority(Priority.LOW)
                .bind();


    @SuppressWarnings("unchecked")
    public static Collection<ArgumentBinding<? super CommandSender, ?, ?>> all() {
        return Constants.getAll(BukkitArgumentBindings.class, (Class<ArgumentBinding<? super CommandSender, ?, ?>>) (Class<?>) ArgumentBinding.class);
    }
}
