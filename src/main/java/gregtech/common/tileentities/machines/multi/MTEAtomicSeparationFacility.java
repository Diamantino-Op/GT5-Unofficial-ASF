package gregtech.common.tileentities.machines.multi;

import bartworks.API.BorosilicateGlass;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Materials;
import gregtech.api.enums.MaterialsUEVplus;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchNanite;
import gregtech.api.metatileentity.implementations.MTEHatchSwarmConductivitySensor;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTStructureUtility;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import tectech.thing.casing.TTCasingsContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static gregtech.api.enums.GTValues.*;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GTStructureUtility.ofFrame;

public class MTEAtomicSeparationFacility extends MTEExtendedPowerMultiBlockBase<MTEAtomicSeparationFacility> implements ISurvivalConstructable {
    private HeatingCoilLevel coilLevel;
    private int coilTier = 0;
    private int frameTier = 0;
    private byte glassTier = 0;

    private NaniteMode naniteMode = NaniteMode.Transport;
    private String naniteMaterial = "None";
    private int currentTransportNanites = 0;
    private int currentLatticeNanites = 0;

    private long baseCapacity = 2_000_000_000L;

    private HashMap<String, Long> fluidInputMap = new HashMap<>();
    private HashMap<String, Long> fluidOutputMap = new HashMap<>();
    private HashMap<Integer, Long> itemInputMap = new HashMap<>();
    private HashMap<Integer, Long> itemOutputMap = new HashMap<>();

    private MTEHatchNanite naniteHatch;
    private MTEHatchSwarmConductivitySensor swarmConductivitySensor;
    private int currentSwarmConductivity = 0;

