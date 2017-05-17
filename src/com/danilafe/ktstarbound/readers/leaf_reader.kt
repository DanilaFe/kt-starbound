package com.danilafe.ktstarbound.readers

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

/**
 * A reader specialized to read BTree leaves - it takes
 * care to avoid jumping over leaf bounds and follows pointers
 * to next nodes.
 */
public class RandomLeafReader(file: File, index: Long, val headerSize: Long, val blockSize: Long, val prefixSize: Long) : GenericReader() {

    private val dataFile = RandomAccessFile(file, "r")
    private var block = (index - headerSize) / blockSize

    init {
        dataFile.seek(index)
    }

    /**
     * Gets the end index of a block.
     */
    internal fun getBlockEndIndex(index: Long): Long {
        return headerSize + blockSize * (index + 1)
    }

    /**
     * Gets the end index of the current block.
     */
    internal fun getCurrentEndIndex(): Long {
        return getBlockEndIndex(block)
    }

    /**
     * Reads length bytes without
     * checking for leaf bounds etc.
     */
    internal fun readRaw(length: Int): ByteArray? {
        val byteArray = ByteArray(length)
        dataFile.read(byteArray)
        return byteArray
    }

    /**
     * Reads a single integer without
     * checking for leaf bounds etc.
     */
    internal fun readRawInt(): Int? {
        val bytes = readRaw(4) ?: return null
        return ByteBuffer.wrap(bytes).int
    }

    override fun read(length: Int): ByteArray? {
        if (dataFile.length() - dataFile.filePointer <= length) return null
        val accumulator = ByteArrayOutputStream(length)
        var remainingRead = length
        while (remainingRead > 0) {
            val canRead = minOf(remainingRead, (getCurrentEndIndex() - 4 - dataFile.filePointer).toInt())
            val readArray = readRaw(canRead)
            accumulator.write(readArray)
            remainingRead -= canRead

            if (dataFile.filePointer == getCurrentEndIndex() - 4) {
                block = readRawInt()?.toLong() ?: return null
                dataFile.seek(headerSize + block * blockSize + prefixSize)
            }
        }
        return accumulator.toByteArray()
    }

    override fun advance(length: Long) {
        var remainingRead = length
        while (remainingRead > 0) {
            val canRead = minOf(remainingRead, (getCurrentEndIndex() - 4 - dataFile.filePointer))
            remainingRead -= canRead
            dataFile.seek(dataFile.filePointer + canRead)

            if (dataFile.filePointer == getCurrentEndIndex() - 4) {
                block = readRawInt()?.toLong() ?: return
                dataFile.seek(block * blockSize + prefixSize)
            }
        }
    }

    override fun move(length: Long) {
        dataFile.seek(length)
    }

    override fun index(): Long {
        return dataFile.filePointer
    }


}


