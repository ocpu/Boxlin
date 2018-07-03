package io.opencubes.boxlin.gui

import io.opencubes.boxlin.Boxlin
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory

class GuiFactoryBoxlin : IModGuiFactory {
  override fun hasConfigGui() = true

  override fun createConfigGui(parentScreen: GuiScreen) =
      Boxlin.Config.guiConfigScreen(parentScreen, "Boxlin configurations")

  override fun runtimeGuiCategories(): MutableSet<IModGuiFactory.RuntimeOptionCategoryElement> {
    throw Error("How?")
  }

  override fun initialize(minecraftInstance: Minecraft?) = Unit
}