    private static final String mainStructurePiece = "main";
    private static final IStructureDefinition<MTEAtomicSeparationFacility> structureDefinition = StructureDefinition
        .<MTEAtomicSeparationFacility>builder()
        .addShape(
            mainStructurePiece,
            transpose(new String[][]{
                // spotless:off
                    {"                                               ","                                               ","                                               ","                      FFF                      ","                    FFFFFFF                    ","                   FFFFFFFFF                   ","                   FFFFFFFFF                   ","                  FFFFFFFFFFF                  ","                  FFFFFQFFFFF                  ","                  FFFFFFFFFFF                  ","                   FFFFFFFFF                   ","                   FFFFFFFFF                   ","                    FFFFFFF                    ","                      FFF                      ","                                               ","                                               ","                                               "},
                    {"                                               ","                                               ","                      GGG                      ","                    GG   GG                    ","                   G-------G                   ","                  G---------G                  ","                  G---------G                  ","                 G-----------G                 ","                 G-----------G                 ","                 G-----------G                 ","                  G---------G                  ","                  G---------G                  ","                   G-------G                   ","                    GG---GG                    ","                      GGG                      ","                                               ","                                               "},
                    {"                     FFFFF                     ","                   FFFFFFFFF                   ","                  FFFFFFFFFFF                  ","                 FFFFFFFFFFFFF                 ","                FFFFFFFFFFFFFFF                ","                FFFFFFFFFFFFFFF                ","               FFFFFFFFFFFFFFFFF               ","               FFFFFFFFFFFFFFFFF               ","               FFFFFFFFFFFFFFFFF               ","               FFFFFFFFFFFFFFFFF               ","               FFFFFFFFFFFFFFFFF               ","                FFFFFFFFFFFFFFF                ","                FFFFFFFFFFFFFFF                ","                 FFFFFFFFFFFFF                 ","                  FFFFFFFFFFF                  ","                   FFFFFFFFF                   ","                     FFFFF                     "},
                    {"                                               ","                     GGGGG                     ","                   GGDDDDDGG                   ","                  GDDNNNNNDDG                  ","                 GDNN-----NNDG                 ","                 GDN-------NDG                 ","  GFFFFFG       GDN---NNN---NDG       GFFFFFG  ","  GFFFFFG       GDN--N-D-N--NDG       GFFFFFG  ","  GGGGGGG       GDN--N-B-N--NDG       GGGGGGG  ","  GFFFFFG       GDN--N-D-N--NDG       GFFFFFG  ","  GFFFFFG       GDN---NNN---NDG       GFFFFFG  ","                 GDN-------NDG                 ","                 GDNN-----NNDG                 ","                  GDDNNNNNDDG                  ","                   GGDDDDDGG                   ","                     GGGGG                     ","                                               "},
                    {"                                               ","                                               ","                    LFFFFFL                    ","                  JFFGGGGGFFJ                  ","                  FGG-----GGF                  ","  GGGGGGG        LFG-------GFL        GGGGGGG  "," FBBBBBBBF       FG---NNN---GF       FBBBBBBBF "," F-J-----F       FG--ND--N--GF       F-----J-F "," G-------G       FG--N-B-N--GF       G-------G "," F-J-----F       FG--N--DN--GF       F-----J-F "," FBBBBBBBF       FG---NNN---GF       FBBBBBBBF ","  GGGGGGG        LFG-------GFL        GGGGGGG  ","                  FGG-----GGF                  ","                  JFFGGGGGFFJ                  ","                    LFFFFFL                    ","                                               ","                                               "},
                    {"                                               ","                                               ","                    L     L                    ","                  J  FFFFF  J                  ","  GFFFFFG          FF-----FF          GFFFFFG  "," FBBBBBBBF       L F-------F L       FBBBBBBBF "," F-------F        F---NNN---F        F-------F ","G--J-----DGEEEEEEGF--N---N--FGEEEEEEGD-----J--G","G--------DGHHHHHHGF--NDBDN--FGHHHHHHGD--------G","G--J-----DGEEEEEEGF--N---N--FGEEEEEEGD-----J--G"," F-------F        F---NNN---F        F-------F "," FBBBBBBBF       L F-------F L       FBBBBBBBF ","  GFFFFFG          FF-----FF          GFFFFFG  ","                  J  FFFFF  J                  ","                    L     L                    ","                                               ","                                               "},
                    {"                                               ","                                               ","                    L     L                    ","                  J  FAAAF  J                  ","  GFRRRFG          FF-----FF          GFGGGFG  "," F-J-----F       L F-------F L       F-----J-F ","G--J-----DGEEEEEEGF---NNN---FGEEEEEEGD-----J--G","A--KMMMMMCDDDDDDDDC--N--DN--CDDDDDDDDCMMMMMK--A","A--KNNNNNCCCCCCCCCC--N-B-N--CCCCCCCCCCNNNNNK--A","A--KMMMMMCDDDDDDDDC--ND--N--CDDDDDDDDCMMMMMK--A","G--J-----DGEEEEEEGF---NNN---FGEEEEEEGD-----J--G"," F-J-----F       L F-------F L       F-----J-F ","  GFSSSFG          FF-----FF          GFGGGFG  ","                  J  FAAAF  J                  ","                    L     L                    ","                                               ","                                               "},
                    {"                                               ","                                               ","                    L     L                    ","                  J  FAAAF  J                  ","  GGGGGGG          FF-----FF          GGGOGGG  "," G-------G       L F-------F L       G-------G ","G--------DGHHHHHHGF---NNN---FGHHHHHHGD--------G","A--KNNNNNCCCCCCCCCC--N-D-N--CCCCCCCCCCNNNNNK--A","A--------------------N-B-N--------------------A","A--KNNNNNCCCCCCCCCC--N-D-N--CCCCCCCCCCNNNNNK--A","G--------DGHHHHHHGF---NNN---FGHHHHHHGD--------G"," G-------G       L F-------F L       G-------G ","  GGGGGGG          FF-----FF          GGGPGGG  ","                  J  FAAAF  J                  ","                    L     L                    ","                                               ","                                               "},
                    {"                                               ","                                               ","                    L     L                    ","                  J  FAAAF  J                  ","  GFTTTFG          FF-----FF          GFGGGFG  "," F-J-----F       L F-------F L       F-----J-F ","G--J-----DGEEEEEEGF---NNN---FGEEEEEEGD-----J--G","A--KMMMMMCDDDDDDDDC--ND--N--CDDDDDDDDCMMMMMK--A","A--KNNNNNCCCCCCCCCC--N-B-N--CCCCCCCCCCNNNNNK--A","A--KMMMMMCDDDDDDDDC--N--DN--CDDDDDDDDCMMMMMK--A","G--J-----DGEEEEEEGF---NNN---FGEEEEEEGD-----J--G"," F-J-----F       L F-------F L       F-----J-F ","  GFUUUFG          FF-----FF          GFGGGFG  ","                  J  FAAAF  J                  ","                    L     L                    ","                                               ","                                               "},
                    {"                                               ","                                               ","                    L     L                    ","                  J  FFFFF  J                  ","  GFFFFFG          FF-----FF          GFFFFFG  "," FBBBBBBBF       L F-------F L       FBBBBBBBF "," F-------F        F---NNN---F        F-------F ","G--J-----DGEEEEEEGF--N---N--FGEEEEEEGD-----J--G","G--------DGHHHHHHGF--NDBDN--FGHHHHHHGD--------G","G--J-----DGEEEEEEGF--N---N--FGEEEEEEGD-----J--G"," F-------F        F---NNN---F        F-------F "," FBBBBBBBF       L F-------F L       FBBBBBBBF ","  GFFFFFG          FF-----FF          GFFFFFG  ","                  J  FFFFF  J                  ","                    L     L                    ","                                               ","                                               "},
                    {"                                               ","                                               ","                    LFF~FFL                    ","                  JFFGGGGGFFJ                  ","                  FGG-----GGF                  "," IGGGGGGGI       LFG-------GFL       IGGGGGGGI "," FBBBBBBBF       FG---NNN---GF       FBBBBBBBF "," F-J-----F       FG--N--DN--GF       F-----J-F "," G-------G       FG--N-B-N--GF       G-------G "," F-J-----F       FG--ND--N--GF       F-----J-F "," FBBBBBBBF       FG---NNN---GF       FBBBBBBBF "," IGGGGGGGI       LFG-------GFL       IGGGGGGGI ","                  FGG-----GGF                  ","                  JFFGGGGGFFJ                  ","                    LFFVFFL                    ","                                               ","                                               "},
                    {"                                               ","                     GGGGG                     ","                   GGDDDDDGG                   ","                  GDDNNNNNDDG                  ","                 GDNN-----NNDG                 "," I       I       GDN-------NDG       I       I ","  GFFFFFG       GDN---NNN---NDG       GFFFFFG  ","  GFFFFFG       GDN--N-D-N--NDG       GFFFFFG  ","  GGGGGGG       GDN--N-B-N--NDG       GGGGGGG  ","  GFFFFFG       GDN--N-D-N--NDG       GFFFFFG  ","  GFFFFFG       GDN---NNN---NDG       GFFFFFG  "," I       I       GDN-------NDG       I       I ","                 GDNN-----NNDG                 ","                  GDDNNNNNDDG                  ","                   GGDDDDDGG                   ","                     GGGGG                     ","                                               "},
                    {"                     WWWWW                     ","                   WWWWWWWWW                   ","                  WWWWWWWWWWW                  ","                 WWWWWWWWWWWWW                 ","                WWWWWWWWWWWWWWW                "," I       I      WWWWWWWWWWWWWWW      I       I ","               WWWWWWWWWWWWWWWWW               ","               WWWWWWWWWWWWWWWWW               ","               WWWWWWWWWWWWWWWWW               ","               WWWWWWWWWWWWWWWWW               ","               WWWWWWWWWWWWWWWWW               "," I       I      WWWWWWWWWWWWWWW      I       I ","                WWWWWWWWWWWWWWW                ","                 WWWWWWWWWWWWW                 ","                  WWWWWWWWWWW                  ","                   WWWWWWWWW                   ","                     WWWWW                     "}
                // spotless:on
            }))
        .addElement('A', withChannel("glass", BorosilicateGlass.ofBoroGlass((byte) 0, (byte) 1, Byte.MAX_VALUE, (te, t) -> te.glassTier = t, te -> te.glassTier))) // Borosilicate Glass (Tiered)
        .addElement('B', ofBlock(GregTechAPI.sBlockCasings1, 15)) // Superconducting Coils
        .addElement('C', ofBlock(GregTechAPI.sBlockCasings12, 3)) // Nanite Accelerator Pipe Coils (New)
        .addElement('D', withChannel("coils", GTStructureUtility.ofCoil(MTEAtomicSeparationFacility::setCoilLevel, MTEAtomicSeparationFacility::getCoilLevel))) // Coils (Tiered)
        .addElement('E', ofBlock(GregTechAPI.sBlockCasings12, 4)) // Nanite Accelerator Pipe Casings (New)
        .addElement('F', ofBlock(GregTechAPI.sBlockCasings12, 6)) // Atomic Resistant Casings (New)
        .addElement('G', lazy(() -> ofBlock(TTCasingsContainer.sBlockCasingsTT, 0))) // High Power Casings
        .addElement('H', ofBlock(GregTechAPI.sBlockCasings12, 5)) // Nanite Accelerator Pipe Director (New)
        .addElement('I', ofFrame(Materials.Bedrockium)) // Bedrockium Frame
        .addElement('J', ofFrame(Materials.Infinity)) // Infinity Frame
        .addElement('K', ofFrame(Materials.StellarAlloy)) // Stellar Alloy Frame
        .addElement('L', ofFrame(Materials.Trinium)) // Trinium Frame
        .addElement('M', withChannel("frame", ofBlocksTiered(
            MTEAtomicSeparationFacility::getFrameTierFromMeta,
            ImmutableList.of(
                Pair.of(GregTechAPI.sBlockFrames, Materials.Longasssuperconductornameforuvwire.mMetaItemSubID),
                Pair.of(GregTechAPI.sBlockFrames, Materials.Longasssuperconductornameforuhvwire.mMetaItemSubID),
                Pair.of(GregTechAPI.sBlockFrames, Materials.SuperconductorUEVBase.mMetaItemSubID),
                Pair.of(GregTechAPI.sBlockFrames, Materials.SuperconductorUIVBase.mMetaItemSubID),
                Pair.of(GregTechAPI.sBlockFrames, Materials.SuperconductorUMVBase.mMetaItemSubID),
                Pair.of(GregTechAPI.sBlockFrames, MaterialsUEVplus.MagnetohydrodynamicallyConstrainedStarMatter.mMetaItemSubID)),
            0, (te, tier) -> te.frameTier = tier , te -> te.frameTier))) // Superconductor Frames (Tiered)
        .addElement('N', ofBlock(GregTechAPI.sBlockGlass1, 1)) // Electron-Permeable Neutronium Coated Glass
        .addElement('O', SpecialHatchElement.NaniteBus.newAny(6, 6)) // Nanite Input Hatch
        .addElement('P', SpecialHatchElement.SwarmConductivitySensorHatch.newAny(6, 7)) // Swarm Conductivity Sensor - Energy.or(ExoticEnergy)
        .addElement('Q', Muffler.newAny(6, 1)) // Muffler (Tiered)
        .addElement('R', InputHatch.newAnyOrCasing(6, 2, GregTechAPI.sBlockCasings12, 6)) // Input Hatch
        .addElement('S', OutputHatch.newAnyOrCasing(6, 3, GregTechAPI.sBlockCasings12, 6)) // Output Hatch
        .addElement('T', InputBus.newAnyOrCasing(6, 4, GregTechAPI.sBlockCasings12, 6)) // Input Bus
        .addElement('U', OutputBus.newAnyOrCasing(6, 5, GregTechAPI.sBlockCasings12, 6)) // Output Bus
        .addElement('V', Maintenance.newAny(6, 8)) // Maintenance Hatch
        .addElement('W', Energy.or(ExoticEnergy).newAnyOrCasing(6, 9, GregTechAPI.sBlockCasings12, 6)) // Energy Hatch
        .build();

