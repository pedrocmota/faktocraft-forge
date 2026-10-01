package com.faktocraft.common.network;

import com.faktocraft.Faktocraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import java.util.function.BiConsumer;

public class ModNetworking {
  public static final String VERSION = "1";

  public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(VERSION);
    registrar.playToServer(com.faktocraft.common.network.packet.PacketExperience.TYPE,
        com.faktocraft.common.network.packet.PacketExperience.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketExperience.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketExtruderRecipe.TYPE,
        com.faktocraft.common.network.packet.PacketExtruderRecipe.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketExtruderRecipe.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketTransformerMode.TYPE,
        com.faktocraft.common.network.packet.PacketTransformerMode.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketTransformerMode.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketScannerCleanScan.TYPE,
        com.faktocraft.common.network.packet.PacketScannerCleanScan.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketScannerCleanScan.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketScannerSaveScan.TYPE,
        com.faktocraft.common.network.packet.PacketScannerSaveScan.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketScannerSaveScan.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketReplicatorAction.TYPE,
        com.faktocraft.common.network.packet.PacketReplicatorAction.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketReplicatorAction.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketMetalFormerChangeMode.TYPE,
        com.faktocraft.common.network.packet.PacketMetalFormerChangeMode.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketMetalFormerChangeMode.handle(msg,
            new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketToggleDischarge.TYPE,
        com.faktocraft.common.network.packet.PacketToggleDischarge.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketToggleDischarge.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketNightVision.TYPE,
        com.faktocraft.common.network.packet.PacketNightVision.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketNightVision.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketRedstoneControl.TYPE,
        com.faktocraft.common.network.packet.PacketRedstoneControl.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketRedstoneControl.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketPlungerDrain.TYPE,
        com.faktocraft.common.network.packet.PacketPlungerDrain.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketPlungerDrain.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketCellFill.TYPE,
        com.faktocraft.common.network.packet.PacketCellFill.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketCellFill.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketCellDrain.TYPE,
        com.faktocraft.common.network.packet.PacketCellDrain.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketCellDrain.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketParticle.TYPE,
        com.faktocraft.common.network.packet.PacketParticle.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketParticle.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketTeleportFx.TYPE,
        com.faktocraft.common.network.packet.PacketTeleportFx.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketTeleportFx.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketTeleportCharge.TYPE,
        com.faktocraft.common.network.packet.PacketTeleportCharge.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketTeleportCharge.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketIEMeterInfo.TYPE,
        com.faktocraft.common.network.packet.PacketIEMeterInfo.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketIEMeterInfo.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketWindInfo.TYPE,
        com.faktocraft.common.network.packet.PacketWindInfo.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketWindInfo.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketProspectorScan.TYPE,
        com.faktocraft.common.network.packet.PacketProspectorScan.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketProspectorScan.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketJetpackInput.TYPE,
        com.faktocraft.common.network.packet.PacketJetpackInput.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketJetpackInput.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketJetpackMode.TYPE,
        com.faktocraft.common.network.packet.PacketJetpackMode.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketJetpackMode.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketAnchorBuffer.TYPE,
        com.faktocraft.common.network.packet.PacketAnchorBuffer.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketAnchorBuffer.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketAnchorScreen.TYPE,
        com.faktocraft.common.network.packet.PacketAnchorScreen.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketAnchorScreen.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketGeoScannerState.TYPE,
        com.faktocraft.common.network.packet.PacketGeoScannerState.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketGeoScannerState.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketGeoScannerPoll.TYPE,
        com.faktocraft.common.network.packet.PacketGeoScannerPoll.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketGeoScannerPoll.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketGeoScannerControl.TYPE,
        com.faktocraft.common.network.packet.PacketGeoScannerControl.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketGeoScannerControl.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketGeoScannerManual.TYPE,
        com.faktocraft.common.network.packet.PacketGeoScannerManual.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketGeoScannerManual.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketScanCode.TYPE,
        com.faktocraft.common.network.packet.PacketScanCode.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketScanCode.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketProspectorPoll.TYPE,
        com.faktocraft.common.network.packet.PacketProspectorPoll.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketProspectorPoll.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketProspectorState.TYPE,
        com.faktocraft.common.network.packet.PacketProspectorState.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketProspectorState.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketLogisticsGhost.TYPE,
        com.faktocraft.common.network.packet.PacketLogisticsGhost.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketLogisticsGhost.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketReqTableState.TYPE,
        com.faktocraft.common.network.packet.PacketReqTableState.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketReqTableState.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketTableState.TYPE,
        com.faktocraft.common.network.packet.PacketTableState.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketTableState.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketModuleTree.TYPE,
        com.faktocraft.common.network.packet.PacketModuleTree.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketModuleTree.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketModuleTreeCount.TYPE,
        com.faktocraft.common.network.packet.PacketModuleTreeCount.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketModuleTreeCount.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketRequestTarget.TYPE,
        com.faktocraft.common.network.packet.PacketRequestTarget.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketRequestTarget.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketTableMessage.TYPE,
        com.faktocraft.common.network.packet.PacketTableMessage.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketTableMessage.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketEnderTankCode.TYPE,
        com.faktocraft.common.network.packet.PacketEnderTankCode.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketEnderTankCode.handle(msg, new PacketContext(ctx)));
    registrar.playToClient(com.faktocraft.common.network.packet.PacketRecipePipeRecipes.TYPE,
        com.faktocraft.common.network.packet.PacketRecipePipeRecipes.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketRecipePipeRecipes.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketTaskHistoryOp.TYPE,
        com.faktocraft.common.network.packet.PacketTaskHistoryOp.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketTaskHistoryOp.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketCraftPattern.TYPE,
        com.faktocraft.common.network.packet.PacketCraftPattern.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketCraftPattern.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketRecipePipeBind.TYPE,
        com.faktocraft.common.network.packet.PacketRecipePipeBind.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketRecipePipeBind.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketRecipePipeFill.TYPE,
        com.faktocraft.common.network.packet.PacketRecipePipeFill.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketRecipePipeFill.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketMenuAction.TYPE,
        com.faktocraft.common.network.packet.PacketMenuAction.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketMenuAction.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketOpenCoreView.TYPE,
        com.faktocraft.common.network.packet.PacketOpenCoreView.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketOpenCoreView.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketCoreTasksReq.TYPE,
        com.faktocraft.common.network.packet.PacketCoreTasksReq.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketCoreTasksReq.handle(msg, new PacketContext(ctx)));
    registrar.playToServer(com.faktocraft.common.network.packet.PacketVeinMining.TYPE,
        com.faktocraft.common.network.packet.PacketVeinMining.STREAM_CODEC,
        (msg, ctx) -> com.faktocraft.common.network.packet.PacketVeinMining.handle(msg, new PacketContext(ctx)));
  }

  public static void sendToServer(CustomPacketPayload msg) {
    ClientPacketDistributor.sendToServer(msg);
  }

  public static void sendToPlayer(ServerPlayer player, CustomPacketPayload msg) {
    PacketDistributor.sendToPlayer(player, msg);
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
