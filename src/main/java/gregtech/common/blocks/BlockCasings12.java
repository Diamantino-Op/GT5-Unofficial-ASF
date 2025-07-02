package gregtech.common.blocks;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.common.misc.GTStructureChannels;

/**
 * The casings are split into separate files because they are registered as regular blocks, and a regular block can have
 * 16 subtypes at most.
 */
public class BlockCasings12 extends BlockCasingsAbstract {

    public BlockCasings12() {
        super(ItemCasings.class, "gt.blockcasings12", MaterialCasings.INSTANCE, 16);

        register(3, ItemList.Nanite_Accelerator_Pipe_Coils, "Nanite Accelerator Pipe Coil");
        register(4, ItemList.Nanite_Accelerator_Pipe_Casings, "Nanite Accelerator Pipe Casing");
        register(5, ItemList.Nanite_Accelerator_Pipe_Director, "Nanite Accelerator Pipe Director");
        register(6, ItemList.Atomic_Resistant_Casings, "Atomic Resistant Casing");

        register(10, ItemList.CasingThaumium, "Alchemically Resistant Thaumium Casing");
        register(11, ItemList.CasingVoid, "Alchemically Inert Void Casing");
        register(12, ItemList.CasingIchorium, "Alchemically Immune Ichorium Casing");
        for (int i = 0; i < 3; i++) {
            GTStructureChannels.METAL_MACHINE_CASING.registerAsIndicator(new ItemStack(this, 1, i + 10), i + 1);
        }
    }

    @Override
    public int getTextureIndex(int aMeta) {
        return (16 << 7) | (aMeta + 80);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int ordinalSide, int aMeta) {
        return switch (aMeta) {
            case 3 -> Textures.BlockIcons.NANITE_ACCELERATOR_PIPE_COIL.getIcon();
            case 4 -> Textures.BlockIcons.NANITE_ACCELERATOR_PIPE_CASING.getIcon();
            case 5 -> Textures.BlockIcons.NANITE_ACCELERATOR_PIPE_DIRECTOR.getIcon();
            case 6 -> Textures.BlockIcons.ATOMIC_RESISTANT_CASING.getIcon();
            case 10 -> Textures.BlockIcons.MACHINE_CASING_THAUMIUM.getIcon();
            case 11 -> Textures.BlockIcons.MACHINE_CASING_VOID.getIcon();
            case 12 -> Textures.BlockIcons.MACHINE_CASING_ICHORIUM.getIcon();
            default -> Textures.BlockIcons.MACHINE_CASING_ROBUST_TUNGSTENSTEEL.getIcon();
        };
    }
}