    public MTEAtomicSeparationFacility(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEAtomicSeparationFacility(String aName) {
        super(aName);
    }

    public HeatingCoilLevel getCoilLevel() {
        return coilLevel;
    }

    public long getActualCapacity() {
        return baseCapacity * frameTier;
    }

    public void setCoilLevel(HeatingCoilLevel aCoilLevel) {
        coilLevel = aCoilLevel;
        coilTier = aCoilLevel.getTier();
    }

    private static Integer getFrameTierFromMeta(Block block, Integer metaID) {
        if (block == GregTechAPI.sBlockFrames) {
            if (metaID == Materials.Longasssuperconductornameforuvwire.mMetaItemSubID) return 1;
            if (metaID == Materials.Longasssuperconductornameforuhvwire.mMetaItemSubID) return 2;
            if (metaID == Materials.SuperconductorUEVBase.mMetaItemSubID) return 3;
            if (metaID == Materials.SuperconductorUIVBase.mMetaItemSubID) return 4;
            if (metaID == Materials.SuperconductorUMVBase.mMetaItemSubID) return 5;
            if (metaID == MaterialsUEVplus.MagnetohydrodynamicallyConstrainedStarMatter.mMetaItemSubID) return 6;
        }
        return 0;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(mainStructurePiece, stackSize, hintsOnly, 23, 10, 2);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(mainStructurePiece, stackSize, 23, 10, 2, elementBudget, env, false, true);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        setCoilLevel(HeatingCoilLevel.None);
        return checkPiece(mainStructurePiece, 23, 10, 2);
    }

    @Override
    public IStructureDefinition<MTEAtomicSeparationFacility> getStructureDefinition() {
        return structureDefinition;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return super.createProcessingLogic();
    }

    @Override
    public @NotNull CheckRecipeResult checkProcessing() {
        return super.checkProcessing();
    }

    @Override
    protected @NotNull CheckRecipeResult postCheckRecipe(@NotNull CheckRecipeResult result, @NotNull ProcessingLogic processingLogic) {
        return super.postCheckRecipe(result, processingLogic);
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {


        return super.onRunningTick(aStack);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (!aBaseMetaTileEntity.isServerSide()) return;

        if (aTick % 20 == 0) {
            //this.currentSwarmConductivity =
        }
    }

    public int getMaxParallelRecipes() {
        return (int) Math.floor((Math.log(this.getMaxInputAmps()) / Math.log(2)) * getNaniteTier() * this.getMaxInputVoltage());
    }

    private int getNaniteTier() {
        return switch (naniteMaterial) {
            case "Crude Infinity Superconductive":
                yield 1;

            case "Spatially Reinforced Superconductive":
                yield 2;

            case "Pauli-Degenerate Ultraconductive":
                yield 3;

            case "Directed Monopole Ultraconductive":
                yield 4;

            default:
                yield 0;
        };
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.electrolyzerRecipes;
    }

    @Override
    public boolean supportsVoidProtection() {
        return true;
    }

    //TODO: Finish
    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        tt.addMachineType("Electrolyzer, ASF")
            .beginStructureBlock(47, 13, 17, false)
            .addController("Front Center")
            .addCasingInfoExactly("High Power Casing", 450, false)
            .addCasingInfoMin("Atomic Resistant Casing", 719, false)
            .addCasingInfoExactly("Nanite Accelerator Pipe Casing", 96, false)
            .addCasingInfoExactly("Nanite Accelerator Pipe Coil", 96, false)
            .addCasingInfoExactly("Nanite Accelerator Pipe Director", 48, false)
            .addCasingInfoExactly("Electron-Permeable Neutronium Coated Glass", 212, false)
            .addCasingInfoExactly("Superconducting Coil", 121, false)
            .addCasingInfoExactly("Coil", 178, true)
            .addCasingInfoExactly("Borosilicate Glass", 36, true)
            .addCasingInfoExactly("Superconductor Frame", 40, true)
            .addCasingInfoExactly("Bedrockium Frame", 24, false)
            .addCasingInfoExactly("Infinity Frame", 60, false)
            .addCasingInfoExactly("Stellar Alloy Frame", 16, false)
            .addCasingInfoExactly("Trinium Frame", 56, false)
            .addOtherStructurePart("Nanite Bus", "Left Center Front", 6)
            .addOtherStructurePart("Swarm Conductivity Sensor Hatch", "Left Center Back", 7)
            .addInputBus("Right Bottom Front", 4)
            .addInputHatch("Right Top Front", 2)
            .addOutputBus("Right Bottom Back", 5)
            .addOutputHatch("Right Top Back", 3)
            .addEnergyHatch("Any Bottom Atomic Resistant Casing", 9)
            .addMaintenanceHatch("Back Middle", 8)
            .addMufflerHatch("Top Center", 1)
            .addPollutionAmount(100)
            .addTecTechHatchInfo()
            .toolTipFinisher(AuthorDiamondMaster);

        return tt;
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        return 100;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setString("naniteMode", naniteMode.name());
        aNBT.setString("naniteMaterial", naniteMaterial);
        aNBT.setInteger("currentTransportNanites", currentTransportNanites);
        aNBT.setInteger("currentLatticeNanites", currentLatticeNanites);
        aNBT.setInteger("frameTier", frameTier);
        aNBT.setInteger("coilTier", coilTier);
        aNBT.setByte("glassTier", glassTier);
        aNBT.setInteger("currentSwarmConductivity", currentSwarmConductivity);

        NBTTagList outputFluidInventory = new NBTTagList();

        fluidOutputMap.forEach((fluidName, amount) -> {
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setString("fluidName", fluidName);
            fluidTag.setLong("amount", amount);
            outputFluidInventory.appendTag(fluidTag);
        });

        aNBT.setTag("fluidOutputMap", outputFluidInventory);

        NBTTagList outputItemInventory = new NBTTagList();

        itemOutputMap.forEach((itemId, amount) -> {
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setInteger("itemId", itemId);
            itemTag.setLong("amount", amount);
            outputItemInventory.appendTag(itemTag);
        });

        aNBT.setTag("itemOutputMap", outputItemInventory);

        NBTTagList inputFluidInventory = new NBTTagList();

        fluidInputMap.forEach((fluidName, amount) -> {
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setString("fluidName", fluidName);
            fluidTag.setLong("amount", amount);
            inputFluidInventory.appendTag(fluidTag);
        });

        aNBT.setTag("fluidInputMap", inputFluidInventory);

        NBTTagList inputItemInventory = new NBTTagList();

        itemInputMap.forEach((itemId, amount) -> {
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setInteger("itemId", itemId);
            itemTag.setLong("amount", amount);
            inputItemInventory.appendTag(itemTag);
        });

        aNBT.setTag("itemInputMap", inputItemInventory);

        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("naniteMode")) naniteMode = NaniteMode.valueOf(aNBT.getString("naniteMode"));
        if (aNBT.hasKey("naniteMaterial")) naniteMaterial = aNBT.getString("naniteMaterial");
        if (aNBT.hasKey("currentTransportNanites")) currentTransportNanites = aNBT.getInteger("currentTransportNanites");
        if (aNBT.hasKey("currentLatticeNanites")) currentLatticeNanites = aNBT.getInteger("currentLatticeNanites");
        if (aNBT.hasKey("frameTier")) frameTier = aNBT.getInteger("frameTier");
        if (aNBT.hasKey("coilTier")) coilTier = aNBT.getInteger("coilTier");
        if (aNBT.hasKey("glassTier")) glassTier = aNBT.getByte("glassTier");
        if (aNBT.hasKey("currentSwarmConductivity")) currentSwarmConductivity = aNBT.getInteger("currentSwarmConductivity");

        if (aNBT.hasKey("fluidOutputMap")) {
            NBTTagList outputFluidInventory = aNBT.getTagList("fluidOutputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < outputFluidInventory.tagCount(); i++) {
                NBTTagCompound fluidTag = outputFluidInventory.getCompoundTagAt(i);
                String fluidName = fluidTag.getString("fluidName");
                long amount = fluidTag.getLong("amount");
                fluidOutputMap.put(fluidName, amount);
            }
        }

        if (aNBT.hasKey("itemOutputMap")) {
            NBTTagList outputItemInventory = aNBT.getTagList("itemOutputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < outputItemInventory.tagCount(); i++) {
                NBTTagCompound itemTag = outputItemInventory.getCompoundTagAt(i);
                int itemId = itemTag.getInteger("itemId");
                long amount = itemTag.getLong("amount");
                itemOutputMap.put(itemId, amount);
            }
        }

        if (aNBT.hasKey("fluidInputMap")) {
            NBTTagList inputFluidInventory = aNBT.getTagList("fluidInputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < inputFluidInventory.tagCount(); i++) {
                NBTTagCompound fluidTag = inputFluidInventory.getCompoundTagAt(i);
                String fluidName = fluidTag.getString("fluidName");
                long amount = fluidTag.getLong("amount");
                fluidInputMap.put(fluidName, amount);
            }
        }

        if (aNBT.hasKey("itemInputMap")) {
            NBTTagList inputItemInventory = aNBT.getTagList("itemInputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < inputItemInventory.tagCount(); i++) {
                NBTTagCompound itemTag = inputItemInventory.getCompoundTagAt(i);
                int itemId = itemTag.getInteger("itemId");
                long amount = itemTag.getLong("amount");
                itemInputMap.put(itemId, amount);
            }
        }

        super.loadNBTData(aNBT);
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y, int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);

