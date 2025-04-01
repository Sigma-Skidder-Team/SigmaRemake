package com.skidders.sigma.utils.file

import com.skidders.SigmaReborn
import net.minecraft.client.MinecraftClient
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtil {
    @JvmStatic
    fun createFolder(name: String) {
        try {
            Files.createDirectories(Paths.get(name))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    val runningPath: Path
        get() = Paths.get(MinecraftClient.getInstance().runDirectory.path, "sigma")

    @JvmStatic
    @Throws(IOException::class)
    fun getFilesInDirectory(directoryPath: String): List<String> {
        val filenames: MutableList<String> = ArrayList()

        val path = Paths.get(directoryPath)
        if (Files.exists(path) && Files.isDirectory(path)) {
            Files.newDirectoryStream(path).use { directoryStream ->
                for (entry in directoryStream) {
                    val filename = entry.fileName.toString()
                    filenames.add(filename)
                }
            }
        } else {
            SigmaReborn.LOGGER.info("Directory not found or is not a directory: {}", directoryPath)
        }

        return filenames
    }

    @JvmStatic
    @Throws(IOException::class)
    fun copyResourceToFile(sourceFileName: String, destinationPath: String) {
        FileUtil::class.java.getResourceAsStream(sourceFileName).use { `in` ->
            FileOutputStream(destinationPath).use { out ->
                if (`in` == null) {
                    throw FileNotFoundException("Resource not found: $sourceFileName")
                }
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while ((`in`.read(buffer).also { bytesRead = it }) != -1) {
                    out.write(buffer, 0, bytesRead)
                }
            }
        }
    }
}