package com.github.xenon.battle.plugin

import com.github.monun.kommand.kommand
import com.github.xenon.battle.BattleProcess
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.plugin.java.JavaPlugin

class BattlePlugin : JavaPlugin() {
    companion object {
        lateinit var instance: BattlePlugin
    }
    private var process: BattleProcess? = null
    override fun onEnable() {
        instance = this
        Bukkit.getServer().worlds.forEach { w ->
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            w.setGameRule(GameRule.KEEP_INVENTORY, true)
            w.setSpawnLocation(0, 0, 0)
            val location = Location(w, 0.5, 0.0, 0.5)
            if (location.block.type != Material.LODESTONE) {
                location.block.type = Material.LODESTONE
            }
        }
        val world = Bukkit.getWorlds().first()
        world.worldBorder.setCenter(0.5, 0.5)
        world.worldBorder.setSize(2000.0, 0)
        dataFolder.mkdirs()
        setupKommands()
    }
    fun processStart() {
        require(process == null) { "Process is already running" }
        for(player in Bukkit.getOnlinePlayers()) {
            val item = ItemStack(Material.COMPASS)
            val compassMeta: CompassMeta = item.itemMeta as CompassMeta
            compassMeta.lodestone = Location(Bukkit.getWorlds().first(), 0.5, 0.0, 0.5)
            compassMeta.isLodestoneTracked = true
            item.itemMeta = compassMeta
            player.inventory.addItem(item)
        }
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