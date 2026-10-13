package com.loadbearing.mixin;

import com.loadbearing.solver.StructuralEventHandler;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stands in for the block-place event the other loaders provide. Fires after a successful place,
 * with the same level, position and placing entity the event carried.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)"
            + "Lnet/minecraft/world/InteractionResult;", at = @At("RETURN"))
    private void loadbearing$afterPlace(BlockPlaceContext context,
            CallbackInfoReturnable<InteractionResult> callback) {
        if (callback.getReturnValue() != InteractionResult.SUCCESS) {
            return;
        }
        StructuralEventHandler.onBlockPlaced(context.getLevel(), context.getClickedPos(),
                context.getPlayer());
    }
}
