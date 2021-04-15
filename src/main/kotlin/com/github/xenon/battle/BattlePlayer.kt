package com.github.xenon.battle

import com.github.monun.tap.ref.weaky
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.util.Vector
import java.util.*

class BattlePlayer(val uniqueId: UUID, name: String) {
    var list = arrayListOf<Player>()
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
    fun onUpdate() {
        val player = player ?: return
        if(rank == -2) {
            list.forEachIndexed { index, p ->
                p.allowFlight = true
                p.isFlying = true
                Bukkit.dispatchCommand(player, "execute as ${player.name} at @s run tp ${p.name} ~ ~${4 * index} ~")
                if(player.gameMode == GameMode.SPECTATOR) {
                    list.remove(p)
                }
            }
        }
    }
}