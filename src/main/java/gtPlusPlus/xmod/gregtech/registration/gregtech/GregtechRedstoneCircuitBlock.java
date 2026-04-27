package gtPlusPlus.xmod.gregtech.registration.gregtech;

import static gregtech.api.enums.MetaTileEntityIDs.RedstoneCircuitBlock;

import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;
import gtPlusPlus.xmod.gregtech.common.tileentities.redstone.MTERedstoneCircuitBlock;

public class GregtechRedstoneCircuitBlock {

    public static void run() {
        GregtechItemList.RedstoneCircuitBlock
            .set(new MTERedstoneCircuitBlock(RedstoneCircuitBlock.ID).getStackForm(1L));
    }
}
