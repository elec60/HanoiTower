package com.hashem.mousavi.hanoitower

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hashem.mousavi.hanoitower.ui.theme.colors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun Hanoi(
    disks: List<DiskData>,
    rodError: SharedFlow<Int>,
    modifier: Modifier,
    onRodSelected: (rodIndex: Int) -> Unit,
    onDropped: (rodIndex: Int) -> Unit,
    onDrag: (rodIndex: Int) -> Unit,
) {

    var wrongRodIndex by remember {
        mutableStateOf(-1)
    }

    LaunchedEffect(key1 = Unit) {
        rodError.collectLatest { rodIndex ->
            wrongRodIndex = rodIndex
            delay(400)
            wrongRodIndex = -1
        }
    }

    var rodWidth by remember {
        mutableStateOf(0)
    }

    var rodHeight by remember {
        mutableStateOf(0)
    }
    var width by remember {
        mutableStateOf(0)
    }

    var height by remember {
        mutableStateOf(0)
    }

    var biggestDiskWidth by remember {
        mutableStateOf(0.dp)
    }

    var rodY by remember {
        mutableStateOf(0)
    }

    var rod1X by remember {
        mutableStateOf(0)
    }

    var rod2X by remember {
        mutableStateOf(0)
    }

    var rod3X by remember {
        mutableStateOf(0)
    }

    val diskHeight = 10.dp

    var pressedOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    var selectedRodOnPress by remember {
        mutableStateOf(-1)
    }

    Layout(
        modifier = modifier
            .pointerInput(true) {
                detectTapGestures(
                    onPress = {
                        pressedOffset = it
                        val selectedRodIndex = getRodIndexByOffset(it, width)
                        selectedRodOnPress = selectedRodIndex
                        onRodSelected(selectedRodIndex)
                    },
                    onTap = {
                        val selectedRodIndex = getRodIndexByOffset(it, width)
                        if (selectedRodOnPress == selectedRodIndex) {
                            onDropped(selectedRodIndex)
                            pressedOffset = Offset.Zero
                            selectedRodOnPress = -1
                        }
                    }
                )
            }
            .pointerInput(true) {
                var index = -1
                detectDragGestures(
                    onDragEnd = {
                        onDropped(index)
                        pressedOffset = Offset.Zero
                        index = -1
                    },
                    onDragCancel = {
                        onDropped(index)
                        pressedOffset = Offset.Zero
                        index = -1
                    },
                    onDrag = { _, dragAmount ->
                        if (pressedOffset != Offset.Zero) {
                            pressedOffset += dragAmount
                            val selectedRodIndex = getRodIndexByOffset(pressedOffset, width)
                            index = selectedRodIndex
                            onDrag(selectedRodIndex)
                        }
                    }
                )
            },
        content = {
            repeat(3) {
                Rod(
                    width = rodWidth,
                    height = rodHeight,
                    color = if (it == wrongRodIndex) Color.Red else Color.Blue.copy(alpha = 0.5f)
                )
            }

            //index = 0 -> 1
            //index = 1 -> 0.8
            //index = 2 -> 0.64
            disks.forEachIndexed { index, diskData ->
                Disk(
                    modifier = Modifier
                        .width(biggestDiskWidth * (0.7f).pow(index))
                        .height(diskHeight),
                    color = colors[index],
                    diskData = diskData.apply {
                        this.rodY = rodY
                        this.rodX[0] = rod1X
                        this.rodX[1] = rod2X
                        this.rodX[2] = rod3X
                        this.rodWidth = rodWidth
                    }
                )
            }
        }
    ) { measurables, constraints ->

        width = constraints.maxWidth
        height = constraints.maxHeight

        rodHeight = height / 2
        rodWidth = 8.dp.roundToPx()

        rodY = height - rodHeight

        biggestDiskWidth = (2 * width / 6).toDp() - 20.dp

        val placeables =
            measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        layout(width, height) {
            var diskX: Int
            var diskY = height
            placeables.forEachIndexed { index, placeable ->
                //rods
                if (index < 3) {
                    //index = 0 -> 1xW/6
                    //index = 1 -> 3xW/6
                    //index = 2 -> 5xW/6
                    val x = width / 6 * (2 * index + 1) - rodWidth / 2
                    val y = rodY
                    when (index) {
                        0 -> {
                            rod1X = x
                        }
                        1 -> {
                            rod2X = x
                        }
                        2 -> {
                            rod3X = x
                        }
                    }
                    placeable.place(x = x, y = y)

                } else {//disks
                    diskX = width / 6 - placeable.width / 2
                    diskY += -diskHeight.roundToPx()
                    placeable.place(x = diskX, y = diskY)
                }
            }

        }
    }
}

private fun getRodIndexByOffset(
    it: Offset,
    width: Int,
) = if (it.x < width / 3) {
    0
} else if (it.x < 2 * width / 3) {
    1
} else {
    2
}


@Composable
fun Disk(
    modifier: Modifier,
    color: Color,
    diskData: DiskData,
) {
    val typeConverter = remember {
        object : TwoWayConverter<IntOffset, AnimationVector2D> {
            override val convertFromVector: (AnimationVector2D) -> IntOffset
                get() = { IntOffset(x = it.v1.toInt(), y = it.v2.toInt()) }
            override val convertToVector: (IntOffset) -> AnimationVector2D
                get() = { AnimationVector2D(it.x.toFloat(), it.y.toFloat()) }
        }
    }

    val animatable = remember {
        Animatable(initialValue = diskData.intOffset,
            typeConverter = typeConverter)
    }

    LaunchedEffect(diskData.intOffset) {
        animatable.animateTo(diskData.intOffset, animationSpec = tween(durationMillis = 300))
    }
    Box(
        modifier = modifier
            .onSizeChanged {
                diskData.width = it.width
                diskData.height = it.height
            }
            .onGloballyPositioned {
                diskData.x = it.positionInParent().x.roundToInt()
                diskData.y = it.positionInParent().y.roundToInt()
            }
            .offset {
                animatable.value
            }
            .background(
                color = color,
                shape = RoundedCornerShape(10f)
            )
    )
}

@Composable
fun Rod(
    width: Int,
    height: Int,
    color: Color,
) {
    Box(
        modifier = Modifier
            .width(width = with(LocalDensity.current) { width.toDp() })
            .height(height = with(LocalDensity.current) { height.toDp() })
            .background(color = color)
    )
}

@Preview
@Composable
fun DiskPreview() {
    Disk(
        modifier = Modifier
            .width(100.dp)
            .height(15.dp),
        color = Color.Blue,
        diskData = DiskData()
    )
}