package com.danilafe.ktstarbound.data

import com.danilafe.ktstarbound.data.internal.VersionedData
import com.danilafe.ktstarbound.readers.RandomReader
import java.io.File

class SBVJ06(val file: File) {

    public val keyString: String
    public val data: VersionedData

    init {
        val reader = RandomReader(file, 0)
        keyString = reader.readString(6)!!
        data = VersionedData(reader)
    }

}

