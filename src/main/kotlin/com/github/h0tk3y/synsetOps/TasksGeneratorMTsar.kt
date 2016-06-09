package com.github.h0tk3y.synsetOps

import java.io.File
import java.util.*

/**
 * Created by igushs on 5/31/16.
 */

fun main(args: Array<String>) {
    val synsets = Synset.readList(File("yarn-synsets.csv"))
    val grouped = groupByIntersection(synsets)
    val intersectionByTwoWords = grouped.filterKeys { it.size == 2 }
    val intersectionIndex = intersectionByTwoWords.entries
            .flatMap { e -> e.value.map { it.words to e } }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second } }

    val encounteredSynsets =
            parseSynsetAlignmentGroups(File("duplicate-cleansing-2016-05-25.in.csv").readText(),
                                       synsets.associate { it.id to it })
                    .values.flatten()
                    .groupBy { it.words }
                    .entries.sortedByDescending { it.value.size }
                    .map { it.value.first() }

    val wiktionaryExamples =
            WiktionaryModel.parse(File("""C:\Users\igushs\Projects\synsetOps\word_meaning_examples_ru.xml""").readText())
                    .toWordsQuotesMap()

    val interestingIntersections = encounteredSynsets
            .flatMap { intersectionIndex[it.words].orEmpty().map { it.key to it.value } }
            .toSet()

    data class Task(val iId: Int,
                    val words: MutableSet<String>,
                    val synsets: MutableSet<Synset>,
                    val sentence: String)

    val taskBySentence = HashMap<String, MutableList<Task>>()

    var intersectionId = 1
    for ((intersection, sets) in interestingIntersections) {
        val iId = intersectionId++

        println("Generating tasks for synsets ${sets.map { it.id }} intersecting by ${intersection} (*_$iId).")

        val filteredSets = sets.filter { it.words.size > intersection.size }
        if (filteredSets.size < 2) {
            println("    E: there's no two+ synsets with words outside the intersection, skipping.\n")
            continue
        }

        val sentences = intersection
                .flatMap { wiktionaryExamples[it].orEmpty().map { reformat(it, mTsarReplacement) } }
                .filter { "*" in it && "{" !in it && it.count { it == ' ' } > 1 }

        if (sentences.isEmpty()) {
            println("    E: no valid sentences found.\n")
            continue
        }

        val taskWords = filteredSets.map { it.words }
                .flatten()
                .distinct()
                .subtract(intersection)
        println("    I: words = $taskWords")

        fun similarity(w1: Set<String>, w2: Set<String>) = w1.intersect(w2).size

        for (s in sentences) {
            val similarTask = taskBySentence[s].orEmpty().firstOrNull { similarity(taskWords, it.words) >= 5 }
            if (similarTask != null) {
                val similarity = similarity(taskWords, similarTask.words)
                println("    I: found and merged with similar ($similarity) task ${similarTask.iId}:${similarTask.sentence}")
                similarTask.words.addAll(taskWords)
                similarTask.synsets.addAll(sets)
            } else {
                taskBySentence.getOrPut(s) { mutableListOf() }.add(Task(iId, HashSet(taskWords), HashSet(sets), s))
                println("    I: created task $iId:$s")
            }
        }
        println()
    }

    var taskId = 1
    val time = System.currentTimeMillis()

    File("tasks.out.csv").printWriter().use { writer ->
        for ((iId, words, sets, sentence) in taskBySentence.values.flatten().sortedBy { it.iId }) {
            val tId = taskId++

            val wordsUnionParts = words
                    .batch(7)
                    .map { it.toList() }
                    .toMutableList()

            with(wordsUnionParts) {
                if (size > 1 && last().size <= 2) {
                    val last = get(wordsUnionParts.lastIndex)
                    val beforeLast = get(wordsUnionParts.lastIndex - 1)
                    removeAt(lastIndex)
                    removeAt(lastIndex)
                    add(last + beforeLast)
                }
            }

            for (b in wordsUnionParts) {
                val wordsString = b.joinToString("|")
                writer.println("$tId,substitutions,$time,${tId}_$iId,multiple,\"$sentence\",$wordsString")
            }
        }
    }

    val nullableString: String? = null

    println("Done.")
}

private val mTsarReplacement = listOf<Pair<Regex, (MatchResult) -> CharSequence>>(
        Regex("""\{\{-\}\}""")   to { `_` -> " - " },
        Regex("""^\{\{--\}\}""") to { `_` -> "- " },
        Regex("""\{\{--\}\}""")  to { `_` -> "\n- " },
        Regex("""\{\{итд\}\}""") to { `_` -> "и т.д." },
        Regex("""\{\{выдел\|(.*?)\}\}""") to { it -> "*${it.groups[1]!!.value}*"})