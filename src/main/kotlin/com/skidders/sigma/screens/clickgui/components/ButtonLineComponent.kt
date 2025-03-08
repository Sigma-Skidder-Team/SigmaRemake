package com.skidders.sigma.screens.clickgui.components

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack

import java.awt.*

class ButtonLineComponent {

    val text: String
    val x: Float
    val y: Float
    private val color: Color
    private val parent: Screen

    constructor(text: String, x: Float, y: Float, color: Color, parent: Screen) {
        this.text = text
        this.x = x
        this.y = y
        this.color = color
        this.parent = parent
    }


    fun draw(matrices: MatrixStack, mouseX: Int, mouseY: Int) {

    }

    fun click(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return true
    }

}
