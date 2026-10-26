package com.echocore.item;

import com.echocore.entity.EchoEntity;
import com.echocore.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.List;

/** ECHO'yu uyandıran çekirdek. ECHO ölürse bu çekirdeği düşürür ve hafızasını içinde taşır. */
public class EchoCoreItem extends Item {

    public EchoCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        EchoEntity echo = ModEntities.ECHO.get().create(level);
        if (echo == null) {
            return InteractionResult.FAIL;
        }

        echo.moveTo(pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D, player.getYRot() + 180.0F, 0.0F);
        echo.tame(player);

        CompoundTag tag = context.getItemInHand().getTag();
        if (tag != null) {
            echo.restoreStory(tag.getInt("Chapter"), tag.getInt("FragmentsGiven"));
        }

        level.addFreshEntity(echo);
        echo.activate(player);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.echocore.echo_core.desc").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getInt("Chapter") > 0) {
            tooltip.add(Component.translatable("echocore.tooltip.memory", tag.getInt("Chapter"), EchoEntity.MAX_CHAPTER)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
