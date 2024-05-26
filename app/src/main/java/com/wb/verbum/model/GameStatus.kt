package com.wb.verbum.model

import java.io.Serializable

enum class GameStatus(val displayName: String) : Serializable {
    COMPLETED("COMPLETAT"), INCOMPLETE("INCOMPLET"),
    IN_PROGRESS("IN PROGRES")
}
