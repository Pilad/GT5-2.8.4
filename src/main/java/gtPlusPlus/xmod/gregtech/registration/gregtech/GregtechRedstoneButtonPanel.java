package gtPlusPlus.xmod.gregtech.registration.gregtech;

import static gregtech.api.enums.MetaTileEntityIDs.RedstoneButtonPanel;

import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;
import gtPlusPlus.xmod.gregtech.common.tileentities.redstone.MTERedstoneButtonPanel;

public class GregtechRedstoneButtonPanel {

    public static void run() {
        GregtechItemList.RedstoneButtonPanel.set(new MTERedstoneButtonPanel(RedstoneButtonPanel.ID).getStackForm(1L));

    }
}
