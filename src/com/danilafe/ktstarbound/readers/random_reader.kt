package com.danilafe.ktstarbound.readers

import java.io.File
import java.io.RandomAccessFile

/**
 * A reader built on top of a RandomAccessFile, allowing
 * fast access to a file without having to hold the buffer in memory.
 */
public class RandomReader(file: File, index: Long) : GenericReader() {

    val dataFile = RandomAccessFile(file, "r")

    init {
        dataFile.seek(index)
    }

    override fun read(length: Int): ByteArray? {
        if (dataFile.length() - dataFile.filePointer < length) return null
        val byteBuffer = ByteArray(length)
        dataFile.read(byteBuffer)
        return byteBuffer
    }

    override fun advance(length: Long) {
        dataFile.seek(dataFile.filePointer + length)
    }

    override fun move(length: Long) {
        dataFile.seek(length)
    }

    override fun index(): Long {
        return dataFile.filePointer
    }

}

