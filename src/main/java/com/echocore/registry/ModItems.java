package com.echocore.registry;

import com.echocore.EchoCoreMod;
import com.echocore.item.EchoCoreItem;
import com.echocore.item.MemoryFragmentItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EchoCoreMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoCoreMod.MODID);

    public static final RegistryObject<Item> ECHO_CORE = ITEMS.register("echo_core",
            () -> new EchoCoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> MEMORY_FRAGMENT = ITEMS.register("memory_fragment",
            () -> new MemoryFragmentItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ECHO_SPAWN_EGG = ITEMS.register("echo_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ECHO, 0x3FF2FF, 0x1C2330, new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("echocore",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.echocore"))
                    .icon(() -> new ItemStack(ECHO_CORE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ECHO_CORE.get());
                        output.accept(MEMORY_FRAGMENT.get());
                        output.accept(ECHO_SPAWN_EGG.get());
                    })
                    .build());
}
