package com.github.h0tk3y.synsetOps

fun reformat(example: String, replacement: List<Pair<Regex, (MatchResult) -> CharSequence>>) =
        replacement.fold(example, { acc, it -> acc.replace(it.first, it.second) })

fun <T> Iterable<T>.batch(batchSize: Int): Iterable<Iterable<T>> = object : Iterable<Iterable<T>> {
    override fun iterator(): Iterator<Iterable<T>> {
        val originalIterator = this@batch.iterator()
        return generateSequence {
            val batch = originalIterator.asSequence().take(batchSize).toList()
            if (batch.isEmpty()) null else batch
        }.iterator()
    }
}

fun <T> List<T>.supplemented(toSize: Int, withItem: T) = (0..(lastIndex.coerceAtLeast(toSize - 1))).map {
    if (it in indices)
        get(it)
    else
        withItem
}