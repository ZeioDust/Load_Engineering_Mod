package com.loadbearing.advancement;

import com.google.gson.JsonObject;
import com.loadbearing.LoadBearing;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * This era read criteria from JSON rather than through a codec, so the trigger carries its own id
 * and reader. The advancement file shape is unchanged: a "kind" string beside the player predicate.
 */
public final class StructuralTrigger extends SimpleCriterionTrigger<StructuralTrigger.Instance> {
    static final ResourceLocation ID = LoadBearing.id("structural");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player,
            DeserializationContext context) {
        return new Instance(player, GsonHelper.getAsString(json, "kind"));
    }

    public void trigger(ServerPlayer player, LBCriteria.Kind kind) {
        this.trigger(player, instance -> instance.kind().equals(kind.id()));
    }

    public static final class Instance extends AbstractCriterionTriggerInstance {
        private final String kind;

        public Instance(ContextAwarePredicate player, String kind) {
            super(ID, player);
            this.kind = kind;
        }

        public String kind() {
            return this.kind;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("kind", this.kind);
            return json;
        }
    }
}