        tag.setString("naniteMode", naniteMode.name());
        tag.setString("naniteMaterial", naniteMaterial);
        tag.setInteger("currentTransportNanites", currentTransportNanites);
        tag.setInteger("currentLatticeNanites", currentLatticeNanites);
        tag.setInteger("currentSwarmConductivity", currentSwarmConductivity);

        NBTTagList outputFluidInventory = new NBTTagList();

        fluidOutputMap.forEach((fluidName, amount) -> {
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setString("fluidName", fluidName);
            fluidTag.setLong("amount", amount);
            outputFluidInventory.appendTag(fluidTag);
        });

        tag.setTag("fluidOutputMap", outputFluidInventory);

        NBTTagList outputItemInventory = new NBTTagList();

        itemOutputMap.forEach((itemId, amount) -> {
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setInteger("itemId", itemId);
            itemTag.setLong("amount", amount);
            outputItemInventory.appendTag(itemTag);
        });

        tag.setTag("itemOutputMap", outputItemInventory);

        NBTTagList inputFluidInventory = new NBTTagList();

        fluidInputMap.forEach((fluidName, amount) -> {
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setString("fluidName", fluidName);
            fluidTag.setLong("amount", amount);
            inputFluidInventory.appendTag(fluidTag);
        });

        tag.setTag("fluidInputMap", inputFluidInventory);

