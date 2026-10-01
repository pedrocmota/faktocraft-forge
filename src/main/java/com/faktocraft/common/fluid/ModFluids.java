package com.faktocraft.common.fluid;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModFluids {

  public static final FluidSet COOLANT = createSet("coolant", "flowing_coolant", "liquid_coolant", 0, 0xA100FFFF,
      false, false, delayedContact("coolant",
          (level, living) -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 100))),
      5, 1000, 1000, MapColor.COLOR_CYAN, null, 0x03161A, 2.5F);
  public static final FluidSet BIOGAS = createSet("biogas", "flowing_biogas", "liquid_biogas", 0, 0xA1e6e312,
      false, false, null,
      5, 1000, 1000, MapColor.COLOR_YELLOW, null, 0x141303, 2.5F);
  public static final FluidSet BIOMASS = createSet("biomass", "flowing_biomass", "liquid_biomass", 0, 0xA11af038,
      false, false, null, MapColor.COLOR_GREEN);
  public static final FluidSet MATTER = createSet("matter", "flowing_matter", "liquid_matter", 6, 0xA1800e7c,
      false, false, (level, living) -> {
        living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300));
        living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200));
      }, 5, 1000, 1000, MapColor.COLOR_PURPLE, null, 0x120214, 2.0F);
  public static final FluidSet SULFURIC_ACID = createSet("sulfuric_acid", "flowing_sulfuric_acid",
      "liquid_sulfuric_acid", 0, 0x50FFFAE0,
      false, true,
      delayedContact("acid",
          (level, living) -> living.hurt(com.faktocraft.common.registries.ModDamageTypes.acid(level), 6.0F)),
      MapColor.SAND);
  public static final FluidSet OIL = createSet("oil", "flowing_oil", "liquid_oil", 0, 0xFA3A3A3A,
      false, false, delayedToxicity("oil"),
      25, 6000, 3000, MapColor.COLOR_BLACK, com.faktocraft.common.registries.ModSounds.OIL_BUBBLE,
      0x0A0A0A, 2.0F);
  public static final FluidSet FUEL = createSet("fuel", "flowing_fuel", "liquid_fuel", 0, 0x96DCAE3C,
      false, false, delayedToxicity("fuel"),
      5, 1000, 1000, MapColor.COLOR_YELLOW, null,
      0x181104, 2.5F);

  private static BiConsumer<Level, LivingEntity> delayedContact(String key,
      BiConsumer<Level, LivingEntity> effect) {
    String lastKey = "faktocraft_" + key + "_last";
    String startKey = "faktocraft_" + key + "_start";
    return (level, living) -> {
      var data = living.getPersistentData();
      long now = level.getGameTime();
      long last = data.getLong(lastKey);
      long start = data.getLong(startKey);
      if (now - last > 5 || start == 0) {
        start = now;
      }
      data.putLong(lastKey, now);
      data.putLong(startKey, start);
      if (now - start >= 60) {
        effect.accept(level, living);
      }
    };
  }

  private static BiConsumer<Level, LivingEntity> delayedToxicity(String key) {
    return delayedContact(key, (level, living) -> {
      living.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
      living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
    });
  }

  public record FluidSet(FlowingFluid still, FlowingFluid flowing, Block block, FluidType fluidType) {
  }

  private static final ResourceLocation UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");

  private static FluidSet createSet(String stillName, String flowingName, String blockName, int lightLevel, int tint,
      boolean vaporizesOnPlacement, boolean corrosive, @Nullable BiConsumer<Level, LivingEntity> contactEffect,
      MapColor mapColor) {
    return createSet(stillName, flowingName, blockName, lightLevel, tint, vaporizesOnPlacement, corrosive,
        contactEffect, 5, 1000, 1000, mapColor, null, null, null);
  }

  private static FluidSet createSet(String stillName, String flowingName, String blockName, int lightLevel, int tint,
      boolean vaporizesOnPlacement, boolean corrosive, @Nullable BiConsumer<Level, LivingEntity> contactEffect,
      int tickRate, int viscosity, int density, MapColor mapColor,
      @Nullable net.minecraft.sounds.SoundEvent ambientSound,
      @Nullable Integer fogColorOverride, @Nullable Float fogEndOverride) {
    FlowingFluid[] holder = new FlowingFluid[2];
    Block[] blockHolder = new Block[1];

    ResourceLocation stillTexture = new ResourceLocation(Faktocraft.MODID, "block/fluid/" + stillName + "_still");
    ResourceLocation flowingTexture = new ResourceLocation(Faktocraft.MODID, "block/fluid/" + stillName + "_flow");
    ResourceLocation overlayTexture = new ResourceLocation(Faktocraft.MODID, "block/fluid/" + stillName + "_overlay");
    FluidType fluidType = new FluidType(FluidType.Properties.create().viscosity(viscosity).density(density)) {
      @Override
      public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return vaporizesOnPlacement || super.isVaporizedOnPlacement(level, pos, stack);
      }

      @Override
      public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        int fogSource = fogColorOverride != null ? fogColorOverride : tint;
        float fogR = ((fogSource >> 16) & 0xFF) / 255.0F;
        float fogG = ((fogSource >> 8) & 0xFF) / 255.0F;
        float fogB = (fogSource & 0xFF) / 255.0F;
        float opacity = ((tint >>> 24) & 0xFF) / 255.0F;
        float fogEnd = fogEndOverride != null ? fogEndOverride : net.minecraft.util.Mth.lerp(opacity, 16.0F, 3.0F);
        com.faktocraft.client.render.FluidFogVolume.register(this, fogR, fogG, fogB, fogEnd);
        consumer.accept(new IClientFluidTypeExtensions() {
          @Override
          public ResourceLocation getStillTexture() {
            return stillTexture;
          }

          @Override
          public ResourceLocation getFlowingTexture() {
            return flowingTexture;
          }

          @Override
          public ResourceLocation getOverlayTexture() {
            return overlayTexture;
          }

          @Override
          public int getTintColor() {
            return tint;
          }

          @Override
          public ResourceLocation getRenderOverlayTexture(net.minecraft.client.Minecraft mc) {
            return UNDERWATER_OVERLAY;
          }

          @Override
          public org.joml.Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick,
              net.minecraft.client.multiplayer.ClientLevel level, int renderDistance, float darkenWorldAmount,
              org.joml.Vector3f fluidFogColor) {
            return new org.joml.Vector3f(fogR, fogG, fogB);
          }

          @Override
          public void modifyFogRender(net.minecraft.client.Camera camera,
              net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance, float partialTick,
              float nearDistance, float farDistance, com.mojang.blaze3d.shaders.FogShape shape) {
            com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(0.0F);
            com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(fogEnd);
          }
        });
      }
    };

    ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(
        () -> fluidType, () -> holder[0], () -> holder[1])
        .slopeFindDistance(2)
        .levelDecreasePerBlock(2)
        .tickRate(tickRate)
        .explosionResistance(100.0F)
        .block(() -> (LiquidBlock) blockHolder[0]);

    FlowingFluid still = new BaseFluid.Source(properties);
    FlowingFluid flowing = new BaseFluid.Flowing(properties);
    holder[0] = still;
    holder[1] = flowing;

    RegistrationHandler.fluid(stillName, still);
    RegistrationHandler.fluid(flowingName, flowing);
    RegistrationHandler.enqueue(ForgeRegistries.Keys.FLUID_TYPES, stillName, fluidType);

    BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
        .mapColor(mapColor)
        .replaceable()
        .noCollission()
        .strength(100.0F)
        .pushReaction(PushReaction.DESTROY)
        .noLootTable()
        .liquid()
        .sound(SoundType.EMPTY);
    if (lightLevel > 0) {
      props = props.lightLevel(state -> lightLevel);
    }
    Block block = ModBlocks.register(blockName, p -> new LiquidBlock(() -> (FlowingFluid) holder[0], p) {
      @Override
      public void animateTick(BlockState state, Level level, BlockPos pos,
          net.minecraft.util.RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (ambientSound != null && random.nextInt(90) == 0) {
          level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ambientSound,
              net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, 0.4F + random.nextFloat() * 0.3F, false);
        }
      }

      @Override
      public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
          return;
        }
        if (corrosive && entity instanceof net.minecraft.world.entity.item.ItemEntity itemEntity) {
          var stack = itemEntity.getItem();
          if (stack.getItem().isEdible()
              || stack.is(com.faktocraft.common.registries.ModTags.ACID_SOLUBLE)) {
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.LAVA_EXTINGUISH,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, 1.4F);
            itemEntity.discard();
          }
          return;
        }
        if (!(entity instanceof LivingEntity living)) {
          return;
        }
        if (com.faktocraft.common.item.impl.armor.HazmatArmorItem.isFullSuit(living)) {
          if (corrosive && living.tickCount % 40 == 0) {
            com.faktocraft.common.item.impl.armor.HazmatArmorItem.wearFullSuit(living, 1);
          }
        } else if (contactEffect != null
            && !com.faktocraft.common.item.impl.nano.ItemNanoArmor.tryUseFullSuit(living,
                com.faktocraft.common.item.impl.nano.ItemNanoArmor.FLUID_COST_PER_TICK * (corrosive ? 2 : 1))) {
          contactEffect.accept(level, living);
        }
      }
    }, props);
    blockHolder[0] = block;

    return new FluidSet(still, flowing, block, fluidType);
  }

  public static void register() {
  }
}
