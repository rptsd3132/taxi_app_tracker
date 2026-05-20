package com.rptsd.app.domain.model

sealed class Decision {
    object Accept : Decision()
    data class Skip(val reason: String) : Decision()
}
