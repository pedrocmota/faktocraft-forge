package com.faktocraft.common.network;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.packet.PacketExperience;
import com.faktocraft.common.network.packet.PacketExtruderRecipe;
import com.faktocraft.common.network.packet.PacketIEMeterInfo;
import com.faktocraft.common.network.packet.PacketMetalFormerChangeMode;
import com.faktocraft.common.network.packet.PacketNightVision;
import com.faktocraft.common.network.packet.PacketParticle;
import com.faktocraft.common.network.packet.PacketReplicatorAction;
import com.faktocraft.common.network.packet.PacketScannerCleanScan;
import com.faktocraft.common.network.packet.PacketScannerSaveScan;
import com.faktocraft.common.network.packet.PacketTeleportFx;
import com.faktocraft.common.network.packet.PacketTransformerMode;
import com.faktocraft.common.network.packet.PacketWindInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.BiConsumer;

public class ModNetworking {

  public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(Faktocraft.MODID, "main"), () -> "1", "1"::equals, "1"::equals);

  public static void init() {
    int id = 0;
    CHANNEL.registerMessage(id++, PacketExperience.class,
        PacketExperience::encode, PacketExperience::decode, PacketExperience::handle);
    CHANNEL.registerMessage(id++, PacketExtruderRecipe.class,
        PacketExtruderRecipe::encode, PacketExtruderRecipe::decode, PacketExtruderRecipe::handle);
    CHANNEL.registerMessage(id++, PacketTransformerMode.class,
        PacketTransformerMode::encode, PacketTransformerMode::decode, PacketTransformerMode::handle);
    CHANNEL.registerMessage(id++, PacketScannerCleanScan.class,
        PacketScannerCleanScan::encode, PacketScannerCleanScan::decode, PacketScannerCleanScan::handle);
    CHANNEL.registerMessage(id++, PacketScannerSaveScan.class,
        PacketScannerSaveScan::encode, PacketScannerSaveScan::decode, PacketScannerSaveScan::handle);
    CHANNEL.registerMessage(id++, PacketReplicatorAction.class,
        PacketReplicatorAction::encode, PacketReplicatorAction::decode, PacketReplicatorAction::handle);
    CHANNEL.registerMessage(id++, PacketMetalFormerChangeMode.class,
        PacketMetalFormerChangeMode::encode, PacketMetalFormerChangeMode::decode, PacketMetalFormerChangeMode::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketToggleDischarge.class,
        com.faktocraft.common.network.packet.PacketToggleDischarge::encode,
        com.faktocraft.common.network.packet.PacketToggleDischarge::decode,
        com.faktocraft.common.network.packet.PacketToggleDischarge::handle);
    CHANNEL.registerMessage(id++, PacketNightVision.class,
        PacketNightVision::encode, PacketNightVision::decode, PacketNightVision::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketRedstoneControl.class,
        com.faktocraft.common.network.packet.PacketRedstoneControl::encode,
        com.faktocraft.common.network.packet.PacketRedstoneControl::decode,
        com.faktocraft.common.network.packet.PacketRedstoneControl::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketPlungerDrain.class,
        com.faktocraft.common.network.packet.PacketPlungerDrain::encode,
        com.faktocraft.common.network.packet.PacketPlungerDrain::decode,
        com.faktocraft.common.network.packet.PacketPlungerDrain::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketCellFill.class,
        com.faktocraft.common.network.packet.PacketCellFill::encode,
        com.faktocraft.common.network.packet.PacketCellFill::decode,
        com.faktocraft.common.network.packet.PacketCellFill::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketCellDrain.class,
        com.faktocraft.common.network.packet.PacketCellDrain::encode,
        com.faktocraft.common.network.packet.PacketCellDrain::decode,
        com.faktocraft.common.network.packet.PacketCellDrain::handle);

    CHANNEL.registerMessage(id++, PacketParticle.class,
        PacketParticle::encode, PacketParticle::decode, PacketParticle::handle);
    CHANNEL.registerMessage(id++, PacketTeleportFx.class,
        PacketTeleportFx::encode, PacketTeleportFx::decode, PacketTeleportFx::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketTeleportCharge.class,
        com.faktocraft.common.network.packet.PacketTeleportCharge::encode,
        com.faktocraft.common.network.packet.PacketTeleportCharge::decode,
        com.faktocraft.common.network.packet.PacketTeleportCharge::handle);
    CHANNEL.registerMessage(id++, PacketIEMeterInfo.class,
        PacketIEMeterInfo::encode, PacketIEMeterInfo::decode, PacketIEMeterInfo::handle);
    CHANNEL.registerMessage(id++, PacketWindInfo.class,
        PacketWindInfo::encode, PacketWindInfo::decode, PacketWindInfo::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketProspectorScan.class,
        com.faktocraft.common.network.packet.PacketProspectorScan::encode,
        com.faktocraft.common.network.packet.PacketProspectorScan::decode,
        com.faktocraft.common.network.packet.PacketProspectorScan::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketJetpackInput.class,
        com.faktocraft.common.network.packet.PacketJetpackInput::encode,
        com.faktocraft.common.network.packet.PacketJetpackInput::decode,
        com.faktocraft.common.network.packet.PacketJetpackInput::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketJetpackMode.class,
        com.faktocraft.common.network.packet.PacketJetpackMode::encode,
        com.faktocraft.common.network.packet.PacketJetpackMode::decode,
        com.faktocraft.common.network.packet.PacketJetpackMode::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketAnchorBuffer.class,
        com.faktocraft.common.network.packet.PacketAnchorBuffer::encode,
        com.faktocraft.common.network.packet.PacketAnchorBuffer::decode,
        com.faktocraft.common.network.packet.PacketAnchorBuffer::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketAnchorScreen.class,
        com.faktocraft.common.network.packet.PacketAnchorScreen::encode,
        com.faktocraft.common.network.packet.PacketAnchorScreen::decode,
        com.faktocraft.common.network.packet.PacketAnchorScreen::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketGeoScannerState.class,
        com.faktocraft.common.network.packet.PacketGeoScannerState::encode,
        com.faktocraft.common.network.packet.PacketGeoScannerState::decode,
        com.faktocraft.common.network.packet.PacketGeoScannerState::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketGeoScannerPoll.class,
        com.faktocraft.common.network.packet.PacketGeoScannerPoll::encode,
        com.faktocraft.common.network.packet.PacketGeoScannerPoll::decode,
        com.faktocraft.common.network.packet.PacketGeoScannerPoll::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketGeoScannerControl.class,
        com.faktocraft.common.network.packet.PacketGeoScannerControl::encode,
        com.faktocraft.common.network.packet.PacketGeoScannerControl::decode,
        com.faktocraft.common.network.packet.PacketGeoScannerControl::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketGeoScannerManual.class,
        com.faktocraft.common.network.packet.PacketGeoScannerManual::encode,
        com.faktocraft.common.network.packet.PacketGeoScannerManual::decode,
        com.faktocraft.common.network.packet.PacketGeoScannerManual::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketLogisticsGhost.class,
        com.faktocraft.common.network.packet.PacketLogisticsGhost::encode,
        com.faktocraft.common.network.packet.PacketLogisticsGhost::decode,
        com.faktocraft.common.network.packet.PacketLogisticsGhost::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketReqTableState.class,
        com.faktocraft.common.network.packet.PacketReqTableState::encode,
        com.faktocraft.common.network.packet.PacketReqTableState::decode,
        com.faktocraft.common.network.packet.PacketReqTableState::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketTableState.class,
        com.faktocraft.common.network.packet.PacketTableState::encode,
        com.faktocraft.common.network.packet.PacketTableState::decode,
        com.faktocraft.common.network.packet.PacketTableState::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketModuleTree.class,
        com.faktocraft.common.network.packet.PacketModuleTree::encode,
        com.faktocraft.common.network.packet.PacketModuleTree::decode,
        com.faktocraft.common.network.packet.PacketModuleTree::handle);

    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketModuleTreeCount.class,
        com.faktocraft.common.network.packet.PacketModuleTreeCount::encode,
        com.faktocraft.common.network.packet.PacketModuleTreeCount::decode,
        com.faktocraft.common.network.packet.PacketModuleTreeCount::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketRequestTarget.class,
        com.faktocraft.common.network.packet.PacketRequestTarget::encode,
        com.faktocraft.common.network.packet.PacketRequestTarget::decode,
        com.faktocraft.common.network.packet.PacketRequestTarget::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketTableMessage.class,
        com.faktocraft.common.network.packet.PacketTableMessage::encode,
        com.faktocraft.common.network.packet.PacketTableMessage::decode,
        com.faktocraft.common.network.packet.PacketTableMessage::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketEnderTankCode.class,
        com.faktocraft.common.network.packet.PacketEnderTankCode::encode,
        com.faktocraft.common.network.packet.PacketEnderTankCode::decode,
        com.faktocraft.common.network.packet.PacketEnderTankCode::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketRecipePipeRecipes.class,
        com.faktocraft.common.network.packet.PacketRecipePipeRecipes::encode,
        com.faktocraft.common.network.packet.PacketRecipePipeRecipes::decode,
        com.faktocraft.common.network.packet.PacketRecipePipeRecipes::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketTaskHistoryOp.class,
        com.faktocraft.common.network.packet.PacketTaskHistoryOp::encode,
        com.faktocraft.common.network.packet.PacketTaskHistoryOp::decode,
        com.faktocraft.common.network.packet.PacketTaskHistoryOp::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketCraftPattern.class,
        com.faktocraft.common.network.packet.PacketCraftPattern::encode,
        com.faktocraft.common.network.packet.PacketCraftPattern::decode,
        com.faktocraft.common.network.packet.PacketCraftPattern::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketRecipePipeBind.class,
        com.faktocraft.common.network.packet.PacketRecipePipeBind::encode,
        com.faktocraft.common.network.packet.PacketRecipePipeBind::decode,
        com.faktocraft.common.network.packet.PacketRecipePipeBind::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketRecipePipeFill.class,
        com.faktocraft.common.network.packet.PacketRecipePipeFill::encode,
        com.faktocraft.common.network.packet.PacketRecipePipeFill::decode,
        com.faktocraft.common.network.packet.PacketRecipePipeFill::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketMenuAction.class,
        com.faktocraft.common.network.packet.PacketMenuAction::encode,
        com.faktocraft.common.network.packet.PacketMenuAction::decode,
        com.faktocraft.common.network.packet.PacketMenuAction::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketOpenCoreView.class,
        com.faktocraft.common.network.packet.PacketOpenCoreView::encode,
        com.faktocraft.common.network.packet.PacketOpenCoreView::decode,
        com.faktocraft.common.network.packet.PacketOpenCoreView::handle);
    CHANNEL.registerMessage(id++, com.faktocraft.common.network.packet.PacketCoreTasksReq.class,
        com.faktocraft.common.network.packet.PacketCoreTasksReq::encode,
        com.faktocraft.common.network.packet.PacketCoreTasksReq::decode,
        com.faktocraft.common.network.packet.PacketCoreTasksReq::handle);
  }

  public static void sendToServer(Object msg) {
    CHANNEL.sendToServer(msg);
  }

  public static void sendToPlayer(ServerPlayer player, Object msg) {
    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
  }

  public static void withBlockEntity(ServerPlayer player, BlockPos pos,
      BiConsumer<ServerPlayer, BlockEntity> handler) {
    if (!(player.distanceToSqr(Vec3.atCenterOf(pos)) <= 8.5 * 8.5)) {
      Faktocraft.LOGGER.debug("Rejected packet for out-of-reach block {} from {}", pos, player.getName().getString());
      return;
    }
    BlockEntity be = player.level().getBlockEntity(pos);
    if (be != null) {
      handler.accept(player, be);
    }
  }
}
