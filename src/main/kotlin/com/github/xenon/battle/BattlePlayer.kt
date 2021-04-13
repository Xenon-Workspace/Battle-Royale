package com.github.xenon.battle

import com.github.monun.tap.ref.weaky
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

class BattlePlayer(val uniqueId: UUID, name: String) {
    var name: String = name
        get() {
            player?.let { field = it.name }
            return field
        }
    var player: Player? by weaky(null) { Bukkit.getPlayer(uniqueId) }
    val offlinePlayer: OfflinePlayer
        get() {
            return player ?: Bukkit.getOfflinePlayer(uniqueId)
        }
    var knockoutTicks = 0L

    var rank = -2
    val isOnline
        get() = player != null
}