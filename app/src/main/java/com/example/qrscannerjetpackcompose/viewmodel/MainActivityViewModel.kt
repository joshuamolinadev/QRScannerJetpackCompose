package com.example.qrscannerjetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {

    //flowTutorial
    val countDownTimerFlow = flow<Int> {
        val startingValue = 10
        var currentValue = startingValue
        emit(startingValue)
        while (currentValue > 0){
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }

    init {
        collectFlow()
    }

    private fun collectFlow(){
        viewModelScope.launch {
            countDownTimerFlow.collect{ time ->
                println("The current timeCount is: $time")
            }
        }
    }
}