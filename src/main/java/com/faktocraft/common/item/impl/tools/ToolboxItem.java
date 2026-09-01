package com.faktocraft.common.item.impl.tools;

import com.faktocraft.common.item.base.DiggerElectricItem;
import com.faktocraft.common.registries.ModTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class ToolboxItem extends Item {

  public ToolboxItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static boolean isTool(ItemStack stack) {
    if (stack.getItem() instanceof ToolboxItem) {
      return false;
    }
    Item item = stack.getItem();
    return stack.is(ModTags.TOOLBOX_TOOLS)
        || item instanceof DiggerItem
        || item instanceof SwordItem
        || item instanceof ShearsItem
        || item instanceof FlintAndSteelItem
        || item instanceof FishingRodItem
        || item instanceof com.faktocraft.common.item.base.ToolItem
        || item instanceof DiggerElectricItem;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player instanceof ServerPlayer serverPlayer) {
      NetworkHooks.openScreen(serverPlayer,
          new SimpleMenuProvider((id, inv, p) -> new ToolboxMenu(id, inv, p.blockPosition()),
              stack.getHoverName()),
          buf -> buf.writeBlockPos(player.blockPosition()));
      level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_OPEN,
          net.minecraft.sounds.SoundSource.PLAYERS, 0.4F, 1.6F);
    }
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
  }

  @Override
  public java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> getTooltipImage(ItemStack stack) {
    net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList
        .withSize(ToolboxMenu.SIZE, ItemStack.EMPTY);
    boolean hasAny = false;
    if (stack.hasTag() && stack.getTag().contains("Items")) {
      net.minecraftforge.items.ItemStackHandler handler = new net.minecraftforge.items.ItemStackHandler(
          ToolboxMenu.SIZE);
      handler.deserializeNBT(stack.getTag().getCompound("Items"));
      for (int i = 0; i < ToolboxMenu.SIZE; i++) {
        items.set(i, handler.getStackInSlot(i));
        hasAny |= !handler.getStackInSlot(i).isEmpty();
      }
    }
    return hasAny ? java.util.Optional.of(new ToolboxTooltip(items)) : java.util.Optional.empty();
  }

  @Override
  public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
      java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    if (getTooltipImage(stack).isEmpty()) {
      tooltip.add(net.minecraft.network.chat.Component
          .translatable("tooltip." + com.faktocraft.Faktocraft.MODID + ".toolbox_empty")
          .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
