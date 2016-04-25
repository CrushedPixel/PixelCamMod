package com.replaymod.pixelcam.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class LoadingPlugin implements IFMLLoadingPlugin {

    public LoadingPlugin() {
        System.out.println("dis shit been constructed");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                CameraTiltCT.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}

