package com.loadbearing.advancement;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public final class StructuralTrigger extends SimpleCriterionTrigger<StructuralTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, LBCriteria.Kind kind) {
        this.trigger(player, instance -> instance.kind().equals(kind.id()));
    }

    public record Instance(Optional<ContextAwarePredicate> player, String kind)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.fieldOf("kind").forGetter(Instance::kind)
        ).apply(i, Instance::new));
    }
}
