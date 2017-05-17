package com.danilafe.ktstarbound

/**
 * Compares two arrays, returning the Integer comparison result of their length if their
 * length are not equal, otherwise returns the integer comparison results
 * of the first different pair of values.
 * Returns 0 on equality.
 */
public fun compareByteArrays(a: ByteArray, b: ByteArray): Int {
    if (a.size != b.size) return Integer.compare(a.size, b.size)
    return (0..a.size - 1)
            .firstOrNull { a[it] != b[it] }
            ?.let { Integer.compare(a[it].toInt(), b[it].toInt()) }
            ?: 0
}
