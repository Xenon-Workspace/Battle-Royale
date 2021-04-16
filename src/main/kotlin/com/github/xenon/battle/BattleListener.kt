package com.github.xenon.battle

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.github.xenon.battle.plugin.BattlePlugin.Companion.instance
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import kotlin.random.Random

class BattleListener(val process: BattleProcess) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(null)

        val p = event.player
        val player = process.player(p)

        if(player == null) {
            p.gameMode = GameMode.SPECTATOR
        } else {
            player.player = p
            if(player.rank >= 0) {
                p.gameMode = GameMode.SPECTATOR
            }
        }
    }
    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val player = event.player; if(player.isOp) return
        val battle = process.player(player)
        if(battle == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, text("게임 참가자가 아닙니다."))
        }
    }
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(null)
    }
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.deathMessage(null)
        val player = event.entity
        if(process.player(player)?.rank!! >= 0 && process.player(player)?.rank!! <= -3) return
        if(player.killer is Player) {
            process.rank(process.player(player)!!, process.player(player.killer!!)!!)
            process.player(player.killer as Player)?.list?.add(player)
            process.player(player)?.list?.forEach {
                if(process.player(it)?.rank!! <= -1) {
                    process.player(player.killer as Player)?.list?.add(it)
                }
            }
            Bukkit.getScheduler().runTaskLater(instance, TeleportTo(player, player.killer as Player), 10L)
        } else {
            process.rank(process.player(player)!!)
            val random = Random.nextInt(process.survivePlayers.count())
            val p = process.survivePlayers[random]
            process.player(p.player!!)?.list?.add(player)
            process.player(player)?.list?.forEach {
                if(process.player(it)?.rank!! <= -1) {
                    process.player(p.player!!)?.list?.add(it)
                }
            }
            Bukkit.getScheduler().runTaskLater(instance, TeleportTo(player, p.player!!), 10L)
        }
    }
    @EventHandler
    fun onKick(event: PlayerKickEvent) {
        if(event.reason == "Flying is not enabled on this server") {
            event.isCancelled = true
            event.leaveMessage(text(""))
        }
    }
    @EventHandler
    fun onPlayerAttack(event: EntityDamageByEntityEvent) {
        if(event.damager is Player) {
            val player = event.entity
            val list = process.player(player as Player)?.list ?: return
            if(list.contains(event.damager)) {
                event.isCancelled = true
            }
        }
    }
    @EventHandler
    fun onKnockBack(event: EntityKnockbackByEntityEvent) {
        if(event.hitBy is Player) {
            val player = event.entity
            val list = process.player(player as Player)?.list ?: return
            if(list.contains(event.hitBy)) {
                event.isCancelled = true
            }
        }
    }
    @EventHandler
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        if(process.player(event.player)!!.rank >= -1) {
            event.isCancelled = true
        }
    }
}
class TeleportTo(val player: Player, val target: Player): Runnable {
    override fun run() {
        player.teleport(target)
    }
}