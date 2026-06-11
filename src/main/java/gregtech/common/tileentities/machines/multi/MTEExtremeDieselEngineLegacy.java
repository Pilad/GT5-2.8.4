package gregtech.common.tileentities.machines.multi;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_EXTREME_DIESEL_ENGINE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_EXTREME_DIESEL_ENGINE_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_EXTREME_DIESEL_ENGINE_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_EXTREME_DIESEL_ENGINE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.casingTexturePages;
import static gregtech.api.util.GTUtility.validMTEList;
import static gregtech.api.util.NumberFormatUtil.formatNumber;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.maps.FuelBackend;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.tooltip.TooltipHelper;

public class MTEExtremeDieselEngineLegacy extends MTEDieselEngineLegacy {

    public MTEExtremeDieselEngineLegacy(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEExtremeDieselEngineLegacy(String aName) {
        super(aName);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        String lubricantRate = TooltipHelper.fluidText(8000);
        String oxygenRate = TooltipHelper.fluidRateText(40);
        String defaultOutput = TooltipHelper.euRateText(10900);
        String defaultEfficiency = TooltipHelper.effText(1.0f);
        String boostedOutput = TooltipHelper.euRateText(32700);
        String boostedEfficiency = TooltipHelper.effText(1.5f);
        String waitPower = TooltipHelper.effText(3.0f);

        tt.addMachineType("Combustion Generator, ECE")
            .addInfo(GTUtility.translate("gt.multiblock.DieselEngine.desc1_1", lubricantRate))
            .addInfo(GTUtility.translate("gt.multiblock.DieselEngine.desc2_1", oxygenRate))
            .addInfo(GTUtility.translate("gt.multiblock.DieselEngine.default_output", defaultOutput, defaultEfficiency))
            .addInfo(GTUtility.translate("gt.multiblock.DieselEngine.boosted_output", boostedOutput, boostedEfficiency))
            .addInfo(GTUtility.translate("gt.multiblock.DieselEngine.wait_power", waitPower))
            .addInfo(GTUtility.translate("gt.multiblock.DieselEngine.intake_warning2"))
            .addPollutionAmount(getPollutionPerSecond(null))
            .beginStructureBlock(3, 3, 4, false)
            .addController("Front center")
            .addCasingInfoRange("Robust Tungstensteel Machine Casing", 16, 22, false)
            .addOtherStructurePart("Titanium Gear Box Machine Casing", "Inner 2 blocks")
            .addOtherStructurePart("Extreme Engine Intake Machine Casing", "8x, ring around controller")
            .addStructureInfo("Extreme Engine Intake Casings must not be obstructed in front (only air blocks)")
            .addDynamoHatch("Back center", 2)
            .addMaintenanceHatch("Any Casing next to a Gear Box", 1)
            .addMufflerHatch("Top middle back, above the rear Gear Box", 1)
            .addInputHatch("HOG, next to a Gear Box", 1)
            .addInputHatch("Lubricant, next to a Gear Box", 1)
            .addInputHatch("Liquid Oxygen, optional, next to a Gear Box", 1)
            .toolTipFinisher();
        return tt;
    }

    @Override
    public RecipeMap<FuelBackend> getRecipeMap() {
        return RecipeMaps.extremeDieselFuels;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        if (side == aFacing) {
            if (aActive) return new ITexture[] { casingTexturePages[0][60], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_EXTREME_DIESEL_ENGINE_ACTIVE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_EXTREME_DIESEL_ENGINE_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { casingTexturePages[0][60], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_EXTREME_DIESEL_ENGINE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_EXTREME_DIESEL_ENGINE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { casingTexturePages[0][60] };
    }

    @Override
    public byte getCasingMeta() {
        return 0;
    }

    @Override
    public Block getIntakeBlock() {
        return GregTechAPI.sBlockCasings8;
    }

    @Override
    public byte getIntakeMeta() {
        return 4;
    }

    @Override
    public byte getCasingTextureIndex() {
        return 60;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEExtremeDieselEngineLegacy(this.mName);
    }

    @Override
    protected int getNominalOutput() {
        return 10900;
    }

    @Override
    protected Materials getBooster() {
        return Materials.LiquidOxygen;
    }

    @Override
    protected int getEfficiencyIncrease() {
        return 20;
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        return GTMod.proxy.mPollutionExtremeCombustionEnginePerSecond;
    }

    @Override
    public String[] getInfoData() {
        long storedEnergy = 0;
        long maxEnergy = 0;
        for (MTEHatchDynamo tHatch : validMTEList(mDynamoHatches)) {
            storedEnergy += tHatch.getBaseMetaTileEntity()
                .getStoredEU();
            maxEnergy += tHatch.getBaseMetaTileEntity()
                .getEUCapacity();
        }

        return new String[] {
            EnumChatFormatting.BLUE + StatCollector.translateToLocal("GT5U.infodata.extreme_diesel_engine")
                + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.multiblock.energy") + ": "
                + EnumChatFormatting.GREEN
                + formatNumber(storedEnergy)
                + EnumChatFormatting.RESET
                + " EU / "
                + EnumChatFormatting.YELLOW
                + formatNumber(maxEnergy)
                + EnumChatFormatting.RESET
                + " EU",
            getIdealStatus() == getRepairStatus()
                ? EnumChatFormatting.GREEN + StatCollector.translateToLocal("GT5U.turbine.maintenance.false")
                    + EnumChatFormatting.RESET
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.turbine.maintenance.true")
                    + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.engine.output") + ": "
                + EnumChatFormatting.RED
                + formatNumber((long) -mEUt * mEfficiency / 10000)
                + EnumChatFormatting.RESET
                + " EU/t",
            StatCollector.translateToLocal("GT5U.engine.consumption") + ": "
                + EnumChatFormatting.YELLOW
                + formatNumber(fuelConsumption)
                + EnumChatFormatting.RESET
                + " L/t",
            StatCollector.translateToLocal("GT5U.engine.value") + ": "
                + EnumChatFormatting.YELLOW
                + formatNumber(fuelValue)
                + EnumChatFormatting.RESET
                + " EU/L",
            StatCollector.translateToLocal("GT5U.turbine.fuel") + ": "
                + EnumChatFormatting.GOLD
                + formatNumber(fuelRemaining)
                + EnumChatFormatting.RESET
                + " L",
            StatCollector.translateToLocal("GT5U.engine.efficiency") + ": "
                + EnumChatFormatting.YELLOW
                + (mEfficiency / 100F)
                + EnumChatFormatting.YELLOW
                + " %",
            StatCollector.translateToLocal("GT5U.multiblock.pollution") + ": "
                + EnumChatFormatting.GREEN
                + getAveragePollutionPercentage()
                + EnumChatFormatting.RESET
                + " %" };
    }
}
