package com.github.h0tk3y.synsetOps

/**
 * Created by igushs on 5/18/16.
 */

fun groupByIntersection(synsets: List<Synset>): Map<Set<String>, Set<Synset>> {
    val wordBins: Map<String, Set<Synset>> = synsets
            .flatMap { synset -> synset.words.map { word -> Pair(word, synset) } }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second }.toSet() }

    fun intersection(s1: Synset, s2: Synset) = s1.words.intersect(s2.words)

    val intersectedByTwoWords = synsets.asSequence()
            .flatMap { synset ->
                synset.words.asSequence().flatMap { word ->
                    wordBins[word]!!.asSequence()
                            .filterNot { it === synset }
                            .map { Pair(intersection(synset, it), setOf(synset, it)) }
                }
            }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second }.fold(setOf<Synset>()) { acc, it -> acc + it } }

    return intersectedByTwoWords
}