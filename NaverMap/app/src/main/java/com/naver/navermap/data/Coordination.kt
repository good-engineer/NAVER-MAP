package com.naver.navermap.data

import com.naver.maps.geometry.LatLng
import java.lang.Error

data class Coordination(val result: RetroResult, val latLng: LatLng)
sealed class RetroResult {
    data class Success(val message: String) : RetroResult()
    data class NoInternetError(val cause: Throwable?) : RetroResult()
    data class NoResponseError(val cause: Throwable?) : RetroResult()
}