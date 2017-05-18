package com.danilafe.ktstarbound.data

import com.danilafe.ktstarbound.Dynamic
import com.danilafe.ktstarbound.readers.RandomReader
import java.io.File

/**
 * An SBAsset6 parser.
 * Able to read Starbound SBAsset files, but not currently extract files
 * from it.
 */
public class SBAsset6(file: File) {

    /**
     * The information known about a file path, such as its length and offset.
     */
    public data class FileData(val offset: Long, val length: Long)

    /**
     * The keyString that is at the beginning of an SBAsset6 file.
     * The string not being SBAsset6 indicates a corrupted file.
     */
    public val keyString: String
    /**
     * The string that is at the beginning of the asset index.
     * The string not being INDEX indicates a corrupted file.
     */
    public val indexString: String
    /**
     * Information in the file, self-described.
     */
    public val fileInfo: Map<String, Dynamic>
    /**
     * The number of files in the asset file.
     */
    public val numberFiles: Long
    /**
     * File paths mapped to info about these files.
     */
    public val files: MutableMap<String, FileData>

    init {
        val randomReder = RandomReader(file, 0)
        keyString = randomReder.readString(8)!!
        val metadataOffset = randomReder.readLong()!!

        randomReder.move(metadataOffset)
        indexString = randomReder.readString(5)!!
        fileInfo = randomReder.serializedReadMap()!!
        numberFiles = randomReder.readVariableInt()!!
        files = mutableMapOf()
        for(i in 0.. numberFiles - 1){
            val length = randomReder.readByte()!!
            val pathLength = randomReder.readString(length.toInt())!!
            files.put(pathLength, FileData(randomReder.readLong()!!, randomReder.readLong()!!))
        }
    }

}
