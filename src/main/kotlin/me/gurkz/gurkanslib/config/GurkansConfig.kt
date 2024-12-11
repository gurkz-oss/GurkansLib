package me.gurkz.gurkanslib.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.gurkz.gurkanslib.Globals.LOGGER
import net.fabricmc.loader.api.FabricLoader
import java.io.*
import java.lang.reflect.InvocationTargetException
import kotlin.system.exitProcess

object GurkansConfig {
    private val GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()

    private fun getFile(filename: String): File {
        return FabricLoader.getInstance().configDir.resolve("GurkansMods/$filename").toFile()
    }

    fun <T> load(clazz: Class<T>, filename: String): T {
        LOGGER.trace("loading config {}", filename)

        val file = getFile(filename)
        var config: T? = null

        if (file.exists()) {
            try {
                BufferedReader(
                    InputStreamReader(
                        FileInputStream(file)
                    )
                ).use { reader ->
                    config = GSON.fromJson(reader, clazz)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (config == null) {
            try {
                config = clazz.getDeclaredConstructor().newInstance()
            } catch (e: InstantiationException) {
                e.printStackTrace()
                exitProcess(1)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                exitProcess(1)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
                exitProcess(1)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
                exitProcess(1)
            }
        }

        GurkansConfig.save(config, filename)
        return config!!
    }

    private fun <T> save(config: T, filename: String) {
        LOGGER.trace("saving config {}", filename)

        val file = getFile(filename)
        if (!file.parentFile.exists()) {
            if (!file.parentFile.mkdir()) {
                System.err.println("Failed to create a directory for $file")
            }
        }

        try {
            OutputStreamWriter(
                FileOutputStream(file)
            ).use { writer ->
                GSON.toJson(config, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}