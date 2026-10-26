package com.echocore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.List;

/** ECHO'nun kaybolan anılarından biri. ECHO'ya sağ tıklayarak verilir. */
public class MemoryFragmentItem extends Item {

    public MemoryFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.echocore.memory_fragment.desc").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
    }
}
