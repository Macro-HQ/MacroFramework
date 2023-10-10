package dev.macrohq.macroframework.dsl

import net.minecraft.client.Minecraft

val mc get() = Minecraft.getMinecraft()
val player get() = mc.thePlayer
val world get() = mc.theWorld
val gameSettings get() = mc.gameSettings