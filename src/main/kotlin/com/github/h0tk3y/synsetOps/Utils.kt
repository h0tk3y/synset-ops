package com.github.h0tk3y.synsetOps

/**
 * Decomposes the receiver [Iterable] to [Iterable] of [List]s, each [batchSize] items, the last one may be less.
 */
fun <T> Iterable<T>.toBatch(batchSize: Int): Iterable<List<T>> = object : Iterable<List<T>> {
    override fun iterator(): Iterator<List<T>> {
        val originalIterator = this@toBatch.iterator()
        return generateSequence {
            val batch = originalIterator.asSequence().take(batchSize).toList()
            if (batch.isEmpty()) null else batch
        }.iterator()
    }
}

/**
 * @return [List] where the first items are taken from the receiver, and if there is less items than [toSize],
 *                the items are added up to [toSize], all are [withItem].
 */
fun <T> List<T>.supplemented(toSize: Int, withItem: T) = (0..(lastIndex.coerceAtLeast(toSize - 1))).map {
    if (it in indices)
        get(it)
    else
        withItem
}