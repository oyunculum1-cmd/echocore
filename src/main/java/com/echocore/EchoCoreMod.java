package com.echocore;

import com.echocore.registry.ModEntities;
import com.echocore.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EchoCoreMod.MODID)
public class EchoCoreMod {
    public static final String MODID = "echocore";

    public EchoCoreMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
    }
}
