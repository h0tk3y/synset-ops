package com.github.h0tk3y.synsetOps

import java.io.File

fun main(args: Array<String>) {
    val synsets = Synset.readList(File("yarn-synsets.csv"))
    val grouped = groupByIntersection(synsets)
    val intersectionByTwoWords = grouped.filterKeys { it.size == 2 }
    val withWordSetsAndIds = intersectionByTwoWords
            .mapValues { it.value.flatMap { it.words }.toSet() to it.value.map { it.id } }
            .toList()
            .sortedByDescending { it.second.first.size }



    File("out.csv").printWriter().use { writer ->
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