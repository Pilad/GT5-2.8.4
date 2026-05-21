package gregtech.common.tileentities.machines.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_GLOW;
import static gregtech.api.util.GTStructureUtility.activeCoils;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofCoil;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.casing.Casings;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.OverclockCalculator;
import gregtech.common.misc.GTStructureChannels;

public class MTEIndustrialArcFurnace extends MTEExtendedPowerMultiBlockBase<MTEIndustrialArcFurnace>
    implements ISurvivalConstructable {

    private int mCasing = 0;

    private HeatingCoilLevel coilLevel = HeatingCoilLevel.None;
    private int heatingCapacity = 0;

    private static final int OFFSET_H = 6;
    private static final int OFFSET_V = 7;
    private static final int OFFSET_D = 1;

    public MTEIndustrialArcFurnace(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected MTEIndustrialArcFurnace(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEIndustrialArcFurnace(this.mName);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Arc Furnace, IAF")
            .addInfo("Speed and parallel depend on voltage tier")
            .addInfo("Coil tier affects heat capacity, overclocks and EU discounts")
            .addController("Front center")
            .beginStructureBlock(17, 11, 19, false)
            .addCasingInfoMin("Solid Steel Machine Casing", 10, false)
            .addInputBus("Any Casing", 1)
            .addOutputBus("Any Casing", 1)
            .addInputHatch("Any Casing", 1)
            .addOutputHatch("Any Casing", 1)
            .addEnergyHatch("Any Casing", 1)
            .addMultiAmpHatchInfo()
            .addMaintenanceHatch("Any Casing", 1)
            .toolTipFinisher();
        return tt;
    }

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<MTEIndustrialArcFurnace> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEIndustrialArcFurnace>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(
                new String[][] { // spotless:off
                            {"                 ","                 ","                 ","                 ","     DDD         ","    D   D        ","    D   D        ","    D   D        ","     DDD         ","     D D         ","     D D         ","     D D         ","     D D         ","     D D         ","     D D         ","     D D         ","     D D         ","     DDD         ","                 "},
                            {"                 ","                 ","                 ","                 ","                 ","     IDI         ","     D D         ","     DID         ","      D          ","      D          ","      D          ","      D          ","      D          ","      D          ","      D          ","    A D A        ","    AD DA        ","                 ","     D D         "},
                            {"                 ","                 ","                 ","                 ","                 ","     I I         ","                 ","      I          ","                 ","                 ","                 ","                 ","              E  ","             EDE ","              E  ","    A   A        ","    DD DD        ","    A   A        ","     DHD         "},
                            {"                 ","                 ","                 ","                 ","                 ","     I I         ","                 ","      I          ","                 ","                 ","                 ","      F          ","      FF      E  ","       FFFFFFE E ","              E  ","    A   A        ","    DD DD        ","    A   A        ","     HHH         "},
                            {"                 ","                 ","                 ","      B          ","    BBBBB        ","    BI IB        ","   BB   BB       ","    BBIBB        ","    BBBBB        ","                 ","      F          ","      F          ","             DED ","             E E ","             DED ","    A   A        ","    DD DD        ","    A   A        ","     HHH         "},
                            {"                 ","    EEEEE        ","   EEBBBEE       ","  EBBB BBBE      "," EEB     BEE     "," EBB I I BBE     "," EB       BE     "," EBB  I  BBE     "," EEB     BEE     ","  EBBBBBBBE      ","   EEBFBEE       ","    EEEEE        ","             DED ","             E E ","             DED ","    A   A        ","    DD DD        ","    A   A        ","     HHH         "},
                            {"    D   D        ","   AABABAA       ","  A       A      "," A         A     ","DA         AD    "," B         B     "," C         C     "," B         B     ","DA         AD    "," A         A     ","  A       A      ","   AABCBAA       ","    D   D    DED ","             E E ","             DED ","    A   A        ","    DD DD        ","    A   A        ","     HHH         "},
                            {"    D   D        ","   GAB~BAG       ","  G       G      "," G         G     ","DA         AD    "," B         B     "," C         C     "," B         B     ","DA         AD    "," G         G     ","  G       G      ","   GABCBAG       ","    D   D    DED ","             E E ","             DED ","    A   A        ","    DD DD        ","    A   A        ","     HHH         "},
                            {"    D   D        ","   AABABAA       ","  A       A      "," A         A     ","DA         AD    "," B         B     "," C         C     "," B         B     ","DA         AD    "," A         A     ","  A       A      ","   AABCBAA       ","    D   D    DED ","             E E ","    A   A    DED ","    A   A        ","    DD DD        ","    A   A        ","     DHD         "},
                            {"    D   D        ","    A   A        ","   AAAAAAA       ","  AAAA AAAA      ","DAAAAC CAAAAD    ","  AACC CCAA      ","  CCC   CCC      ","  AACC CCAA      ","DAAAACCCAAAAD    ","  AAAACAAAA      ","   AAACAAA       ","    A   A    BBB ","    D   D   BEEEB","            BE EB","    A   A   BEEEB","    A   A    BBB ","    DD DD        ","    A   A        ","    ADDDA        "},
                            {"    D   D        ","  DDA   ADD      "," D   AAA   D     "," D    A    D     ","DA    A    AD    ","D     A     D    ","D    AAA    D    ","D     A     D    ","DA         AD    "," D         D     "," D         D     ","  DDA   ADD  BBB ","    D   D   B   B","            B   B","    A   A   B   B","    A   A    BBB ","    AD DA        ","    A   A        ","    ADDDA        "}
                        } // spotless:on
            ))
        .addElement(
            'A',
            buildHatchAdder(MTEIndustrialArcFurnace.class)
                .atLeast(InputBus, OutputBus, InputHatch, OutputHatch, Maintenance, Energy, ExoticEnergy)
                .casingIndex(Casings.SolidSteelMachineCasing.textureId)
                .dot(1)
                .buildAndChain(onElementPass(e -> e.mCasing++, Casings.SolidSteelMachineCasing.asElement())))
        .addElement('B', Casings.SteelPipeCasing.asElement())
        .addElement(
            'C',
            GTStructureChannels.HEATING_COIL
                .use(activeCoils(ofCoil(MTEIndustrialArcFurnace::setCoilLevel, MTEIndustrialArcFurnace::getCoilLevel))))
        .addElement('D', ofFrame(Materials.Steel))
        .addElement('E', Casings.BoltedNaquadahCasing.asElement())
        .addElement('F', Casings.InsulatedFluidPipeCasing.asElement())
        .addElement('G', Casings.HeatProofCokeOvenCasing.asElement())
        .addElement('H', Casings.BlastSmelterHeatContainmentCoil.asElement())
        .addElement('I', ofBlock(GregTechAPI.sBlockCasings13, 4))
        .build();

    @Override
    public IStructureDefinition<MTEIndustrialArcFurnace> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, OFFSET_H, OFFSET_V, OFFSET_D);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivalBuildPiece(
            STRUCTURE_PIECE_MAIN,
            stackSize,
            OFFSET_H,
            OFFSET_V,
            OFFSET_D,
            elementBudget,
            env,
            true);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mCasing = 0;
        this.heatingCapacity = 0;
        this.setCoilLevel(HeatingCoilLevel.None);

        if (!checkPiece(STRUCTURE_PIECE_MAIN, OFFSET_H, OFFSET_V, OFFSET_D)) return false;

        if (this.getCoilLevel() == HeatingCoilLevel.None) return false;

        this.heatingCapacity = (int) getCoilLevel().getHeat()
            + 100 * (GTUtility.getTier(this.getMaxInputVoltage()) - 2);

        return mCasing >= 10 && checkHatch();
    }

    public boolean checkHatch() {
        return !mMaintenanceHatches.isEmpty() && !mEnergyHatches.isEmpty();
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.arcFurnaceRecipes;
    }

    @Override
    public Collection<RecipeMap<?>> getAvailableRecipeMaps() {
        return Arrays.asList(RecipeMaps.arcFurnaceRecipes);
    }

    @Override
    public int getRecipeCatalystPriority() {
        return -1;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @Override
            protected OverclockCalculator createOverclockCalculator(@Nonnull GTRecipe recipe) {
                return super.createOverclockCalculator(recipe).setRecipeHeat(recipe.mSpecialValue)
                    .setMachineHeat(MTEIndustrialArcFurnace.this.heatingCapacity)
                    .setHeatOC(true)
                    .setHeatDiscount(true);
            }
        }.setSpeedBonus(1F / 3.5F)
            .setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    @Override
    public int getMaxParallelRecipes() {
        return 8 * GTUtility.getTier(this.getMaxInputVoltage());
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        final ITexture casingTexture = Casings.SolidSteelMachineCasing.getCasingTexture();
        if (side == facing) {
            if (active) return new ITexture[] { casingTexture, TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { casingTexture, TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { casingTexture };
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
    }

    public void setCoilLevel(HeatingCoilLevel aCoilLevel) {
        this.coilLevel = aCoilLevel;
    }

    public HeatingCoilLevel getCoilLevel() {
        return this.coilLevel;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.GT_MACHINES_ARC_FURNACE_LOOP;
    }
}
