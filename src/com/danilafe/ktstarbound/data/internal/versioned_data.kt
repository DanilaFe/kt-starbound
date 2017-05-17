package com.danilafe.ktstarbound.data.internal

import com.danilafe.ktstarbound.Dynamic
import com.danilafe.ktstarbound.readers.GenericReader

/**
 * Versioned data stored in Starbound files.
 * The parameter is a reader because this isn't always
 * a top-level element or file.
 */
public class VersionedData(reader: GenericReader) {

    /**
     * The name of the data.
     */
    public val name: String = reader.serializedReadString()!!
    /**
     * The version of the versioned data.
     */
    public val version: Int
    /**
     * The data itself.
     */
    public val data: Dynamic

    init {
        reader.advance(1)
        version = reader.readInt()!!
        data = reader.serializedReadDynamic()!!
    }
}
