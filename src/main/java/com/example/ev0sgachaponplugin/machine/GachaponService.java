package com.example.ev0sgachaponplugin.machine;

import com.example.ev0sgachaponplugin.compat.ComponentCompat;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class GachaponService {

    public record MachineContext(String machineName, List<GachaponDefinitions.PoolDefinition> pools) {
    }

    public record SpinResult(boolean success, String message, GachaponDefinitions.PrizeDefinition prize) {
    }

    public record SpinPreparation(boolean success,
                                  String message,
                                  GachaponDefinitions.PoolDefinition pool,
                                  GachaponDefinitions.PrizeDefinition finalPrize,
                                  List<GachaponDefinitions.PrizeDefinition> animationSequence) {
    }

    private GachaponService() {
    }

    public static MachineContext resolveMachineContext(World world, Vector3i blockPos) {
        GachaponMachine machine = lookupMachine(world, blockPos);
        String machineName = machine != null ? machine.getMachineNameOrDefault() : "Plush Gachapon";
        String[] enabledPools = machine != null ? machine.getEnabledPoolsOrDefault() : new String[0];
        return new MachineContext(machineName, GachaponDefinitions.getPools(enabledPools));
    }

    public static SpinResult spin(Store<EntityStore> store, Ref<EntityStore> playerEntity, Vector3i blockPos, String poolId) {
        SpinPreparation preparation = prepareSpin(store, playerEntity, blockPos, poolId);
        if (!preparation.success()) {
            return new SpinResult(false, preparation.message(), null);
        }

        Player player = store.getComponent(playerEntity, Player.getComponentType());
        if (player == null) {
            return new SpinResult(false, "Player inventory was unavailable.", null);
        }

        grantPrize(player, preparation.finalPrize());
        return new SpinResult(true, "Won " + readableItemName(preparation.finalPrize().itemId()) + " from " + preparation.pool().label() + ".", preparation.finalPrize());
    }

    public static SpinPreparation prepareSpin(Store<EntityStore> store, Ref<EntityStore> playerEntity, Vector3i blockPos, String poolId) {
        World world = store.getExternalData().getWorld();
        if (world == null) {
            return new SpinPreparation(false, "World lookup failed.", null, null, List.of());
        }

        MachineContext machineContext = resolveMachineContext(world, blockPos);
        GachaponDefinitions.PoolDefinition pool = null;
        for (GachaponDefinitions.PoolDefinition candidate : machineContext.pools()) {
            if (candidate.id().equals(poolId)) {
                pool = candidate;
                break;
            }
        }
        if (pool == null) {
            return new SpinPreparation(false, "That pool is not enabled on this machine.", null, null, List.of());
        }

        Player player = store.getComponent(playerEntity, Player.getComponentType());
        if (player == null) {
            return new SpinPreparation(false, "Player inventory was unavailable.", null, null, List.of());
        }

        if (!InventoryUtils.hasAccessibleInventory(player)) {
            return new SpinPreparation(false, "Player inventory was unavailable.", null, null, List.of());
        }

        int balance = InventoryUtils.countItem(player, pool.currencyItemId());
        if (balance < pool.currencyCost()) {
            return new SpinPreparation(false, "Need " + pool.currencyCost() + "x " + readableItemName(pool.currencyItemId()) + " to spin this pool.", null, null, List.of());
        }

        if (!InventoryUtils.removeItem(player, pool.currencyItemId(), pool.currencyCost())) {
            return new SpinPreparation(false, "Currency removal failed.", null, null, List.of());
        }

        GachaponDefinitions.PrizeDefinition prize = rollPrize(pool.prizes());
        return new SpinPreparation(true, "", pool, prize, buildAnimationSequence(pool.prizes(), prize));
    }

    public static void grantPrize(Player player, GachaponDefinitions.PrizeDefinition prize) {
        if (player == null || prize == null) {
            return;
        }
        InventoryUtils.giveItem(player, prize.itemId(), 1);
    }

    public static int getCurrencyBalance(Store<EntityStore> store, Ref<EntityStore> playerEntity, Vector3i blockPos, String poolId) {
        MachineContext machineContext = resolveMachineContext(store.getExternalData().getWorld(), blockPos);
        GachaponDefinitions.PoolDefinition pool = getPool(machineContext.pools(), poolId);
        if (pool == null) {
            return 0;
        }

        Player player = store.getComponent(playerEntity, Player.getComponentType());
        if (player == null || !InventoryUtils.hasAccessibleInventory(player)) {
            return 0;
        }
        return InventoryUtils.countItem(player, pool.currencyItemId());
    }

    private static GachaponDefinitions.PrizeDefinition rollPrize(List<GachaponDefinitions.PrizeDefinition> prizes) {
        int totalWeight = 0;
        for (GachaponDefinitions.PrizeDefinition prize : prizes) {
            totalWeight += Math.max(prize.weight(), 0);
        }

        if (totalWeight <= 0) {
            return prizes.get(0);
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (GachaponDefinitions.PrizeDefinition prize : prizes) {
            cumulativeWeight += Math.max(prize.weight(), 0);
            if (roll < cumulativeWeight) {
                return prize;
            }
        }
        return prizes.get(prizes.size() - 1);
    }

    private static GachaponDefinitions.PoolDefinition getPool(List<GachaponDefinitions.PoolDefinition> pools, String poolId) {
        for (GachaponDefinitions.PoolDefinition pool : pools) {
            if (pool.id().equals(poolId)) {
                return pool;
            }
        }
        return null;
    }

    private static GachaponMachine lookupMachine(World world, Vector3i blockPos) {
        try {
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockPos.x, blockPos.z));
            if (chunk == null) {
                return null;
            }

            Object component = ComponentCompat.getBlockComponent(chunk, blockPos.x, blockPos.y, blockPos.z, GachaponMachine.class);
            if (component instanceof GachaponMachine machine) {
                return machine;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static List<GachaponDefinitions.PrizeDefinition> buildAnimationSequence(List<GachaponDefinitions.PrizeDefinition> prizes,
                                                                                    GachaponDefinitions.PrizeDefinition finalPrize) {
        if (prizes == null || prizes.isEmpty()) {
            return List.of(finalPrize);
        }

        List<GachaponDefinitions.PrizeDefinition> sequence = new ArrayList<>();
        int frameCount = 12;
        for (int index = 0; index < frameCount - 1; index++) {
            sequence.add(prizes.get(ThreadLocalRandom.current().nextInt(prizes.size())));
        }
        sequence.add(finalPrize);
        return List.copyOf(sequence);
    }

    private static String readableItemName(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return "Unknown Item";
        }

        String normalized = itemId.trim();
        if (normalized.startsWith("*")) {
            normalized = normalized.substring(1);
        }

        String[] parts = normalized.split("[_\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.length() == 0 ? itemId : builder.toString();
    }
}