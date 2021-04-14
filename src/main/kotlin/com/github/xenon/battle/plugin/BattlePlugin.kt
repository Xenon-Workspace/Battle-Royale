package com.github.xenon.battle.plugin

import com.github.monun.kommand.kommand
import com.github.xenon.battle.BattleProcess
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.plugin.java.JavaPlugin

class BattlePlugin : JavaPlugin() {
    private var process: BattleProcess? = null
    override fun onEnable() {
        Bukkit.getServer().worlds.forEach { w ->
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            w.setGameRule(GameRule.KEEP_INVENTORY, true)
        }
        val world = Bukkit.getWorlds().first()
        world.worldBorder.setCenter(0.5, 0.5)
        dataFolder.mkdirs()
        setupKommands()
    }
    fun processStart() {
        require(process == null) { "Process is already running" }
        process = BattleProcess(this)
    }
    fun processStop() {
        process?.let {
            it.unregister()
            process = null
        }
    }
    private fun setupKommands() = kommand {
        register("bf") {
            then("start") {
                executes {
                    processStart()
                }
            }
            then("stop") {
                executes {
                    processStop()
                }
            }
        }
    }
}