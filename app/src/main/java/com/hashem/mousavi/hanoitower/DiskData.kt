package com.hashem.mousavi.hanoitower

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round

data class DiskData(
    var width: Int = 0,
    var height: Int = 0,
    var x: Int = 0,
    var y: Int = 0,
) {
    var rodWidth: Int = 0
    var rodIndex: Int = 0
    var rodY: Int = 0
    var rodX: IntArray = IntArray(3) { 0 }

    private var offset = mutableStateOf(Offset.Zero)

    private var previousPosition = IntOffset.Zero

    val intOffset: IntOffset
        get() = offset.value.round()

    val actualPosition: IntOffset
        get() = IntOffset(x = x + intOffset.x, y = y + intOffset.y)

    fun goToTopOfRod(rodIndex: Int) {
        val currX = actualPosition.x
        val currY = actualPosition.y
        val targetX = rodX[rodIndex] + (rodWidth - width) / 2
        val targetY = rodY
        val xOffset = targetX - currX
        val yOffset = targetY - currY

        if (xOffset == 0 && yOffset == 0){
            return
        }

        if (this.rodIndex == rodIndex) {
            println("djdjhdeljofjfe ${this.rodIndex} currY:$currY")
            previousPosition = IntOffset(x = currX, y = currY)
        }

        offset.value += Offset(x = xOffset.toFloat(), y = yOffset.toFloat())
    }

    fun drop(rodIndex: Int, countOfDisksOnRod: Int) {
        this.rodIndex = rodIndex
        offset.value += Offset(x = 0f, y = (rodY - height - countOfDisksOnRod * height).toFloat())
    }

    fun dropOnPreviousRod() {
        offset.value += Offset(x = previousPosition.x.toFloat() - actualPosition.x,
            y = previousPosition.y.toFloat() - actualPosition.y)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiskData) return false
        return this.actualPosition == other.actualPosition
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + x
        result = 31 * result + y
        return result
    }

}