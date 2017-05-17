package com.danilafe.ktstarbound.readers

import com.danilafe.ktstarbound.*
import java.nio.ByteBuffer

/**
 * A generic abstraction over something
 * that can read data.
 */
public abstract class GenericReader {
    /**
     * Reads length bytes of data from the reader.
     */
    public abstract fun read(length: Int): ByteArray?

    /**
     * Advances the reader by length bytes, ignoring
     * data in that interval.
     */
    public abstract fun advance(length: Long)

    /**
     * Moves the reader to the given position.
     */
    public abstract fun move(length: Long)

    /**
     * Gets the current position of the reader.
     */
    public abstract fun index(): Long

    /**
     * Reads a double from the reader.
     */
    public fun readDouble(): Double? {
        val readBytes = read(8)
        return if (readBytes != null) ByteBuffer.wrap(readBytes).double else null
    }

    /**
     * Reads a float from the reader.
     */
    public fun readFloat(): Float? {
        val readBytes = read(4)
        return if (readBytes != null) ByteBuffer.wrap(readBytes).float else null
    }

    /**
     * Reads a long from the reader.
     */
    public fun readLong(): Long? {
        val readBytes = read(8)
        return if (readBytes != null) ByteBuffer.wrap(readBytes).long else null
    }

    /**
     * Reads an integer from the reader.
     */
    public fun readInt(): Int? {
        val readBytes = read(4)
        return if (readBytes != null) ByteBuffer.wrap(readBytes).int else null
    }

    /**
     * Reads a byte from the reader.
     */
    public fun readByte(): Byte? {
        val readBytes = read(1)
        return if (readBytes != null) readBytes[0] else null
    }

    /**
     * Reads a boolean from the reader.
     */
    public fun readBoolean(): Boolean? {
        val byte = readByte()
        return if (byte != null) byte.toInt() != 0 else null
    }

    /**
     * Reads a string of a fixed length
     * from the reader.
     */
    public fun readString(length: Int): String? {
        val readBytes = read(length)
        return if (readBytes != null) String(readBytes).substringBefore(0.toChar()) else null
    }

    /**
     * Reads a starbound variable-length integer
     * from the reader.
     */
    public fun readVariableInt(): Long? {
        var initialVariable = 0L
        var byte: Long
        do {
            byte = readByte()?.toLong() ?: return null
            initialVariable = (initialVariable shl 7) or (byte and 0b1111111L)
        } while (byte and 0x10000000L == 0x10000000L)
        return initialVariable
    }

    /**
     * Reads a serialized sequence of bytes,
     * whose length is specified by a variable-length
     * integer.
     */
    public fun serializedReadBytes(): ByteArray? {
        val length = readVariableInt()
        return if (length != null) read(length.toInt()) else null
    }

    /**
     * Reads a serialized sequence of bytes and converts
     * it to a string.
     */
    public fun serializedReadString(): String? {
        val bytes = serializedReadBytes()
        return if (bytes != null) String(bytes) else null
    }

    /**
     * Reads a list of dynamic elements.
     */
    public fun serializedReadList(): List<Dynamic>? {
        val length = readVariableInt() ?: return null
        val list = mutableListOf<Dynamic>()
        for (i in 0L..length) {
            list.add(serializedReadDynamic() ?: return null)
        }
        return list
    }

    /**
     * Reads a map of strings to dynamic elements.
     */
    public fun serializedReadMap(): Map<String, Dynamic>? {
        val length = readVariableInt() ?: return null
        val map = mutableMapOf<String, Dynamic>()
        for (i in 0L..length) {
            val key = serializedReadString()
            val value = serializedReadDynamic()
            if (key == null || value == null) return null
            map.put(key, value)
        }
        return map
    }

    /**
     * Reads a single dynamic element.
     */
    public fun serializedReadDynamic(): Dynamic? {
        val type = readByte()?.toInt() ?: return null
        return when (type) {
            2 -> {
                val double = readDouble()
                if (double != null) DynamicDouble(double) else null
            }
            3 -> {
                val boolean = readBoolean()
                if (boolean != null) DynamicBoolean(boolean) else null
            }
            4 -> {
                val variableInt = readVariableInt()
                if (variableInt != null) DynamicVariableInt(variableInt) else null
            }
            5 -> {
                val string = serializedReadString()
                if (string != null) DynamicString(string) else null
            }
            6 -> {
                val list = serializedReadList()
                if (list != null) DynamicList(list) else null
            }
            7 -> {
                val map = serializedReadMap()
                if (map != null) DynamicMap(map) else null
            }
            else -> null
        }
    }
}