        NBTTagList inputItemInventory = new NBTTagList();

        itemInputMap.forEach((itemId, amount) -> {
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setInteger("itemId", itemId);
            itemTag.setLong("amount", amount);
            inputItemInventory.appendTag(itemTag);
        });

        tag.setTag("itemInputMap", inputItemInventory);
    }

    //TODO: Translations
    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currentTip, accessor, config);

        final NBTTagCompound tag = accessor.getNBTData();

        if (tag.hasKey("naniteMode")) currentTip.add(
            EnumChatFormatting.GOLD
                + "Nanite Mode: "
                + EnumChatFormatting.RESET
                + tag.getString("naniteMode"));

        if (tag.hasKey("naniteMaterial")) currentTip.add(
            EnumChatFormatting.GOLD
                + "Nanite Material: "
                + EnumChatFormatting.RESET
                + tag.getString("naniteMaterial"));

        if (tag.hasKey("currentTransportNanites")) currentTip.add(
            EnumChatFormatting.GOLD
                + "Current Transport Nanites: "
                + EnumChatFormatting.RESET
                + tag.getInteger("currentTransportNanites"));

        if (tag.hasKey("currentLatticeNanites")) currentTip.add(
            EnumChatFormatting.GOLD
                + "Current Lattice Nanites: "
                + EnumChatFormatting.RESET
                + tag.getInteger("currentLatticeNanites"));

        if (tag.hasKey("currentSwarmConductivity")) currentTip.add(
            EnumChatFormatting.GOLD
                + "Current Swarm Conductivity: "
                + EnumChatFormatting.RESET
                + tag.getInteger("currentSwarmConductivity"));

        currentTip.add(EnumChatFormatting.GOLD + "Input Buffer: ");

        boolean hasInputs = false;

        if (tag.hasKey("fluidInputMap")) {
            NBTTagList inputFluidInventory = tag.getTagList("fluidInputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < inputFluidInventory.tagCount(); i++) {
                NBTTagCompound fluidTag = inputFluidInventory.getCompoundTagAt(i);
                String fluidName = fluidTag.getString("fluidName");
                long amount = fluidTag.getLong("amount");

                String localisedName = new FluidStack(FluidRegistry.getFluid(fluidName), 1).getLocalizedName();

                currentTip.add(localisedName + ": " + amount + " mB");
            }

            if (inputFluidInventory.tagCount() > 0) {
                hasInputs = true;
            }
        }

        if (tag.hasKey("itemInputMap")) {
            NBTTagList inputItemInventory = tag.getTagList("itemInputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < inputItemInventory.tagCount(); i++) {
                NBTTagCompound itemTag = inputItemInventory.getCompoundTagAt(i);
                int itemId = itemTag.getInteger("itemId");
                long amount = itemTag.getLong("amount");

                String localisedName = new ItemStack(Item.getItemById(itemId), 1).getDisplayName();

                currentTip.add(localisedName + ": " + amount);
            }

            if (inputItemInventory.tagCount() > 0) {
                hasInputs = true;
            }
        }

        if (!hasInputs) {
            currentTip.add("Empty");
        }

        currentTip.add(EnumChatFormatting.GOLD + "Output Buffer: ");

        boolean hasOutputs = false;

        if (tag.hasKey("fluidOutputMap")) {
            NBTTagList outputFluidInventory = tag.getTagList("fluidOutputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < outputFluidInventory.tagCount(); i++) {
                NBTTagCompound fluidTag = outputFluidInventory.getCompoundTagAt(i);
                String fluidName = fluidTag.getString("fluidName");
                long amount = fluidTag.getLong("amount");

                String localisedName = new FluidStack(FluidRegistry.getFluid(fluidName), 1).getLocalizedName();

                currentTip.add(localisedName + ": " + amount + " mB");
            }

            if (outputFluidInventory.tagCount() > 0) {
                hasOutputs = true;
            }
        }

        if (tag.hasKey("itemOutputMap")) {
            NBTTagList outputItemInventory = tag.getTagList("itemOutputMap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < outputItemInventory.tagCount(); i++) {
                NBTTagCompound itemTag = outputItemInventory.getCompoundTagAt(i);
                int itemId = itemTag.getInteger("itemId");
                long amount = itemTag.getLong("amount");

                String localisedName = new ItemStack(Item.getItemById(itemId), 1).getDisplayName();

                currentTip.add(localisedName + ": " + amount);
            }

            if (outputItemInventory.tagCount() > 0) {
                hasInputs = true;
            }
        }

        if (!hasInputs) {
            currentTip.add("Empty");
        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEAtomicSeparationFacility(this.mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing, int colorIndex, boolean aActive, boolean redstoneLevel) {
        ITexture[] rTexture;

        if (side == aFacing) {
            if (aActive) {
                rTexture = new ITexture[] {
                    Textures.BlockIcons
                        .getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings12, 6)),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_ASF_ACTIVE)
                        .extFacing()
                        .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_ASF_ACTIVE_GLOW)
                        .extFacing()
                        .glow()
                        .build() };
            } else {
                rTexture = new ITexture[] {
                    Textures.BlockIcons
                        .getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings12, 6)),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_ASF)
                        .extFacing()
                        .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_ASF_GLOW)
                        .extFacing()
                        .glow()
                        .build() };
            }
        } else {
            rTexture = new ITexture[] { Textures.BlockIcons
                .getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings12, 6)) };
        }
        return rTexture;
    }

    public boolean addNaniteBusToMachineList(IGregTechTileEntity tileEntity, int baseCasingIndex) {
        if (tileEntity == null) return false;

        IMetaTileEntity metaTileEntity = tileEntity.getMetaTileEntity();

        if (metaTileEntity instanceof MTEHatchNanite naniteBus) {
            naniteBus.updateTexture(baseCasingIndex);
            this.naniteHatch = naniteBus;
            return true;
        } else if (metaTileEntity instanceof MTEHatchSwarmConductivitySensor swarmConductivitySensorHatch) {
            swarmConductivitySensorHatch.updateTexture(baseCasingIndex);
            this.swarmConductivitySensor = swarmConductivitySensorHatch;
            return true;
        }

        return false;
    }

    private enum NaniteMode {
        Transport,
        Lattice
    }

    private enum SpecialHatchElement implements IHatchElement<MTEAtomicSeparationFacility> {
        NaniteBus(MTEAtomicSeparationFacility::addNaniteBusToMachineList, MTEHatchNanite.class) {
            @Override
            public long count(MTEAtomicSeparationFacility te) {
                if (te.naniteHatch != null) return 1;

                return 0;
            }
        },

        SwarmConductivitySensorHatch(MTEAtomicSeparationFacility::addNaniteBusToMachineList, MTEHatchSwarmConductivitySensor.class) {
            @Override
            public long count(MTEAtomicSeparationFacility te) {
                if (te.swarmConductivitySensor != null) return 1;

                return 0;
            }
        };

        private final List<Class<? extends IMetaTileEntity>> mteClasses;
        private final IGTHatchAdder<MTEAtomicSeparationFacility> adder;

        @SafeVarargs
        SpecialHatchElement(IGTHatchAdder<MTEAtomicSeparationFacility> adder, Class<? extends IMetaTileEntity>... mteClasses) {
            this.mteClasses = Collections.unmodifiableList(Arrays.asList(mteClasses));
            this.adder = adder;
        }

        @Override
        public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
            return mteClasses;
        }

        public IGTHatchAdder<? super MTEAtomicSeparationFacility> adder() {
            return adder;
        }
    }
}
