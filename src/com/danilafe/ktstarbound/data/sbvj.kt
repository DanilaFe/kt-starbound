package com.danilafe.ktstarbound.data

import com.danilafe.ktstarbound.data.internal.VersionedData
import com.danilafe.ktstarbound.readers.RandomReader
import java.io.File

/**
 * An SBVJS06 parser.
 * Able to parse Starbound's SBVJ06 files,
 * which are just a VersionedData package with a header.
 */
class SBVJ06(file: File) {

    /**
     * The string at the beginning of the file.
     * This string not being SBVJ06 indicates a corrupted file.
     */
    public val keyString: String
    /**
     * The actual data in the file.
     */
    public val versionedData: VersionedData

    init {
        val reader = RandomReader(file, 0)
        keyString = reader.readString(6)!!
        versionedData = VersionedData(reader)
    }

}

