package com.buuz135.bettermodlist;

import com.buuz135.bettermodlist.command.ModlistCommand;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.ScriptedBrushAsset;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;

import javax.annotation.Nonnull;

public class Main extends JavaPlugin {

    private static Main INSTANCE;

    public static Main getInstance() {
        return INSTANCE;
    }

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
    }

    @Override
    protected void setup() {
        super.setup();
        this.getCommandRegistry().registerCommand(new ModlistCommand());

    }

}