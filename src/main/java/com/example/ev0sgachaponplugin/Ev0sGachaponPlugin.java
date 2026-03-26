package com.example.ev0sgachaponplugin;

import com.example.ev0sgachaponplugin.compat.ComponentCompat;
import com.example.ev0sgachaponplugin.interaction.GachaponInteraction;
import com.example.ev0sgachaponplugin.machine.GachaponDefinitions;
import com.example.ev0sgachaponplugin.machine.GachaponMachine;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.util.logging.Level;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class Ev0sGachaponPlugin extends JavaPlugin {

    private static Ev0sGachaponPlugin instance;
    private ComponentType<ChunkStore, GachaponMachine> gachaponMachineComponentType;

    public Ev0sGachaponPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        getLogger().at(Level.INFO).log("[Ev0'sGachaponPlugin] Plugin loaded!");
    }

    public static Ev0sGachaponPlugin getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("[Ev0'sGachaponPlugin] Plugin setup!");

        GachaponDefinitions.loadFromConfigFolder(getDataDirectory());
        registerEvents();
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("[Ev0'sGachaponPlugin] Plugin enabled!");
    }

    @Override
    public void shutdown() {
        getLogger().at(Level.INFO).log("[Ev0'sGachaponPlugin] Plugin disabled!");
    }

    private void registerEvents() {
        try {
            try {
                this.gachaponMachineComponentType = this.getChunkStoreRegistry().registerComponent(GachaponMachine.class, idPascal("GachaponMachine"), GachaponMachine.CODEC);
            } catch (Throwable primaryFailure) {
                try {
                    ComponentRegistryProxy chunkStoreRegistry = this.getChunkStoreRegistry();
                    java.lang.reflect.Method method = chunkStoreRegistry.getClass().getMethod("registerComponent", Class.class, java.util.function.Supplier.class);
                    Object result = method.invoke(chunkStoreRegistry, GachaponMachine.class, (java.util.function.Supplier<GachaponMachine>) GachaponMachine::new);
                    if (result instanceof ComponentType<?, ?> componentType) {
                        this.gachaponMachineComponentType = (ComponentType<ChunkStore, GachaponMachine>) componentType;
                    }
                } catch (Throwable fallbackFailure) {
                    ComponentCompat.registerComponent(GachaponMachine.class, "Ev0sGachaponMachine", GachaponMachine.CODEC);
                }
            }
        } catch (Throwable throwable) {
            getLogger().at(Level.WARNING).log("[Ev0'sGachaponPlugin] Failed to register GachaponMachine component: " + throwable.getMessage());
        }

        try {
            this.getCodecRegistry(Interaction.CODEC).register("GachaponInteraction", GachaponInteraction.class, GachaponInteraction.BUILDER_CODEC);
        } catch (Throwable throwable) {
            getLogger().at(Level.WARNING).log("[Ev0'sGachaponPlugin] Failed to register GachaponInteraction codec: " + throwable.getMessage());
        }
    }

    private static String idPascal(String id) {
        return "Ev0sModsEv0sGachapon" + id;
    }
}
