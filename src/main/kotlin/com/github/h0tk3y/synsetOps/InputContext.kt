package com.github.h0tk3y.synsetOps

import java.io.File

object InputContext {
    val synsets by lazy { Synset.listFromCsv(File("yarn-synsets.csv").readText()) }

    val grouped by lazy { groupByIntersection(synsets) }

    val intersectionByTwoWords by lazy { grouped.filterKeys { it.size == 2 } }

    val intersectionIndex by lazy {
        intersectionByTwoWords.entries
                .flatMap { e -> e.value.map { it.words to e } }
                .groupBy({ it.first }) { it.second }
    }

    val encounteredSynsets by lazy {
        parsePwnAlignmentClusters(File("duplicate-cleansing-2016-05-25.in.csv").readText(),
                                  synsets.associate { it.id to it })
                .values.flatten()
                .groupBy { it.words }
                .entries.sortedByDescending { it.value.size }
                .map { it.value.first() }
    }

    val wiktionaryExamples by lazy {
        WiktionaryModel.parse(File("""word_meaning_examples_ru.xml""").readText())
                .toWordsQuotesMap()
    }

    val pwnClusters by lazy {
        encounteredSynsets
                .flatMap {
                    intersectionIndex[it.words]
                            .orEmpty()
                            .map { it.key to it.value.groupBy { it.words }.map { it.value.first() }.toSet() }
                }
                .toMap()
    }
}
