package com.buuz135.bettermodlist.gui;


import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.plugin.AuthorInfo;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModListGui extends InteractiveCustomUIPage<ModListGui.SearchGuiData> {

    private String searchQuery;
    private final List<CustomManifest> visibleItems;
    private boolean showOnlyWithDescription;
    private boolean showHytale;
    private final List<PluginManifest> plugins;
    private final List<PluginManifest> assetPacks;

    public ModListGui(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, SearchGuiData.CODEC);
        this.searchQuery = "";
        this.visibleItems = new ArrayList<>();
        this.showOnlyWithDescription = true;
        this.showHytale = true;
        this.plugins = new ArrayList<>(PluginManager.get().getAvailablePlugins().values());
        this.assetPacks = new ArrayList<>(AssetModule.get().getAssetPacks().stream().map(AssetPack::getManifest).toList());
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/Buuz135_BetterModlist_Gui.ui");
        uiCommandBuilder.set("#SearchInput.Value", this.searchQuery);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
        uiCommandBuilder.set("#ShowOnlyWithDesc #CheckBox.Value", this.showOnlyWithDescription);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ShowOnlyWithDesc #CheckBox", EventData.of("ShowOnlyDesc", "CAT"), false);
        uiCommandBuilder.set("#ShowHytale #CheckBox.Value", this.showHytale);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ShowHytale #CheckBox", EventData.of("ShowHytale", "CAT"), false);
        this.buildList(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SearchGuiData data) {
        super.handleDataEvent(ref, store, data);
        if (data.showOnlyDesc != null) {
            this.showOnlyWithDescription = !this.showOnlyWithDescription;
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.buildList(ref, commandBuilder, eventBuilder, store);
            this.sendUpdate(commandBuilder, eventBuilder, false);
        }
        if (data.showHytale != null) {
            this.showHytale = !this.showHytale;
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.buildList(ref, commandBuilder, eventBuilder, store);
            this.sendUpdate(commandBuilder, eventBuilder, false);
        }
        if (data.searchQuery != null) {
            this.searchQuery = data.searchQuery.trim().toLowerCase();
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.buildList(ref, commandBuilder, eventBuilder, store);
            this.sendUpdate(commandBuilder, eventBuilder, false);
        }
    }

    private void buildList(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

        assert playerComponent != null;

        List<CustomManifest> itemList = new ArrayList<>(this.plugins.stream().map(value -> new CustomManifest(value, false)).toList());
        for (PluginManifest value : this.assetPacks) {
            if (itemList.stream().anyMatch(pluginManifest -> pluginManifest.manifest.getGroup().equals(value.getGroup()) && pluginManifest.manifest.getName().equals(value.getName()))) {
                continue;
            }
            itemList.add(new CustomManifest(value, true));
        }
        itemList.sort(Comparator.comparing(customManifest -> customManifest.manifest.getName()));

        if (this.searchQuery.isEmpty()) {
            this.visibleItems.clear();
            for (CustomManifest pluginManifest : itemList) {
                if (this.showOnlyWithDescription && pluginManifest.manifest.getDescription() == null) continue;
                if (!this.showHytale && pluginManifest.manifest.getGroup().equals("Hytale")) continue;
                this.visibleItems.add(pluginManifest);
            }
        } else {
            this.visibleItems.clear();
            for (CustomManifest pluginManifest : itemList) {
                if (this.showOnlyWithDescription && pluginManifest.manifest.getDescription() == null) continue;
                if (!this.showHytale && pluginManifest.manifest.getGroup().equals("Hytale")) continue;
                if (pluginManifest.manifest.getName().toLowerCase().contains(this.searchQuery)) {
                    this.visibleItems.add(pluginManifest);
                    continue;
                }
                var description = pluginManifest.manifest.getDescription() != null ? pluginManifest.manifest.getDescription() : "No description";
                if (description.contains(this.searchQuery)) {
                    this.visibleItems.add(pluginManifest);
                    continue;
                }
                if (pluginManifest.manifest.getAuthors().stream().anyMatch(authorInfo -> authorInfo.getName().toLowerCase().contains(this.searchQuery))) {
                    this.visibleItems.add(pluginManifest);
                    continue;
                }
            }
        }
        this.buildButtons(this.visibleItems, playerComponent, commandBuilder, eventBuilder);
    }

    private void buildButtons(List<CustomManifest> list, @Nonnull Player playerComponent, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        uiCommandBuilder.clear("#ModCards");
        uiCommandBuilder.appendInline("#Main #ModList", "Group #ModCards { LayoutMode: Left; }");

        for (int i = 0; i < list.size(); i++) {
            generateModList(list.get(i).manifest, list.get(i).isAssetPack, i, playerComponent, uiCommandBuilder, eventBuilder);
        }
    }

    private void generateModList(PluginManifest value, boolean isAssetPack, int i, @Nonnull Player playerComponent, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        uiCommandBuilder.append("#ModCards", "Pages/Buuz135_BetterModlist_Entry.ui");
        uiCommandBuilder.set("#ModCards[" + i + "] #ModName.Text", value.getName());
        if (value.getDescription() != null) {
            uiCommandBuilder.set("#ModCards[" + i + "] #ModDescription.Text", value.getDescription());
        } else {
            uiCommandBuilder.remove("#ModCards[" + i + "] #ModDescription");
        }
        var version = "v" + value.getVersion();
        while (version.length() < 8) version = " " + version + " ";
        uiCommandBuilder.set("#ModCards[" + i + "] #ModVersion.Text", version);

        var authors = "By: ";
        if (value.getAuthors().isEmpty()) {
            authors += value.getGroup();
        } else {
            authors += value.getAuthors().stream().map(AuthorInfo::getName).reduce((a, b) -> a + ", " + b).orElse("");
        }
        uiCommandBuilder.set("#ModCards[" + i + "] #AuthorList.Text", authors);
        if (isAssetPack) {
            uiCommandBuilder.set("#ModCards[" + i + "] #Enabled.Visible", true);
            uiCommandBuilder.set("#ModCards[" + i + "] #Disabled.Visible", false);
            uiCommandBuilder.set("#ModCards[" + i + "] #IncludesAssets.Visible", true);
            uiCommandBuilder.set("#ModCards[" + i + "] #IncludesAssets.Text", "Asset Pack");
            uiCommandBuilder.set("#ModCards[" + i + "] #ModLogo.Background", "pack_icon_not_found.png");
        } else {
            uiCommandBuilder.set("#ModCards[" + i + "] #Enabled.Visible", PluginManager.get().getPlugins().stream().anyMatch(plugin -> plugin.getManifest().equals(value)));
            uiCommandBuilder.set("#ModCards[" + i + "] #Disabled.Visible", PluginManager.get().getPlugins().stream().noneMatch(plugin -> plugin.getManifest().equals(value)));
            uiCommandBuilder.set("#ModCards[" + i + "] #IncludesAssets.Visible", value.includesAssetPack());
        }
        if (value.getWebsite() != null) {
            uiCommandBuilder.set("#ModCards[" + i + "] #Website.Text", value.getWebsite());
        } else {
            uiCommandBuilder.set("#ModCards[" + i + "] #Website.Visible", false);
        }
        var iconName = value.getGroup() + "_" + value.getName() + ".png";
        if (CommonAssetRegistry.hasCommonAsset("UI/Custom/" + iconName)){
            uiCommandBuilder.set("#ModCards[" + i + "] #ModLogo.Background", iconName);
        }
    }

    public static class SearchGuiData {
        static final String KEY_SHOW_ONLY_DESC = "ShowOnlyDesc";
        static final String KEY_SHOW_HYTALE = "ShowHytale";
        static final String KEY_SEARCH_QUERY = "@SearchQuery";
        public static final BuilderCodec<SearchGuiData> CODEC = BuilderCodec.builder(SearchGuiData.class, SearchGuiData::new)
                .addField(new KeyedCodec<>(KEY_SEARCH_QUERY, Codec.STRING), (searchGuiData, s) -> searchGuiData.searchQuery = s, searchGuiData -> searchGuiData.searchQuery)
                .addField(new KeyedCodec<>(KEY_SHOW_ONLY_DESC, Codec.STRING), (searchGuiData, s) -> searchGuiData.showOnlyDesc = s, searchGuiData -> searchGuiData.showOnlyDesc)
                .addField(new KeyedCodec<>(KEY_SHOW_HYTALE, Codec.STRING), (searchGuiData, s) -> searchGuiData.showHytale = s, searchGuiData -> searchGuiData.showHytale)
                .build();

        private String showOnlyDesc;
        private String searchQuery;
        private String showHytale;

    }

    public class CustomManifest {
        public PluginManifest manifest;
        public boolean isAssetPack;

        public CustomManifest(PluginManifest manifest, boolean isAssetPack) {
            this.manifest = manifest;
            this.isAssetPack = isAssetPack;
        }
    }

}
