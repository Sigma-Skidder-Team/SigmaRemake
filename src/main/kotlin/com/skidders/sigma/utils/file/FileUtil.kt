package com.skidders.sigma.utils.file

import com.skidders.SigmaReborn
import net.minecraft.client.MinecraftClient

import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtil {

    public fun createFolder(name: String) {
        try {
            Files.createDirectories(Paths.get(name))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    public fun getRunningPath(): Path {
        return Paths.get(MinecraftClient.getInstance().runDirectory.path, "sigma")
    }

    @Throws(IOException::class)
    fun getFilesInDirectory(directoryPath: String): List<String> {
        val filenames = mutableListOf<String>()

        val path = Paths.get(directoryPath)
        if (Files.exists(path) && Files.isDirectory(path)) {
            Files.newDirectoryStream(path).use { directoryStream ->
                directoryStream.forEach { filename ->
                    filenames.add(filename.fileName.toString())
                }
            }
        } else {
            SigmaReborn.LOGGER.info("Directory not found or is not a directory: {}", directoryPath)
        }

        return filenames.toList()
    }

    @Throws(IOException::class)
    fun copyResourceToFile(sourceFileName: String, destinationPath: String) {
        FileUtil::class.java.getResourceAsStream(sourceFileName)?.use { `in` ->
            FileOutputStream(destinationPath).use { out ->
//                if (false) {
//                    throw FileNotFoundException("Resource not found: $sourceFileName")
//                }

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (`in`.read(buffer).also { bytesRead = it } != -1) {
                    out.write(buffer, 0, bytesRead)
                }
            }
        }
    }

}
