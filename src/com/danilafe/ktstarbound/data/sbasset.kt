package com.danilafe.ktstarbound.data

import com.danilafe.ktstarbound.Dynamic
import com.danilafe.ktstarbound.readers.RandomReader
import java.io.File

public class SBAsset6(val file: File) {

    public data class FileData(val offset: Long, val length: Long)

    public val keyString: String
    public val indexString: String
    public val fileInfo: Map<String, Dynamic>
    public val numberFiles: Long
    public val files: MutableMap<String, FileData>

    init {
        val randomReder = RandomReader(file, 0)
        keyString = randomReder.readString(8)!!
        val metadataOffset = randomReder.readLong()!!

        randomReder.move(metadataOffset)
        indexString = randomReder.readString(5)!!
        fileInfo = randomReder.serializedReadMap()!!
        numberFiles = randomReder.readLong()!!
        files = mutableMapOf()
        for(i in 0.. numberFiles - 1){
            val length = randomReder.readByte()!!
            val pathLength = randomReder.readString(length.toInt())!!
            files.put(pathLength, FileData(randomReder.readLong()!!, randomReder.readLong()!!))
        }
    }

}
