package com.hashem.mousavi.hanoitower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hashem.mousavi.hanoitower.ui.theme.HanoiTowerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HanoiTowerTheme {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Hanoi(
                        disks = viewModel.disks,
                        rodError = viewModel.rodError,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(color = Color(0xFF64FFDA))
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        onRodSelected = { rodIndex ->
                            viewModel.onRodSelected(rodIndex)
                        },
                        onDropped = { rodIndex ->
                            viewModel.onDropped(rodIndex)
                        }
                    ) { rodIndex ->
                        viewModel.onDrag(rodIndex)
                    }
                }

            }
        }
    }
}
