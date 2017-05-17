package com.danilafe.ktstarbound.data

import com.danilafe.ktstarbound.compareByteArrays
import com.danilafe.ktstarbound.readers.RandomLeafReader
import com.danilafe.ktstarbound.readers.RandomReader
import java.io.File

/**
 * A BTreeDB5 parser.
 * This class is able to open a Starbound B-Tree database
 * and read data from it.
 */
public class BTreeDB5(val file: File) {

    /**
     * The size of the header of a BTreeDB5 database.
     */
    public val headerSize = 512
    /**
     * The size of the prefix that identifies a node.
     */
    public val prefixSize = 2

    /**
     * The KeyString that is read first in
     * the file. Should always be BTreeDB5.
     * The string being anything else indicates an
     * invalid file.
     */
    public val keyString: String
    /**
     * The name of the database.
     */
    public val name: String
    /**
     * The size of data blocks in this database.
     */
    public val blockSize: Int
    /**
     * The size of keys in this database.
     */
    public val keySize: Int
    /**
     * The size of key-value entries in the database.
     */
    public val entrySize: Int

    /**
     * Whether to use the second root node instead
     * of the first.
     */
    public val swap: Boolean

    /**
     * The block index of the first free node.
     */
    public val firstFreeNode: Int
    /**
     * The offset of the first free node.
     */
    public val firstFreeNodeOffset: Int
    /**
     * The block index of the first root node.
     */
    public val firstRootNode: Int
    /**
     * Whether the first root node is a leaf.
     */
    public val isFirstRootLeaf: Boolean

    /**
     * The block index of the second free node.
     */
    public val secondFreeNode: Int
    /**
     * The offset of the second free node.
     */
    public val secondFreeNodeOffset: Int
    /**
     * The block index of the second root node.
     */
    public val secondRootNode: Int
    /**
     * Whether the second root node is a leaf.
     */
    public val isSecondRootLeaf: Boolean

    init {
        val reader = RandomReader(file, 0)
        keyString = reader.readString(8)!!
        blockSize = reader.readInt()!!
        name = reader.readString(16)!!
        keySize = reader.readInt()!!
        entrySize = keySize + 4
        swap = reader.readBoolean()!!

        firstFreeNode = reader.readInt()!!
        reader.advance(4)
        firstFreeNodeOffset = reader.readInt()!!
        firstRootNode = reader.readInt()!!
        isFirstRootLeaf = reader.readBoolean()!!

        secondFreeNode = reader.readInt()!!
        reader.advance(4)
        secondFreeNodeOffset = reader.readInt()!!
        secondRootNode = reader.readInt()!!
        isSecondRootLeaf = reader.readBoolean()!!
    }

    /**
     * Reads data from the given key of the BTree.
     * If the key is not found or something goes wrong, returns NULL.
     */
    public fun get(key: ByteArray): ByteArray? {
        /* Gets the index where to start */
        var index = headerSize + (if (swap) secondRootNode else firstRootNode) * blockSize

        /* Reader to actually read the file */
        val reader = RandomReader(file, index.toLong())

        /* The String representing the type of the node. */
        var typeString: String
        while (true) {
            typeString = reader.readString(2) ?: return null
            /* If the node is an index, we keep going in this loop.
               Unfortunately, Kotlin doesn't have evaluate assignments
               as expressions, so it's not possible to make a loop
               with an actual condition. */
            if (typeString == "II") {
                /* Skips apparently unimportant data. */
                reader.advance(1)

                /* Initial bounds for low and high data in a BTree. */
                var low = 0
                var high = reader.readInt() ?: return null

                /* The block to go to if the key is smaller than all keys in this node.
                   Since BTrees have 1 more child than the number of values in each node,
                   it's impossible to simply maintain a key-pointer mapping as
                   the rest of this tree does. Thus, the first pointer, which is smaller
                   than all keys, is alone, and is read separately. */
                var block = reader.readInt() ?: return null

                /* Performs a binary search. We'll need to jump around,
                   so we save the start index since advance() won't cut it. */
                val startIndex = reader.index()
                while (low < high) {
                    /* Find the new mid and check the key under it. */
                    val mid = (low + high) / 2
                    reader.move(startIndex + entrySize * mid)
                    val otherKey = reader.read(keySize) ?: return null

                    /* This is part of binary search. Moves low or high to be
                       the mid, thus getting us closer to the value we're looking for. */
                    val comparisonResult = compareByteArrays(otherKey, key)
                    if (comparisonResult > 0) high = mid
                    else low = mid + 1
                }
                /* The key isn't smaller than ALL others, so we need to read the pointer. */
                if (low > 0) {
                    reader.move(startIndex + entrySize * (low - 1) + keySize)
                    block = reader.readInt() ?: return null
                }
                reader.move((headerSize + blockSize * block).toLong())
            } else {
                break
            }
        }

        if (typeString == "LL") {
            /* LeafReader is a nice implementation of GenericReader that allows for jumping around leaf nodes and their data. */
            val leafReader = RandomLeafReader(file, reader.index(), headerSize.toLong(), blockSize.toLong(), prefixSize.toLong())
            val numKeys = leafReader.readInt() ?: return null

            /* Look through all the keys, looking for the right one. */
            for (i in 0..numKeys - 1) {
                val otherKey = leafReader.read(keySize) ?: return null
                val length = leafReader.readVariableInt() ?: return null

                if (compareByteArrays(key, otherKey) == 0) {
                    /* If the key we read is right, read that data and return. */
                    return leafReader.read(length.toInt())
                } else {
                    /* Otherwise, since the data is sequential, we still have to skip it. */
                    leafReader.advance(length)
                }
            }
        }

        return null
    }

}
