package com.github.xenon.battle.plugin

import com.github.monun.kommand.kommand
import org.bukkit.plugin.java.JavaPlugin

class BattlePlugin : JavaPlugin() {
    override fun onEnable() {
        kommand {
            register("bf") {
                then("start") {

                }
                then("stop") {

                }
            }
        }
    }
}