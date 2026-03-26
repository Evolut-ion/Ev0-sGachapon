package com.example.ev0sgachaponplugin.machine;

import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class GachaponDefinitions {

    private static final String[] DEFAULT_PRIZE_IDS = {
            "Deco_Kweebec_Plush",
            "Violet_Plushie_Alesa",
            "Violet_Plushie_Bear",
            "Violet_Plushie_Bee",
            "Violet_Plushie_Brick",
            "Violet_Plushie_BuddhaCat",
            "Violet_Plushie_Bunny",
            "Violet_Plushie_Bunny_Blue",
            "Violet_Plushie_Bunny_Pink",
            "Violet_Plushie_Bunny_Yellow",
            "Violet_Plushie_Cactee",
            "Violet_Plushie_Gaia",
            "Violet_Plushie_Kweebec_Burnt",
            "Violet_Plushie_Kweebec_Gordon",
            "Violet_Plushie_Mosshorn",
            "Violet_Plushie_Mushroom",
            "Violet_Plushie_Mushroom_Blue",
            "Violet_Plushie_Mushroom_Green",
            "Violet_Plushie_Mushroom_Yellow",
            "Violet_Plushie_Octopus",
            "Violet_Plushie_Octopus_Chef",
            "Violet_Plushie_Octopus_Orange",
            "Violet_Plushie_Octopus_Purple",
            "Violet_Plushie_Seal",
            "Violet_Plushie_Seedling",
            "Violet_Plushie_Shark",
            "Violet_Plushie_Simon",
            "Violet_Plushie_Slamma",
            "Violet_Plushie_Snail",
            "Violet_Plushie_Snail_Green",
            "Violet_Plushie_Trork_Blue",
            "Violet_Plushie_Trork_Green",
            "Violet_Plushie_Trork_Red",
            "Violet_Plushie_Varyn",
            "Violet_Plushie_Vinesauce",
                "Violet_Plushie_Violet",
                "Deco_Kirby_Plush",
                "Deco_Korok_Plush",
                "Deco_Link_Plush",
                "Deco_Mario_Plush",
                "Deco_Pikachu_Plush",
                "Deco_Yoshi_Plush"
    };

    public record PrizeDefinition(String itemId, int weight) {
    }

    public record PoolDefinition(String id, String label, String currencyItemId, int currencyCost, List<PrizeDefinition> prizes) {
    }

    private static final Map<String, PoolDefinition> POOLS = new LinkedHashMap<>();
    private static final String DEFAULT_POOL_ID = "test";
    private static final String CONFIG_FILE_NAME = "gachapon-pools.properties";

    private GachaponDefinitions() {
    }

    private static void register(PoolDefinition definition) {
        POOLS.put(definition.id(), definition);
    }

    public static synchronized void loadFromConfigFolder(Path dataDirectory) {
        Path configFolder = dataDirectory.resolve("Config");
        Path configFile = configFolder.resolve(CONFIG_FILE_NAME);

        ensureDefaultConfig(configFolder, configFile);

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            loadDefaultTestConfig();
            return;
        }

        Map<String, PoolDefinition> loadedPools = new LinkedHashMap<>();
        String[] orderedIds = splitCsv(properties.getProperty("pool.ids", DEFAULT_POOL_ID));
        for (String poolId : orderedIds) {
            PoolDefinition definition = parsePool(poolId, properties);
            if (definition != null) {
                loadedPools.put(definition.id(), definition);
            }
        }

        if (loadedPools.isEmpty()) {
            loadDefaultTestConfig();
            return;
        }

        POOLS.clear();
        POOLS.putAll(loadedPools);
    }

    public static List<PoolDefinition> getPools(String[] enabledPoolIds) {
        if (enabledPoolIds == null || enabledPoolIds.length == 0) {
            return List.copyOf(POOLS.values());
        }

        List<PoolDefinition> enabledPools = new ArrayList<>();
        for (String poolId : enabledPoolIds) {
            PoolDefinition definition = POOLS.get(poolId);
            if (definition != null) {
                enabledPools.add(definition);
            }
        }

        if (enabledPools.isEmpty()) {
            return List.copyOf(POOLS.values());
        }
        return List.copyOf(enabledPools);
    }

    private static void ensureDefaultConfig(Path configFolder, Path configFile) {
        try {
            Files.createDirectories(configFolder);
            if (Files.exists(configFile)) {
                return;
            }

            Properties defaults = new Properties();
            defaults.setProperty("pool.ids", DEFAULT_POOL_ID);
            defaults.setProperty("pool." + DEFAULT_POOL_ID + ".label", "Prize Pool");
            defaults.setProperty("pool." + DEFAULT_POOL_ID + ".currencyItemId", "Ingredient_Life_Essence");
            defaults.setProperty("pool." + DEFAULT_POOL_ID + ".currencyCost", "1");
            defaults.setProperty("pool." + DEFAULT_POOL_ID + ".prizes", defaultPrizeConfigValue());

            try (OutputStream outputStream = Files.newOutputStream(configFile)) {
                defaults.store(outputStream, "Ev0s Gachapon pool configuration");
            }
        } catch (IOException ignored) {
        }
    }

    private static PoolDefinition parsePool(String poolId, Properties properties) {
        if (poolId == null || poolId.isBlank()) {
            return null;
        }

        String prefix = "pool." + poolId + ".";
        String label = properties.getProperty(prefix + "label", poolId);
        String currencyItemId = properties.getProperty(prefix + "currencyItemId", "Ingredient_Life_Essence");
        if (!isValidItemId(currencyItemId)) {
            return null;
        }
        int currencyCost = parseInt(properties.getProperty(prefix + "currencyCost"), 1);
        List<PrizeDefinition> prizes = parsePrizes(properties.getProperty(prefix + "prizes", defaultPrizeConfigValue()));
        if (prizes.isEmpty()) {
            List<PrizeDefinition> defaultPrizes = defaultPrizeDefinitions();
            if (defaultPrizes.isEmpty()) {
                return null;
            }
            prizes = defaultPrizes;
        }
        return new PoolDefinition(poolId, label, currencyItemId, Math.max(currencyCost, 1), prizes);
    }

    private static List<PrizeDefinition> parsePrizes(String prizeConfig) {
        if (prizeConfig == null || prizeConfig.isBlank()) {
            return Collections.emptyList();
        }

        List<PrizeDefinition> prizes = new ArrayList<>();
        String[] entries = splitCsv(prizeConfig);
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }

            String[] parts = trimmedEntry.split(":", 2);
            String itemId = parts[0].trim();
            int weight = 1;
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                weight = parseInt(parts[1], 1);
            }
            if (!itemId.isEmpty() && isValidItemId(itemId)) {
                prizes.add(new PrizeDefinition(itemId, Math.max(weight, 1)));
            }
        }
        return prizes;
    }

    private static boolean isValidItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }
        try {
            return new ItemStack(itemId.trim(), 1) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static List<PrizeDefinition> defaultPrizeDefinitions() {
        List<PrizeDefinition> prizes = new ArrayList<>();
        for (String itemId : DEFAULT_PRIZE_IDS) {
            if (isValidItemId(itemId)) {
                prizes.add(new PrizeDefinition(itemId, 1));
            }
        }
        return List.copyOf(prizes);
    }

    private static String defaultPrizeConfigValue() {
        return String.join(",", DEFAULT_PRIZE_IDS);
    }

    private static String[] splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        String[] parts = value.split(",");
        List<String> cleaned = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }
        return cleaned.toArray(new String[0]);
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static void loadDefaultTestConfig() {
        POOLS.clear();
        register(new PoolDefinition(
                DEFAULT_POOL_ID,
                "Prize Pool",
                "Ingredient_Life_Essence",
                1,
            defaultPrizeDefinitions()));
    }
}