package com.github.h0tk3y.synsetOps

import java.io.File

/**
 * @param synsets to find intersection
 * @return mapping from words set to set of [Synset]s which intersect by the words
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

/**
 * Prints human-readable synset clustering by intersection where intersection size is 2.
 * @param args arg0 -- path to synsets CSV
 * @see Synset.Companion.listFromCsv
 */
fun main(args: Array<String>) {
    val synsets = Synset.listFromCsv(File("yarn-synsets.csv").readText())
    val grouped = groupByIntersection(synsets)
    val intersectionByTwoWords = grouped.filterKeys { it.size == 2 }
    val withWordSetsAndIds = intersectionByTwoWords
            .mapValues { it.value.flatMap { it.words }.toSet() to it.value.map { it.id } }
            .toList()
            .sortedByDescending { it.second.first.size }

    File("clusters-intersecting-by-two-words.csv").printWriter().use { writer ->
        writer.println("intersection,words,synsetIds")
        for ((intersection, wordsAndIds) in withWordSetsAndIds) {
            writer.println(
                    listOf(intersection,
                           wordsAndIds.first,
                           wordsAndIds.second
                    ).map { it.joinToString(";") }.joinToString(", ")
            )
        }
    }
}