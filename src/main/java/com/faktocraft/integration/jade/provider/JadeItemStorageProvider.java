package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.util.transfer.CapabilityBridge;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.IItemHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class JadeItemStorageProvider
    implements IServerExtensionProvider<ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {

  public static final JadeItemStorageProvider INSTANCE = new JadeItemStorageProvider();

  private static final Identifier UID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "item_storage");
  private static final int PRIORITY = 900;

  @Override
  public Identifier getUid() {
    return UID;
  }

  @Override
  public int getDefaultPriority() {
    return PRIORITY;
  }

  @Override
  @Nullable
  public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
    if (!(accessor.getTarget() instanceof BlockEntity blockEntity)) {
      return null;
    }
    IItemHandler handler = CapabilityBridge.get(blockEntity, ForgeCapabilities.ITEM_HANDLER, null);
    if (handler == null) {
      return null;
    }
    List<ItemStack> merged = new ArrayList<>();
    for (int slot = 0; slot < handler.getSlots(); slot++) {
      ItemStack stack = handler.getStackInSlot(slot);
      if (stack.isEmpty()) {
        continue;
      }
      ItemStack existing = null;
      for (ItemStack candidate : merged) {
        if (candidate.getItem() == stack.getItem()) {
          existing = candidate;
          break;
        }
      }
      if (existing == null) {
        merged.add(stack.copy());
      } else {
        existing.grow(stack.getCount());
      }
    }
    return List.of(new ViewGroup<>(merged));
  }

  @Override
  public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
    return ClientViewGroup.map(groups, ItemView::new, null);
  }
}
