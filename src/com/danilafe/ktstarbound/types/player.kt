package com.danilafe.ktstarbound.types

import com.danilafe.ktstarbound.DynamicMap
import com.danilafe.ktstarbound.DynamicString
import com.danilafe.ktstarbound.data.SBVJ06
import java.io.File

/**
 * A class that wraps Starbound's .player files.
 */
public class Player(file: File) {

    /**
     * The data of the player that appears useful to the reader.
     * The rest of the data can be accessed using the sbvj field.
     */
    public data class PlayerData(val name: String)

    /**
     * The SBVJ06 data structure that makes up the structure of the player file.
     */
    val sbvj = SBVJ06(file)
    /**
     * Seemingly useful player data, already converted from SBVJ.
     */
    val playerData = readPlayerData()

    /**
     * Creates a new PlayerData instance from the data in the player's SBVJ06
     */
    fun readPlayerData(): PlayerData? {
        val dataMap = sbvj.versionedData.rootElement as? DynamicMap ?: return null
        val identityMap = dataMap.data["identity"] as? DynamicMap ?: return null
        val name = identityMap.data["name"] as? DynamicString ?: return null

        return PlayerData(name.data)
    }


}

