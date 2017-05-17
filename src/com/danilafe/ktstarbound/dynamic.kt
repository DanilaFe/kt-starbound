package com.danilafe.ktstarbound

public sealed class Dynamic
public object DynamicNull : Dynamic()
public data class DynamicDouble(val data: Double) : Dynamic()
public data class DynamicBoolean(val data: Boolean) : Dynamic()
public data class DynamicVariableInt(val data: Long) : Dynamic()
public data class DynamicString(val data: String) : Dynamic()
public data class DynamicList(val data: List<Dynamic>) : Dynamic()
public data class DynamicMap(val data: Map<String, Dynamic>) : Dynamic()