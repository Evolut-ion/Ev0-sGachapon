package com.example.ev0sgachaponplugin.machine;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nullable;

public class GachaponMachine implements Component<ChunkStore> {

    public String machineName;
    public String[] enabledPools;
    public Data data;

    public static final BuilderCodec<GachaponMachine> CODEC = buildCodec();

    public GachaponMachine() {
    }

    public GachaponMachine(GachaponMachine other) {
        this.machineName = other.machineName;
        this.enabledPools = other.enabledPools != null ? other.enabledPools.clone() : null;
        this.data = other.data;
    }

    private static BuilderCodec<GachaponMachine> buildCodec() {
        try {
            return BuilderCodec.builder(GachaponMachine.class, GachaponMachine::new)
                    .append(new KeyedCodec<>("MachineName", Codec.STRING, true), (machine, value) -> machine.machineName = value, machine -> machine.machineName).add()
                    .append(new KeyedCodec<>("EnabledPools", Codec.STRING_ARRAY, true), (machine, value) -> machine.enabledPools = value, machine -> machine.enabledPools).add()
                    .build();
        } catch (Throwable ignored) {
            return null;
        }
    }

    public String getMachineNameOrDefault() {
        if (machineName == null || machineName.isBlank()) {
            return "Plush Gachapon";
        }
        return machineName;
    }

    public String[] getEnabledPoolsOrDefault() {
        if (enabledPools == null) {
            return new String[0];
        }
        return enabledPools;
    }

    @Nullable
    @Override
    public Component<ChunkStore> clone() {
        return new GachaponMachine(this);
    }

    public static class Data extends StateData {
        public String machineName;
        public String[] enabledPools;
    }
}