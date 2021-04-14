package com.github.xenon.battle

import com.github.xenon.battle.plugin.BattlePlugin
import com.google.common.collect.ImmutableMap
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.scheduler.BukkitTask
import java.util.*

class BattleProcess(val plugin: BattlePlugin) {
    private val players: Map<UUID, BattlePlayer>
    private val onlinePlayers
        get() = Bukkit.getOnlinePlayers().mapNotNull { players[it.uniqueId] }
    val survivePlayers
        get() = players.values.filter { it.rank == -2 }
    private val topPlayers
        get() = players.values.filter { it.rank == -1 }
    private val knockoutPlayers
        get() = players.values.filter { it.rank >= 0 }

    private val battleListener: BattleListener
    private val bukkitTask: BukkitTask
    var phase: Int = 1
    var shrinktime = 300 - (phase - 1) * 60
    var waittime = shrinktime * 2
    var ticks = 0
    init {
        players = ImmutableMap.copyOf(
            Bukkit.getOnlinePlayers().asSequence().filter {
                it.gameMode.isDamageable
            }.associate { p ->
                p.uniqueId to BattlePlayer(p.uniqueId, p.name).apply {
                    player = p
                }
            }
        )
        Bukkit.getOnlinePlayers().forEach {
            if(!it.isOp && it.uniqueId !in players) {
                it.kick(text("게임 참가자가 아닙니다."))
            }
        }
        plugin.server.apply {
            battleListener = BattleListener(this@BattleProcess).also {
                pluginManager.registerEvents(it, plugin)
            }
            bukkitTask = scheduler.runTaskTimer(plugin, this@BattleProcess::onUpdate, 0L, 1L)
        }
    }
    fun unregister() {
        HandlerList.unregisterAll()
        bukkitTask.cancel()
    }
    fun player(uuid: UUID) = players[uuid]

    fun player(player: Player) = player(player.uniqueId)

    private fun onUpdate() {
        survivePlayers.forEach { it.onUpdate() }
        knockoutPlayers.forEach { p ->
            if(p.player!!.isOp) {
                p.player!!.gameMode = GameMode.SPECTATOR
            } else {
                p.player!!.banPlayer("You are knocked down", "BATTLEROYALE")
            }
        }
        updateBorder()
    }
    fun rank(player: BattlePlayer, killer: BattlePlayer) {
        require(player.rank <= -1) { "Cannot redefine rank ${player.name}" }
        player.rank += 1
        if(player.rank >= 0) {
            for(p in Bukkit.getOnlinePlayers()) {
                p.sendMessage("${player.name}님이 탈락하셨습니다.")
            }
        }
    }
    fun rank(player: BattlePlayer) {
        require(player.rank <= -1) { "Cannot redefine rank ${player.name}" }
        player.rank += 1
        if(player.rank >= 0) {
            for(p in Bukkit.getOnlinePlayers()) {
                p.sendMessage("${player.name}님이 탈락하셨습니다.")
            }
        }
    }
    fun updateBorder() {
        val world = Bukkit.getWorlds().first()
        ticks++
        if(ticks == 20) {
            waittime--
            ticks = 0
        }
        for(player in Bukkit.getOnlinePlayers()) {
            player.sendActionBar(text("전장 축소까지 ${waittime}초"))
        }
    }
}
private val GameMode.isDamageable
    get() = this == GameMode.SURVIVAL || this == GameMode.ADVENTURE