package com.buuz135.bettermodlist.gui;


import com.buuz135.bettermodlist.Main;
import com.buuz135.bettermodlist.util.MessageHelper;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.plugin.AuthorInfo;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.CustomPageLifetime;
import com.hypixel.hytale.protocol.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.MatchResult;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.plugin.PluginClassLoader;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class ModListGui extends InteractiveCustomUIPage<ModListGui.SearchGuiData> {

    private String searchQuery = "";
    private final List<PluginManifest> visibleItems = new ArrayList<>();
    private boolean showOnlyWithDescription;

    public ModListGui(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, SearchGuiData.CODEC);
        this.searchQuery = "";
        this.showOnlyWithDescription = true;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/BetterModlistGui.ui");
        uiCommandBuilder.set("#SearchInput.Value", this.searchQuery);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
        uiCommandBuilder.set("#ShowOnlyWithDesc #CheckBox.Value", this.showOnlyWithDescription);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ShowOnlyWithDesc #CheckBox", EventData.of("ShowOnlyDesc", "CAT"), false);
        this.buildList(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SearchGuiData data) {
        super.handleDataEvent(ref, store, data);
        if (data.showOnlyDesc != null){
            this.showOnlyWithDescription = !this.showOnlyWithDescription;
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
        List<PluginManifest> itemList = new ArrayList<>();
        itemList.addAll(PluginManager.get().getAvailablePlugins().values());

        Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

        assert playerComponent != null;

        if (this.searchQuery.isEmpty()) {
            this.visibleItems.clear();
            for (PluginManifest pluginManifest : itemList) {
                if (this.showOnlyWithDescription && pluginManifest.getDescription() == null) continue;
                this.visibleItems.add(pluginManifest);
            }
        } else {
            this.visibleItems.clear();
            for (PluginManifest pluginManifest : itemList) {
                if (this.showOnlyWithDescription && pluginManifest.getDescription() == null) continue;
                if (pluginManifest.getName().toLowerCase().contains(this.searchQuery)) {
                    this.visibleItems.add(pluginManifest);
                    continue;
                }
                var description = pluginManifest.getDescription() != null ? pluginManifest.getDescription() : "No description";
                if (description.contains(this.searchQuery)) {
                    this.visibleItems.add(pluginManifest);
                    continue;
                }
                if (pluginManifest.getAuthors().stream().anyMatch(authorInfo -> authorInfo.getName().toLowerCase().contains(this.searchQuery))){
                    this.visibleItems.add(pluginManifest);
                    continue;
                }
            }
        }
        this.buildButtons(this.visibleItems, playerComponent, commandBuilder, eventBuilder);
    }

    private void buildButtons(List<PluginManifest> items, @Nonnull Player playerComponent, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        uiCommandBuilder.clear("#ModCards");
        uiCommandBuilder.appendInline("#Main #ModList", "Group #ModCards { LayoutMode: Left; }");
        var i = 0;
        for (PluginManifest value : items) {
            uiCommandBuilder.append("#ModCards", "Pages/BetterModlistEntry.ui");
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
            if (value.getAuthors().isEmpty()){
                authors += value.getGroup();
            } else {
                authors += value.getAuthors().stream().map(AuthorInfo::getName).reduce((a, b) -> a + ", " + b).orElse("");
            }
            uiCommandBuilder.set("#ModCards[" + i + "] #AuthorList.Text", authors);
            uiCommandBuilder.set("#ModCards[" + i + "] #Enabled.Visible", PluginManager.get().getPlugins().stream().anyMatch(plugin -> plugin.getManifest().equals(value)));
            uiCommandBuilder.set("#ModCards[" + i + "] #Disabled.Visible", PluginManager.get().getPlugins().stream().noneMatch(plugin -> plugin.getManifest().equals(value)));
            uiCommandBuilder.set("#ModCards[" + i + "] #IncludesAssets.Visible", value.includesAssetPack());
            if (value.getWebsite() != null) {
                uiCommandBuilder.set("#ModCards[" + i + "] #Website.Text", value.getWebsite());
            } else {
                uiCommandBuilder.set("#ModCards[" + i + "] #Website.Visible", false);
            }

            if (!value.getGroup().equals("Hytale")) uiCommandBuilder.set("#ModCards[" + i + "] #ModLogo.Background", value.getGroup() + "_" + value.getName() + ".png");
            ++i;
        }
    }

    public static class SearchGuiData {
        static final String KEY_SHOW_ONLY_DESC = "ShowOnlyDesc";
        static final String KEY_SEARCH_QUERY = "@SearchQuery";
        public static final BuilderCodec<SearchGuiData> CODEC = BuilderCodec.<SearchGuiData>builder(SearchGuiData.class, SearchGuiData::new)
                .addField(new KeyedCodec<>(KEY_SEARCH_QUERY, Codec.STRING), (searchGuiData, s) -> searchGuiData.searchQuery = s, searchGuiData -> searchGuiData.searchQuery)
                .addField(new KeyedCodec<>(KEY_SHOW_ONLY_DESC, Codec.STRING), (searchGuiData, s) -> searchGuiData.showOnlyDesc = s, searchGuiData -> searchGuiData.showOnlyDesc).build();

        private String showOnlyDesc;
        private String searchQuery;

    }

}
