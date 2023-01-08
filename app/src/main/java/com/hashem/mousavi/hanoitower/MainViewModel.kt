package com.hashem.mousavi.hanoitower

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val disks by mutableStateOf(listOf(DiskData(), DiskData(), DiskData(), DiskData()))
    private val _rodError = MutableSharedFlow<Int>()
    val rodError = _rodError.asSharedFlow()

    private var selectedDisk: DiskData? = null

    fun onRodSelected(rodIndex: Int) {
        val disksOnRod = disks.filter { it.rodIndex == rodIndex }
        if (disksOnRod.isNotEmpty()) {
            val minY = disksOnRod.minOf { it.actualPosition.y }//top most disk
            selectedDisk = disksOnRod.find { it.actualPosition.y == minY }
            selectedDisk?.goToTopOfRod(rodIndex)
        }
    }

    fun onDrag(rodIndex: Int) {
        selectedDisk?.goToTopOfRod(rodIndex)
    }

    fun onDropped(rodIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            selectedDisk?.let { diskData ->
                val countOfDisksOnRod = disks.count { it.rodIndex == rodIndex && it != diskData }
                if (countOfDisksOnRod == 0) {
                    diskData.drop(rodIndex, 0)
                } else {
                    //move validation
                    val disksOnRod = disks.filter { it.rodIndex == rodIndex && it != diskData }
                    val minY = disksOnRod.minOf { it.actualPosition.y }
                    val topMostDisk = disksOnRod.find { it.actualPosition.y == minY }
                    topMostDisk?.let { top ->
                        if (top.width > diskData.width) {
                            diskData.drop(rodIndex, countOfDisksOnRod)
                        } else {
                            _rodError.emit(rodIndex)
                            diskData.dropOnPreviousRod()
                        }
                    }
                }
                selectedDisk = null
            }
        }
    }

}