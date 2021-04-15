package com.github.xenon.battle

import com.github.xenon.battle.plugin.BattlePlugin.Companion.instance
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
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
        process.player(player)?.let { victim ->
            if(victim.rank >= 0 && victim.rank <= -3) return@let
            player.killer?.let { killer ->
                val kill = process.player(killer)
                if(kill != null) {
                    process.rank(victim, kill)
                } else {
                    process.rank(victim)
                }
            }
        }
        if(player.killer is Player) {
            Bukkit.getScheduler().runTaskLater(instance, TeleportTo(player, player.killer as Player), 10L)
            process.player(player.killer as Player)?.list?.add(player)
            process.player(player)?.list?.forEach {
                if(process.player(it)?.rank!! <= -1) {
                    process.player(player.killer as Player)?.list?.add(it)
                }
            }
        } else {
            val random = Random.nextInt(process.survivePlayers.count())
            val p = process.survivePlayers[random + 1]
            process.player(p.player!!)?.list?.add(player)
            process.player(player)?.list?.forEach {
                if(process.player(it)?.rank!! <= -1) {
                    process.player(p.player!!)?.list?.add(it)
                }
            }
        }
    }
    @EventHandler
    fun onKick(event: PlayerKickEvent) {
        if(event.reason == "Flying is not enabled on this server") {
            event.isCancelled = true
            event.leaveMessage(text(""))
        }
    }
}
class TeleportTo(val player: Player, val target: Player): Runnable {
    override fun run() {
        player.teleport(target)
    }
}