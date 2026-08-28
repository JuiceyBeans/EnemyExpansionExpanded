package com.juiceybeans.eeexpanded.client.gui;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.KelpieEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EEExpanded.MOD_ID, value = Dist.CLIENT)
public class KelpieTongueWrapOverlay {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void renderGui(RenderGuiEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (!(player.getVehicle() instanceof KelpieEntity)) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        guiGraphics.blit(
                EEExpanded.id("textures/gui/kelpie_tongue_wrap.png"),
                0, 0,
                0, 0,
                screenWidth, screenHeight,
                screenWidth, screenHeight);
    }
}
