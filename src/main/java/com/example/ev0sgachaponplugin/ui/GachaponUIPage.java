package com.example.ev0sgachaponplugin.ui;

import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.builders.HyUIPage;
import au.ellie.hyui.builders.ItemIconBuilder;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.events.PageRefreshResult;
import com.example.ev0sgachaponplugin.Ev0sGachaponPlugin;
import com.example.ev0sgachaponplugin.machine.GachaponDefinitions;
import com.example.ev0sgachaponplugin.machine.GachaponService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class GachaponUIPage {

    private static final int PRIZE_GRID_COLUMNS = 5;
    private static final int PRIZE_GRID_ROWS = 4;
    private static final int PRIZES_PER_PAGE = PRIZE_GRID_COLUMNS * PRIZE_GRID_ROWS;
    private static final int MAIN_PREVIEW_COUNT = 5;
    private static final int ROULETTE_WINDOW = 7;
    private static final ConcurrentHashMap<PlayerRef, Boolean> ACTIVE_SPINS = new ConcurrentHashMap<>();

    private static final String STYLE = """
            <style>
                .page-shell {
                    layout-mode: Center;
                    anchor-width: 100%;
                    anchor-height: 100%;
                    horizontal-align: center;
                    vertical-align: middle;
                }
                .panel-body {
                    layout-mode: Top;
                    padding: 20 24;
                    spacing: 10;
                    horizontal-align: center;
                }
                .title-text {
                    font-size: 24;
                    font-weight: bold;
                    color: #ffffff;
                    padding-bottom: 8;
                    horizontal-align: center;
                }
                .muted-text {
                    font-size: 14;
                    color: #b8c7d6;
                    padding-bottom: 4;
                    horizontal-align: center;
                }
                .status-banner {
                    padding: 10 14;
                    margin-bottom: 8;
                    background-color: #2c4f3f;
                    color: #dcffe9;
                    font-size: 14;
                    horizontal-align: center;
                }
                .pool-list {
                    layout-mode: Top;
                    spacing: 12;
                    padding-top: 6;
                    padding-bottom: 8;
                    anchor-width: 100%;
                }
                .pool-card {
                    layout-mode: Top;
                    padding: 14 16;
                    background-color: #162330;
                    border-color: #324c63;
                    border-width: 1;
                    anchor-width: 100%;
                }
                .pool-title {
                    font-size: 18;
                    font-weight: bold;
                    color: #ffffff;
                    padding-bottom: 4;
                    horizontal-align: center;
                }
                .pool-subtitle {
                    font-size: 13;
                    color: #8ea7bd;
                    padding-bottom: 6;
                    horizontal-align: center;
                }
                .preview-row {
                    layout-mode: Left;
                    horizontal-align: center;
                    padding-top: 2;
                    padding-bottom: 8;
                }
                .preview-tile {
                    layout-mode: Center;
                    anchor-width: 54;
                    anchor-height: 54;
                    padding: 6;
                    margin-left: 5;
                    margin-right: 5;
                    background-color: #203244;
                    border-color: #567fa0;
                    border-width: 1;
                    horizontal-align: center;
                    vertical-align: middle;
                }
                .preview-icon {
                    anchor-width: 40;
                    anchor-height: 40;
                }
                .grid-wrap {
                    layout-mode: Top;
                    padding-top: 10;
                    padding-bottom: 6;
                    anchor-width: 100%;
                    horizontal-align: center;
                }
                .grid-row {
                    layout-mode: Center;
                    horizontal-align: center;
                    padding-top: 4;
                    padding-bottom: 4;
                }
                .prize-tile {
                    layout-mode: Top;
                    horizontal-align: center;
                    vertical-align: top;
                    margin-top: 4;
                    margin-bottom: 4;
                    margin-left: 5;
                    margin-right: 5;
                    padding: 10 6 8 6;
                    anchor-width: 118;
                    anchor-height: 142;
                    background-color: #162330;
                    border-color: #324c63;
                    border-width: 1;
                }
                .prize-button {
                    layout-mode: Center;
                    anchor-width: 64;
                    anchor-height: 64;
                    padding: 8;
                    margin-bottom: 6;
                    background-color: #203244;
                    border-color: #567fa0;
                    border-width: 1;
                    horizontal-align: center;
                    vertical-align: middle;
                }
                .prize-name {
                    font-size: 12;
                    color: #ffffff;
                    padding-bottom: 4;
                    horizontal-align: center;
                }
                .chance-pill {
                    font-size: 13;
                    color: #ffe2a8;
                    padding: 2 6;
                    horizontal-align: center;
                }
                .footer-row {
                    layout-mode: Center;
                    anchor-width: 100%;
                        spacing: 0;
                    padding-top: 4;
                    padding-bottom: 6;
                    horizontal-align: center;
                }
                .controls-shell {
                    layout-mode: Center;
                    anchor-width: 100%;
                    horizontal-align: center;
                    padding-top: 12;
                    padding-bottom: 8;
                }
                .action-stack {
                    layout-mode: Top;
                    anchor-width: 420;
                    spacing: 22;
                    horizontal-align: center;
                }
                .meta-row {
                    layout-mode: Left;
                    spacing: 8;
                    horizontal-align: center;
                    padding-bottom: 2;
                }
                .pager-row {
                    layout-mode: Center;
                    anchor-width: 100%;
                    spacing: 18;
                    horizontal-align: center;
                    padding-top: 4;
                    padding-bottom: 40;
                }
                .pager-label {
                    font-size: 14;
                    color: #d8e8f6;
                    padding-top: 8;
                    padding-bottom: 40;
                    min-width: 88;
                    horizontal-align: center;
                }
                .roulette-shell {
                    layout-mode: Top;
                    anchor-width: 100%;
                    horizontal-align: center;
                    padding-top: 8;
                    padding-bottom: 10;
                }
                .roulette-row {
                    layout-mode: Left;
                    horizontal-align: center;
                    padding-top: 2;
                    padding-bottom: 4;
                }
                .roulette-tile {
                    layout-mode: Center;
                    horizontal-align: center;
                    vertical-align: middle;
                    anchor-width: 92;
                    anchor-height: 108;
                    padding: 8 4 6 4;
                    margin-left: 4;
                    margin-right: 4;
                    background-color: #162330;
                    border-color: #324c63;
                    border-width: 1;
                }
                .roulette-focus {
                    background-color: #315b7a;
                    border-color: #b4dd6c;
                    border-width: 2;
                }
                .roulette-name {
                    font-size: 11;
                    color: #ffffff;
                    padding-top: 4;
                    horizontal-align: center;
                }
                .roulette-caption {
                    font-size: 13;
                    color: #d8e8f6;
                    horizontal-align: center;
                    padding-bottom: 4;
                }
                .result-shell {
                    layout-mode: Top;
                    horizontal-align: center;
                    padding-top: 8;
                    padding-bottom: 14;
                }
                .result-icon-frame {
                    layout-mode: Center;
                    anchor-width: 140;
                    anchor-height: 140;
                    padding: 14;
                    background-color: #315b7a;
                    border-color: #b4dd6c;
                    border-width: 2;
                    margin-bottom: 12;
                    horizontal-align: center;
                    vertical-align: middle;
                }
                .result-title {
                    font-size: 30;
                    font-weight: bold;
                    color: #ffffff;
                    horizontal-align: center;
                    padding-bottom: 4;
                }
                .result-subtitle {
                    font-size: 14;
                    color: #d8e8f6;
                    horizontal-align: center;
                    padding-bottom: 4;
                }
                .result-chip {
                    font-size: 13;
                    color: #ffe2a8;
                    horizontal-align: center;
                    padding: 4 8;
                    margin-bottom: 8;
                    background-color: #1f3142;
                    border-color: #567fa0;
                    border-width: 1;
                }
            </style>
            """;

    private GachaponUIPage() {
    }

    public static void openMain(PlayerRef playerRef,
                                Ref<EntityStore> playerEntity,
                                Store<EntityStore> store,
                                Vector3i blockPos,
                                String statusMessage) {
        GachaponService.MachineContext machineContext = GachaponService.resolveMachineContext(store.getExternalData().getWorld(), blockPos);
        String html = buildMainPage(machineContext, statusMessage);

        PageBuilder builder = PageBuilder.pageForPlayer(playerRef)
                .fromHtml(html)
                .withLifetime(CustomPageLifetime.CanDismissOrCloseThroughInteraction);

        List<GachaponDefinitions.PoolDefinition> pools = machineContext.pools();
        for (GachaponDefinitions.PoolDefinition pool : pools) {
            builder.addEventListener(buttonId(pool.id()), CustomUIEventBindingType.Activating,
                    (ignored, ctx) -> openPool(playerRef, playerEntity, store, blockPos, pool.id(), null, 0));
        }

        builder.open(store);
    }

    public static void openPool(PlayerRef playerRef,
                                Ref<EntityStore> playerEntity,
                                Store<EntityStore> store,
                                Vector3i blockPos,
                                String poolId,
                                String statusMessage) {
        openPool(playerRef, playerEntity, store, blockPos, poolId, statusMessage, 0);
    }

    private static void openPool(PlayerRef playerRef,
                                 Ref<EntityStore> playerEntity,
                                 Store<EntityStore> store,
                                 Vector3i blockPos,
                                 String poolId,
                                 String statusMessage,
                                 int requestedPage) {
        GachaponService.MachineContext machineContext = GachaponService.resolveMachineContext(store.getExternalData().getWorld(), blockPos);
        GachaponDefinitions.PoolDefinition selectedPool = null;
        for (GachaponDefinitions.PoolDefinition pool : machineContext.pools()) {
            if (pool.id().equals(poolId)) {
                selectedPool = pool;
                break;
            }
        }

        if (selectedPool == null) {
            openMain(playerRef, playerEntity, store, blockPos, "Selected pool is not enabled on this machine.");
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil(selectedPool.prizes().size() / (double) PRIZES_PER_PAGE));
        int pageIndex = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int currencyBalance = GachaponService.getCurrencyBalance(store, playerEntity, blockPos, selectedPool.id());
        String html = buildPoolPage(machineContext.machineName(), selectedPool, currencyBalance, statusMessage, pageIndex, totalPages);
        PageBuilder builder = PageBuilder.pageForPlayer(playerRef)
                .fromHtml(html)
                .withLifetime(CustomPageLifetime.CanDismissOrCloseThroughInteraction);

        GachaponDefinitions.PoolDefinition poolForAction = selectedPool;
        int pageForAction = pageIndex;
        builder.addEventListener("spinPool", CustomUIEventBindingType.Activating, (ignored, ctx) -> {
            startAnimatedSpin(playerRef, playerEntity, store, blockPos, poolForAction, pageForAction);
        });
        builder.addEventListener("backToMain", CustomUIEventBindingType.Activating,
                (ignored, ctx) -> openMain(playerRef, playerEntity, store, blockPos, null));

        if (pageForAction > 0) {
            builder.addEventListener("prevPage", CustomUIEventBindingType.Activating,
                    (ignored, ctx) -> openPool(playerRef, playerEntity, store, blockPos, poolForAction.id(), statusMessage, pageForAction - 1));
        }
        if (pageForAction < totalPages - 1) {
            builder.addEventListener("nextPage", CustomUIEventBindingType.Activating,
                    (ignored, ctx) -> openPool(playerRef, playerEntity, store, blockPos, poolForAction.id(), statusMessage, pageForAction + 1));
        }

        builder.open(store);
    }

    private static String buildMainPage(GachaponService.MachineContext machineContext, String statusMessage) {
        StringBuilder poolSections = new StringBuilder();
        for (GachaponDefinitions.PoolDefinition pool : machineContext.pools()) {
            poolSections.append("""
                    <div class="pool-card">
                        <p class="pool-title">%s</p>
                        <p class="pool-subtitle">%s prizes in rotation</p>
                        <div class="preview-row">%s</div>
                        <div class="meta-row">
                            <span class="item-icon" data-hyui-item-id="%s" style="anchor-width: 20; anchor-height: 20;"></span>
                            <p class="muted-text">%sx %s</p>
                        </div>
                        <p class="muted-text">Tap below to view the full prize page.</p>
                        <button id="%s">Open %s</button>
                    </div>
                    """.formatted(
                    escape(pool.label()),
                    pool.prizes().size(),
                    buildPoolPreview(pool.prizes()),
                    escape(pool.currencyItemId()),
                    pool.currencyCost(),
                    escape(displayName(pool.currencyItemId())),
                    buttonId(pool.id()),
                    escape(pool.label())));
        }

        return STYLE + """
                <div class="page-shell">
                    <div class="decorated-container" data-hyui-title="%s" style="anchor-width: 760; anchor-height: 760;">
                        <div class="container-contents panel-body">
                            <p class="title-text">Choose a prize pool</p>
                            %s
                            <div class="pool-list">%s</div>
                        </div>
                    </div>
                </div>
                """.formatted(
                escape(machineContext.machineName()),
                renderStatus(statusMessage),
                poolSections);
    }

    private static String buildPoolPage(String machineName,
                                        GachaponDefinitions.PoolDefinition pool,
                                        int currencyBalance,
                                        String statusMessage,
                                        int pageIndex,
                                        int totalPages) {
        String prizeGrid = buildPrizeGrid(pool.prizes(), pageIndex);

        return STYLE + """
                <div class="page-shell">
                    <div class="decorated-container" data-hyui-title="%s" style="anchor-width: 760; anchor-height: 980;">
                        <div class="container-contents panel-body">
                            <p class="title-text">%s</p>
                            <div class="meta-row">
                                <span class="item-icon" data-hyui-item-id="%s" style="anchor-width: 22; anchor-height: 22;"></span>
                                <p class="muted-text">Spin cost: %sx %s</p>
                            </div>
                            <div class="meta-row">
                                <span class="item-icon" data-hyui-item-id="%s" style="anchor-width: 22; anchor-height: 22;"></span>
                                <p class="muted-text">Current balance: %sx %s</p>
                            </div>
                            %s
                            <div class="grid-wrap">%s</div>
                            <div class="controls-shell">
                                <div class="action-stack">
                                    %s
                                    <div class="footer-row">
                                            <button id="spinPool" style="margin-right: 20;">Spin</button>
                                            <button id="backToMain" style="margin-left: 20;">Back</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                """.formatted(
                escape(machineName),
                escape(pool.label()),
                escape(pool.currencyItemId()),
                pool.currencyCost(),
                escape(displayName(pool.currencyItemId())),
                escape(pool.currencyItemId()),
                currencyBalance,
                escape(displayName(pool.currencyItemId())),
                renderStatus(statusMessage),
                prizeGrid,
                buildPager(pageIndex, totalPages));
    }

    private static String buildSpinPage(String machineName,
                                        GachaponDefinitions.PoolDefinition pool,
                                        int currencyBalance,
                                        int frameIndex,
                                        List<GachaponDefinitions.PrizeDefinition> sequence) {
        return STYLE + """
                <div class="page-shell">
                    <div class="decorated-container" data-hyui-title="%s" style="anchor-width: 760; anchor-height: 620;">
                        <div class="container-contents panel-body">
                            <div style="layout-mode: Top; horizontal-align: center;">
                                <p id="spinTitle" class="title-text">Spinning %s</p>
                                <div id="spinBalanceRow" class="meta-row">
                                    <span class="item-icon" data-hyui-item-id="%s" style="anchor-width: 22; anchor-height: 22;"></span>
                                    <p id="spinBalanceText" class="muted-text">Balance after charge: %sx %s</p>
                                </div>
                                <div id="rouletteShell" class="roulette-shell">
                                    <p id="rouletteCaption" class="roulette-caption">Rolling...</p>
                                    %s
                                </div>
                                <p id="spinHelperText" class="muted-text">The prize is granted when the row stops on the winning item.</p>
                            </div>
                        </div>
                    </div>
                </div>
                """.formatted(
                escape(machineName),
                escape(pool.label()),
                escape(pool.currencyItemId()),
                currencyBalance,
                escape(displayName(pool.currencyItemId())),
                buildRoulette(sequence, frameIndex));
    }

    private static String buildResultPage(String machineName,
                                          GachaponDefinitions.PoolDefinition pool,
                                          GachaponDefinitions.PrizeDefinition prize) {
        return STYLE + """
                <div class="page-shell">
                    <div class="decorated-container" data-hyui-title="%s" style="anchor-width: 760; anchor-height: 620;">
                        <div class="container-contents panel-body">
                            <p class="title-text">Prize Won</p>
                            <div class="result-shell">
                                <p class="result-chip">Winner</p>
                                <div class="result-icon-frame">
                                    <span class="item-icon" data-hyui-item-id="%s" style="anchor-width: 104; anchor-height: 104;"></span>
                                </div>
                                <p class="result-title">%s</p>
                                <p class="result-subtitle">Awarded from %s</p>
                            </div>
                            <div class="footer-row">
                                <button id="resultBack">Back to Prize Pool</button>
                            </div>
                        </div>
                    </div>
                </div>
                """.formatted(
                escape(machineName),
                escape(prize.itemId()),
                escape(displayName(prize.itemId())),
                escape(pool.label()));
    }

    private static String buildPrizeGrid(List<GachaponDefinitions.PrizeDefinition> prizes, int pageIndex) {
        StringBuilder builder = new StringBuilder();
        int startIndex = pageIndex * PRIZES_PER_PAGE;
        int endIndex = Math.min(startIndex + PRIZES_PER_PAGE, prizes.size());

        for (int localIndex = 0; localIndex < endIndex - startIndex; localIndex++) {
            if (localIndex % PRIZE_GRID_COLUMNS == 0) {
                builder.append("<div class=\"grid-row\">\n");
            }

            GachaponDefinitions.PrizeDefinition prize = prizes.get(startIndex + localIndex);
            builder.append("""
                    <div class="prize-tile">
                        <div class="prize-button" data-hyui-tooltiptext="%s">
                            <span class="item-icon" data-hyui-item-id="%s" style="anchor-width: 46; anchor-height: 46;"></span>
                        </div>
                        <p class="prize-name">%s</p>
                        <p class="chance-pill">%s</p>
                    </div>
                    """.formatted(
                    escape(displayName(prize.itemId())),
                    escape(prize.itemId()),
                    escape(trimToLength(displayName(prize.itemId()), 20)),
                    escape(formatChance(prize, prizes))));

            if (localIndex % PRIZE_GRID_COLUMNS == PRIZE_GRID_COLUMNS - 1 || startIndex + localIndex == endIndex - 1) {
                builder.append("</div>\n");
            }
        }
        return builder.toString();
    }

    private static String buildPoolPreview(List<GachaponDefinitions.PrizeDefinition> prizes) {
        StringBuilder builder = new StringBuilder();
        int count = Math.min(prizes.size(), MAIN_PREVIEW_COUNT);
        for (int index = 0; index < count; index++) {
            String itemId = prizes.get(index).itemId();
            builder.append("""
                    <div class="preview-tile">
                        <span class="item-icon preview-icon" data-hyui-item-id="%s"></span>
                    </div>
                    """.formatted(
                    escape(itemId)));
        }
        return builder.toString();
    }

    private static String buildRoulette(List<GachaponDefinitions.PrizeDefinition> sequence, int frameIndex) {
        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"roulette-row\">\n");
        for (int slot = 0; slot < ROULETTE_WINDOW; slot++) {
            int offset = slot - (ROULETTE_WINDOW / 2);
            int sequenceIndex = Math.max(0, Math.min(sequence.size() - 1, frameIndex + offset));
            GachaponDefinitions.PrizeDefinition prize = sequence.get(sequenceIndex);
            String tileClass = slot == ROULETTE_WINDOW / 2 ? "roulette-tile roulette-focus" : "roulette-tile";
            builder.append("""
                    <div class="%s">
                        <span id="%s" class="item-icon" data-hyui-item-id="%s" style="anchor-width: 40; anchor-height: 40;"></span>
                    </div>
                    """.formatted(
                    tileClass,
                    rouletteSlotId(slot),
                    escape(prize.itemId())));
        }
        builder.append("</div>\n");
        return builder.toString();
    }

    private static void startAnimatedSpin(PlayerRef playerRef,
                                          Ref<EntityStore> playerEntity,
                                          Store<EntityStore> store,
                                          Vector3i blockPos,
                                          GachaponDefinitions.PoolDefinition pool,
                                          int returnPage) {
        if (ACTIVE_SPINS.putIfAbsent(playerRef, Boolean.TRUE) != null) {
            openPool(playerRef, playerEntity, store, blockPos, pool.id(), "A spin is already in progress.", returnPage);
            return;
        }

        GachaponService.SpinPreparation preparation = GachaponService.prepareSpin(store, playerEntity, blockPos, pool.id());
        if (!preparation.success()) {
            ACTIVE_SPINS.remove(playerRef);
            openPool(playerRef, playerEntity, store, blockPos, pool.id(), preparation.message(), returnPage);
            return;
        }

        int postChargeBalance = GachaponService.getCurrencyBalance(store, playerEntity, blockPos, pool.id());
        List<GachaponDefinitions.PrizeDefinition> sequence = new ArrayList<>(preparation.animationSequence());
        int[] delays = {0, 120, 240, 360, 480, 600, 740, 900, 1080, 1260, 1400, 1500};
        int frameCount = Math.min(sequence.size(), delays.length);
        if (frameCount <= 0) {
            ACTIVE_SPINS.remove(playerRef);
            Player player = store.getComponent(playerEntity, Player.getComponentType());
            GachaponService.grantPrize(player, preparation.finalPrize());
            openPool(playerRef, playerEntity, store, blockPos, pool.id(),
                    "Won " + displayName(preparation.finalPrize().itemId()) + " from " + preparation.pool().label() + ".", returnPage);
            return;
        }

        try {
            long startTimeMs = System.currentTimeMillis();
            int[] lastFrameIndex = {0};
            boolean[] completed = {false};
            boolean[] finalizing = {false};
            String html = buildSpinPage("Plush Gachapon", preparation.pool(), postChargeBalance, 0, sequence);
            HyUIPage spinPage = PageBuilder.pageForPlayer(playerRef)
                    .fromHtml(html)
                    .withLifetime(CustomPageLifetime.CanDismissOrCloseThroughInteraction)
                    .withRefreshRate(50)
                    .onDismiss((ref, dismissedStore) -> ACTIVE_SPINS.remove(playerRef))
                    .onRefresh(page -> {
                        if (completed[0]) {
                            return PageRefreshResult.NONE;
                        }

                        long elapsed = System.currentTimeMillis() - startTimeMs;
                        int nextFrameIndex = resolveFrameIndex(delays, frameCount, elapsed);
                        boolean frameChanged = nextFrameIndex != lastFrameIndex[0];
                        if (frameChanged) {
                            updateRoulette(page, sequence, nextFrameIndex);
                            lastFrameIndex[0] = nextFrameIndex;
                        }

                        if (nextFrameIndex >= frameCount - 1) {
                            completed[0] = true;
                            if (!finalizing[0]) {
                                finalizing[0] = true;
                                finalizeAnimatedSpinOnWorldThread(
                                        store.getExternalData().getWorld(),
                                        playerRef,
                                        playerEntity,
                                        store,
                                        blockPos,
                                        pool.id(),
                                        returnPage,
                                        page,
                                        preparation.pool(),
                                        preparation.finalPrize());
                            }
                            return PageRefreshResult.UPDATE_CLEAR;
                        }

                        return frameChanged ? PageRefreshResult.UPDATE_CLEAR : PageRefreshResult.NONE;
                    })
                    .open(store);

            if (frameCount == 1) {
                completed[0] = true;
                finalizing[0] = true;
                finalizeAnimatedSpinOnWorldThread(
                        store.getExternalData().getWorld(),
                        playerRef,
                        playerEntity,
                        store,
                        blockPos,
                        pool.id(),
                        returnPage,
                        spinPage,
                        preparation.pool(),
                        preparation.finalPrize());
            }
        } catch (Throwable throwable) {
            ACTIVE_SPINS.remove(playerRef);
            logSpinError("Spin animation failed", throwable);
            openPool(playerRef, playerEntity, store, blockPos, pool.id(), "Spin animation failed.", returnPage);
        }
    }

    private static int resolveFrameIndex(int[] delays, int frameCount, long elapsedMs) {
        int frameIndex = 0;
        for (int index = 1; index < frameCount; index++) {
            if (elapsedMs >= delays[index]) {
                frameIndex = index;
            } else {
                break;
            }
        }
        return frameIndex;
    }

    private static void finalizeAnimatedSpinOnWorldThread(World world,
                                                          PlayerRef playerRef,
                                                          Ref<EntityStore> playerEntity,
                                                          Store<EntityStore> store,
                                                          Vector3i blockPos,
                                                          String poolId,
                                                          int returnPage,
                                                          HyUIPage page,
                                                          GachaponDefinitions.PoolDefinition pool,
                                                          GachaponDefinitions.PrizeDefinition prize) {
        if (world == null) {
            ACTIVE_SPINS.remove(playerRef);
            return;
        }

        world.execute(() -> {
            ACTIVE_SPINS.remove(playerRef);
            try {
                Player player = store.getComponent(playerEntity, Player.getComponentType());
                GachaponService.grantPrize(player, prize);
                player.notifyPickupItem(playerEntity, new ItemStack(prize.itemId(), 1), null, store);
                world.execute(() -> {
                    try {
                        openResult(playerRef, playerEntity, store, blockPos, poolId, returnPage, pool, prize);
                    } catch (Throwable throwable) {
                        logSpinError("Failed to open result page", throwable);
                        openPool(playerRef, playerEntity, store, blockPos, poolId,
                                "Won " + displayName(prize.itemId()) + " from " + pool.label() + ".",
                                returnPage);
                    }
                });
            } catch (Throwable throwable) {
                logSpinError("Failed to finalize animated spin", throwable);
                openPool(playerRef, playerEntity, store, blockPos, poolId,
                        "Won " + displayName(prize.itemId()) + " from " + pool.label() + ".",
                        returnPage);
            }
        });
    }

    private static void updateRoulette(HyUIPage page,
                                       List<GachaponDefinitions.PrizeDefinition> sequence,
                                       int frameIndex) {
        for (int slot = 0; slot < ROULETTE_WINDOW; slot++) {
            int offset = slot - (ROULETTE_WINDOW / 2);
            int sequenceIndex = Math.max(0, Math.min(sequence.size() - 1, frameIndex + offset));
            String itemId = sequence.get(sequenceIndex).itemId();
            page.editById(rouletteSlotId(slot), ItemIconBuilder.class,
                    icon -> icon.withItemId(itemId));
        }
    }

    private static void openResult(PlayerRef playerRef,
                                   Ref<EntityStore> playerEntity,
                                   Store<EntityStore> store,
                                   Vector3i blockPos,
                                   String poolId,
                                   int returnPage,
                                   GachaponDefinitions.PoolDefinition pool,
                                   GachaponDefinitions.PrizeDefinition prize) {
        String html = buildResultPage("Plush Gachapon", pool, prize);
        PageBuilder.pageForPlayer(playerRef)
                .fromHtml(html)
                .withLifetime(CustomPageLifetime.CanDismissOrCloseThroughInteraction)
                .addEventListener("resultBack", CustomUIEventBindingType.Activating,
                        (ignored, ctx) -> openPool(playerRef, playerEntity, store, blockPos, poolId, null, returnPage))
                .open(store);
    }

    private static void logSpinError(String message, Throwable throwable) {
        Ev0sGachaponPlugin plugin = Ev0sGachaponPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().at(Level.WARNING).log("[Ev0'sGachaponPlugin] " + message + ": " + throwable.getMessage());
        }
    }

    private static String rouletteSlotId(int slot) {
        return "rouletteSlot" + slot;
    }

    private static String buildPager(int pageIndex, int totalPages) {
        if (totalPages <= 1) {
            return "";
        }

        String prevButton = pageIndex > 0
                ? "<button id=\"prevPage\">Previous</button>"
            : "<button>Previous</button>";
        String nextButton = pageIndex < totalPages - 1
                ? "<button id=\"nextPage\">Next</button>"
            : "<button>Next</button>";

        return """
                <div class="pager-row">
                    %s
                    <p class="pager-label">Page %s / %s</p>
                    %s
                </div>
                """.formatted(prevButton, pageIndex + 1, totalPages, nextButton);
    }

    private static String buttonId(String poolId) {
        return "pool_" + poolId.toLowerCase(Locale.ROOT);
    }

    private static String renderStatus(String statusMessage) {
        if (statusMessage == null || statusMessage.isBlank()) {
            return "";
        }
        return "<p class=\"status-banner\">" + escape(statusMessage) + "</p>";
    }

    private static String formatChance(GachaponDefinitions.PrizeDefinition prize, List<GachaponDefinitions.PrizeDefinition> prizes) {
        int totalWeight = 0;
        for (GachaponDefinitions.PrizeDefinition entry : prizes) {
            totalWeight += Math.max(entry.weight(), 0);
        }
        if (totalWeight <= 0) {
            return "0%";
        }
        double percent = (prize.weight() * 100.0D) / totalWeight;
        return String.format(Locale.ROOT, "%.1f%%", percent);
    }

    private static String displayName(String itemId) {
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

    private static String trimToLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}