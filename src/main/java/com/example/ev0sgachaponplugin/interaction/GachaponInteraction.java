package com.example.ev0sgachaponplugin.interaction;

import com.example.ev0sgachaponplugin.ui.GachaponUIPage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class GachaponInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<GachaponInteraction> BUILDER_CODEC = BuilderCodec.builder(GachaponInteraction.class, GachaponInteraction::new).build();

    @Override
    protected void interactWithBlock(@NonNullDecl World world,
                                     @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                                     @NonNullDecl InteractionType interactionType,
                                     @NonNullDecl InteractionContext interactionContext,
                                     @NullableDecl ItemStack itemStack,
                                     @NonNullDecl Vector3i vector3i,
                                     @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> playerEntity = interactionContext.getOwningEntity();

        Store<EntityStore> store = playerEntity.getStore();
        PlayerRef playerRef = store.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        BlockPosition targetBlock = interactionContext.getTargetBlock();
        Vector3i blockPos = targetBlock != null
                ? new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z)
                : vector3i;

        GachaponUIPage.openMain(playerRef, playerEntity, store, blockPos, null);
    }

    @Override
    protected void simulateInteractWithBlock(@NonNullDecl InteractionType interactionType,
                                             @NonNullDecl InteractionContext interactionContext,
                                             @NullableDecl ItemStack itemStack,
                                             @NonNullDecl World world,
                                             @NonNullDecl Vector3i vector3i) {
    }
}