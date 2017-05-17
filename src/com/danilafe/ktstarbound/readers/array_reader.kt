package com.danilafe.ktstarbound.readers

import java.util.*

/**
 * A simple reader that holds its data in the given array.
 */
class ArrayReader(val array: ByteArray, var index: Int) : GenericReader() {

    override fun read(length: Int): ByteArray? {
        return Arrays.copyOfRange(array, index, index + length)
    }

    override fun advance(length: Long) {
        index += length.toInt()
    }

    override fun move(length: Long) {
        index = length.toInt()
    }

    override fun index(): Long {
        return index.toLong()
    }

}
