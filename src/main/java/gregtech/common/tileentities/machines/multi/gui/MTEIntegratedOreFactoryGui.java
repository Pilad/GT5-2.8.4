package gregtech.common.tileentities.machines.multi.gui;

import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.metatileentity.implementations.gui.MTEMultiBlockBaseGui;
import gregtech.api.modularui2.GTGuiTextures;

public class MTEIntegratedOreFactoryGui extends MTEMultiBlockBaseGui {

    public MTEIntegratedOreFactoryGui(MTEMultiBlockBase base) {
        super(base);
    }

    @Override
    protected void setMachineModeIcons() {
        machineModeIcons.add(GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_IOF_MACERATOR);
        machineModeIcons.add(GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_IOF_WASHER);
        machineModeIcons.add(GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_IOF_CENTRIFUGE);
        machineModeIcons.add(GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_IOF_SIFTER);
        machineModeIcons.add(GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_IOF_BATH);
        machineModeIcons.add(GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_IOF_THERMAL);
        machineModeIcons.add(GTGuiTextures.OVERLAY_BUTTON_MACHINEMODE_IOF_FORGE);
    }
}
