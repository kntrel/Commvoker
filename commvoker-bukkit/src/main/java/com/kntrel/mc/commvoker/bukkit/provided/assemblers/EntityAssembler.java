package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.binding.Components;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblerHook;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EntityAssembler<E extends Entity> implements ComposedAssembler<CommandSender, List<E>> {

    //FACTORY
    public static <E extends Entity> EntityAssembler<E> entities(Class<E> entityType) {
        return new EntityAssembler<>(entityType, Type.PLURAL);
    }
    public static <E extends Entity> Assembler<CommandSender, Optional<E>> entity(Class<E> entityType) {
        return new OptionalAssembler<>(new EntityAssembler<>(entityType, Type.SINGLE));
    }
    public static <E extends Entity> Assembler<CommandSender, E> requiredEntity(Class<E> entityType) {
        return new RequiredAssembler<>(entity(entityType));
    }
    public static EntityAssembler<Player> players() {
        return entities(Player.class);
    }
    public static Assembler<CommandSender, Optional<Player>> player() {
        return entity(Player.class);
    }
    public static Assembler<CommandSender, Player> requiredPlayer() {
        return requiredEntity(Player.class);
    }
    public static EntityAssembler<Entity> entities() {
        return entities(Entity.class);
    }
    public static Assembler<CommandSender, Optional<Entity>> entity() {
        return entity(Entity.class);
    }
    public static Assembler<CommandSender, Entity> requiredEntity() {
        return requiredEntity(Entity.class);
    }


    //INTERNAL
    private enum Type { SINGLE, PLURAL };
    private static class OptionalAssembler<S, T, C extends Collection<T>> implements TransformAssembler<S, C, Optional<T>> {

        //FIELDS
        private final Assembler<? super S, ? extends C> delegate_;

        //CONSTRUCTORS
        private OptionalAssembler(Assembler<? super S, ? extends C> delegate) {
            this.delegate_ = delegate;
        }

        //IMPLEMENTATION
        @Override
        public Assembler<? super S, ? extends C> delegate() {
            return this.delegate_;
        }
        @Override
        public Optional<T> compose(CommandContext<? extends S> ctx, C collection) {
            if (collection.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(collection.iterator().next());
        }
    }
    private static class RequiredAssembler<S, T> implements TransformAssembler<S, Optional<T>, T> {

        //FIELDS
        private final Assembler<? super S, ? extends Optional<T>> delegate_;

        //CONSTRUCTORS
        private RequiredAssembler(Assembler<? super S, ? extends Optional<T>> delegate) {
            this.delegate_ = delegate;
        }

        //IMPLEMENTATION
        @Override
        public Assembler<? super S, ? extends Optional<T>> delegate() {
            return this.delegate_;
        }
        @Override
        public T compose(CommandContext<? extends S> ctx, Optional<T> object) {
            return object.orElseThrow();
        }
    }


    //FIELDS
    private final Class<E> entityType_;
    private final Type type_;


    //CONSTRUCTORS
    private EntityAssembler(Class<E> entityType, Type type) {
        this.entityType_ = entityType;
        this.type_ = type;
    }


    //IMPLEMENTATION
    @Override
    public void composedOf(AssemblerHook<CommandSender> hooK) {
        EntityArgument entityArgument;
        if (this.entityType_.equals(Player.class)) {
            entityArgument = this.type_.equals(Type.SINGLE) ? EntityArgument.player() : EntityArgument.players();
        } else {
            entityArgument = this.type_.equals(Type.SINGLE) ? EntityArgument.entity() : EntityArgument.entities();
        }
        hooK.hook("entityArg", Assembler.ofArgumentType(entityArgument));
    }

    @Override @SuppressWarnings("unchecked")
    public List<E> contextualize(CommandContext<? extends CommandSender> ctx, Components components) {
        CommandSender sender = ctx.getSource();

        EntitySelector selector = components.get("entityArg", EntitySelector.class);
        CommandSourceStack css = (CommandSourceStack) CraftBukkitAccessors.getCommandSourceStack(sender);
        List<? extends net.minecraft.world.entity.Entity> serverEntities;
        try {
            if (this.entityType_.equals(Player.class)) {
                if (this.type_.equals(Type.PLURAL)) {
                    serverEntities = selector.findPlayers(css);
                } else {
                    serverEntities = List.of(selector.findSinglePlayer(css));
                }
            } else {
                if (this.type_.equals(Type.PLURAL)) {
                    serverEntities = selector.findEntities(css);
                } else {
                    serverEntities = List.of(selector.findSingleEntity(css));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return serverEntities.stream()
                .map(e -> e.getBukkitEntity())
                .filter(e -> this.entityType_.isAssignableFrom(e.getClass()))
                .map(e -> (E) e)
                .toList();
    }
}
