package me.gurkz.gurkanslib.config

import me.gurkz.gurkanslib.Globals.LOGGER
import java.util.function.Consumer
import java.util.stream.Collectors

internal data class Config<T>(val clazz: Class<T>, val apply: Consumer<T>, val filename: String) {
    fun refresh() {
        apply.accept(GurkansConfig.load(clazz, filename))
    }
}

object ConfigManager {
    private val configs = ArrayList<Config<*>>()

    fun <T> register(clazz: Class<T>, filename: String, apply: Consumer<T>): T {
        LOGGER.trace("registerring config manager {}", filename)

        configs.add(Config(clazz, apply, filename))
        return GurkansConfig.load(clazz, filename)
    }

    fun unregister(clazz: Class<*>) {
        configs.removeIf { conf: Config<*> -> conf.clazz == clazz }
    }

    fun refresh(clazz: Class<*>) {
        configs.stream()
            .filter { conf: Config<*> -> conf.clazz == clazz }
            .forEach { obj: Config<*> -> obj.refresh() }
    }

    fun refreshAll() {
        configs.forEach(Consumer { obj: Config<*> -> obj.refresh() })
    }

    val allRegistered: List<Class<*>>
        get() = configs.stream()
            .map(Config<*>::clazz)
            .collect(Collectors.toList())
}