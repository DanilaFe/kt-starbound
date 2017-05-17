package com.danilafe.ktstarbound.types

import com.danilafe.ktstarbound.*
import com.danilafe.ktstarbound.data.BTreeDB5
import com.danilafe.ktstarbound.data.internal.VersionedData
import com.danilafe.ktstarbound.readers.ArrayReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Inflater

public class World(file: File) {

    public data class WorldMetadata(val worldWidth: Int, val worldHeight: Int, val playerStart: Pair<Double, Double>,
                             val spawningEnabled: Boolean, val respawnInWorld: Boolean, val adjustPlayerStart: Boolean,
                             val dungeonIds: MutableMap<Long, String>, val protectedDungeons: MutableList<Long>)

    public val btreedb = BTreeDB5(file)
    public val metadata = readMetadata()

    public fun getKey(layer: Byte, x: Short, y: Short) : ByteArray {
        val key = ByteArray(5)
        key[0] = layer
        key[1] = (x.toInt() shr 8).toByte()
        key[2] = (x.toInt() and 0xff).toByte()
        key[3] = (y.toInt() shr 8).toByte()
        key[4] = (y.toInt() and 0xff).toByte()
        return key
    }

    public fun get(layer: Byte, x: Short, y: Short) : ByteArray? {
        val key = getKey(layer, x, y)
        val data = btreedb.get(key) ?: return null

        val output = ByteArrayOutputStream()
        val inflater = Inflater()
        val buffer = ByteArray(2048)
        inflater.setInput(data)
        while(!inflater.finished()){
            val readBytes = inflater.inflate(buffer)
            output.write(buffer, 0, readBytes)
        }
        output.close()
        return output.toByteArray()
    }

    public fun readMetadata() : WorldMetadata? {
        val data = get(0, 0, 0) ?: return null
        val arrayReader = ArrayReader(data, 0)
        val worldWidth = arrayReader.readInt() ?: return null
        val worldHeight = arrayReader.readInt() ?: return null
        val versionedData = VersionedData(arrayReader)
        val worldProperties = versionedData.rootElement as? DynamicMap ?: return null

        val playerStart = worldProperties.data["playerStart"] as? DynamicList ?: return null
        val playerStartPair = (playerStart.data[0] as? DynamicDouble ?: return null).data to
                (playerStart.data[1] as? DynamicDouble ?: return null).data

        val spawningEnabled = (worldProperties.data["spawningEnabled"] as? DynamicBoolean ?: return null).data
        val respawnInWorld = (worldProperties.data["respawnInWorld"] as? DynamicBoolean ?: return null).data
        val adjustPlayerStart = (worldProperties.data["adjustPlayerStart"] as? DynamicBoolean ?: return null).data

        val dungeonMap = mutableMapOf<Long, String>()
        for(dynamic in (worldProperties.data["dungeonIdMap"] as? DynamicList ?: return null).data){
            val list = dynamic as? DynamicList ?: return null
            dungeonMap.put((list.data[0] as? DynamicVariableInt ?: return null).data,
                    (list.data[1] as? DynamicString ?: return null).data)
        }

        val protectedDungeons = mutableListOf<Long>()
        (worldProperties.data["protectedDungeonIds"] as? DynamicList ?: return null).data.
                mapTo(protectedDungeons) { (it as? DynamicVariableInt?: return null).data }

        return WorldMetadata(worldWidth, worldHeight, playerStartPair,
                spawningEnabled, respawnInWorld, adjustPlayerStart,
                dungeonMap, protectedDungeons)
    }

}

