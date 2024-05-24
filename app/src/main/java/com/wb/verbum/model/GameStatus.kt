package com.wb.verbum.model

import java.io.Serializable

enum class GameStatus(val displayName: String) : Serializable {
    COMPLETED("FINALIZAT"), INCOMPLETE("NEFINALIZAT"),
    IN_PROGRESS("IN PROGRES")
}
