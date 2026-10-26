package com.echocore.registry;

import com.echocore.EchoCoreMod;
import com.echocore.entity.EchoEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EchoCoreMod.MODID);

    public static final RegistryObject<EntityType<EchoEntity>> ECHO = ENTITIES.register("echo",
            () -> EntityType.Builder.of(EchoEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 1.0F)
                    .clientTrackingRange(10)
                    .build(EchoCoreMod.MODID + ":echo"));
}
