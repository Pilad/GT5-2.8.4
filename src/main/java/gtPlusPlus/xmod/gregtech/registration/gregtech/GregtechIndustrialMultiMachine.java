package gtPlusPlus.xmod.gregtech.registration.gregtech;

import static gregtech.api.enums.MetaTileEntityIDs.Industrial_MultiMachine;

import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;
import gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.processing.MTEIndustrialMultiMachine;

public class GregtechIndustrialMultiMachine {

    public static void run() {
        run1();
    }

    private static void run1() {
        GregtechItemList.Industrial_MultiMachine.set(
            new MTEIndustrialMultiMachine(
                Industrial_MultiMachine.ID,
                "industrialmultimachine.controller.tier.single",
                "Large Processing Factory").getStackForm(1L));
    }
}
