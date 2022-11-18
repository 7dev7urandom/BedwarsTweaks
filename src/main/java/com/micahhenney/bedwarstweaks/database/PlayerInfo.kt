package com.micahhenney.bedwarstweaks.database

import java.util.UUID

data class PlayerInfo(
    val uuid: UUID,
    var quickBuy: String?,
    var hotbarManager: String?
)
