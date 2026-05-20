package com.rptsd.app.services

import com.rptsd.app.domain.model.RideData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcceptanceQueue @Inject constructor() {

    private val _queue = MutableSharedFlow<RideData>(replay = 0, extraBufferCapacity = 8)
    val queue: SharedFlow<RideData> = _queue.asSharedFlow()

    suspend fun queueAccept(ride: RideData) {
        _queue.emit(ride)
    }
}
