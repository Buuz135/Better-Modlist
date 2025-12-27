package com.buuz135.bettermodlist;

import com.buuz135.bettermodlist.gui.ModListGui;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;


public class ModLogo implements JsonAssetWithMap<String, DefaultAssetMap<String, ModLogo>> {

    public static final AssetCodec<String, ModLogo> CODEC = AssetBuilderCodec.builder(ModLogo.class, ModLogo::new, Codec.STRING, ModLogo::setId, ModLogo::getId, ModLogo::setData, ModLogo::getData)
            .build();


    protected String id;
    protected AssetExtraInfo.Data data;

    public ModLogo() {
    }



    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private void setData(AssetExtraInfo.Data data) {
        this.data = data;
    }

    public AssetExtraInfo.Data getData() {
        return this.data;
    }
}
