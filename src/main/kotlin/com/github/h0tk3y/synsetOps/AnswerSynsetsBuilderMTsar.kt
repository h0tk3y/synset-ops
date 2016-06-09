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
            .groupBy({ it.first }) { it.second }

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

    val tasksByTag = makeTasks(interestingIntersections, wiktionaryExamples).associateBy { it.taskTag }

    val mTsarTasksIdToTag = File("mtsar.tasks.csv").readLines().drop(1).filter { it.isNotBlank() }.associate { it.split(",").let { it[0] to it[3] } }

    val mTsarAnswers = File("mtsar.answers.csv").readLines()
            .drop(1)
            .filter { it.isNotBlank() && "skip" !in it }
            .map { it.split(",").let { it[5] to it[7].split("|").filter { it.isNotBlank() }.toSet() } }
            .groupBy { mTsarTasksIdToTag[it.first] }

    for ((taskTag, answersList) in mTsarAnswers) {
        val task = tasksByTag[taskTag]!!
        val answersGrouped = answersList.groupBy({ it.first }) { it.second }
        val newSynset = mutableSetOf<String>()
        for ((taskId, answers) in answersGrouped) {
            val n = answers.size
            val wordsCounts = answers.flatten().groupBy { it }.mapValues { it.value.size }
            newSynset.addAll(wordsCounts.filter { it.value >= (n.toDouble() / 2) - 0.001 }.map { it.key })
        }
        if (newSynset.isNotEmpty()) {
            newSynset.add(task.markedWord)
            println("New synset: $newSynset\n    for task tag $taskTag,\n    sentence: ${task.sentence},\n    from synsets: ${task.synsets.map { it.id }}")
        }
    }


    println("Done.")
}

private fun makeTasks(interestingIntersections: Set<Pair<Set<String>, Set<Synset>>>, wiktionaryExamples: Map<String, List<String>>): Set<Task> {
    val taskBySentence = HashMap<String, MutableList<Task>>()

    var intersectionId = 1
    var taskId = 1

    for ((intersection, sets) in interestingIntersections) {
        val iId = intersectionId++

        val filteredSets = sets.filter { it.words.size > intersection.size }
        if (filteredSets.size < 2) {
            continue
        }

        val wordsWithSentences = intersection
                .flatMap { word -> wiktionaryExamples[word].orEmpty().map { word to reformat(it, mTsarReplacement) } }
                .filter { "*" in it.second && "{" !in it.second && it.second.count { it == ' ' } > 1 }

        if (wordsWithSentences.isEmpty()) {
            continue
        }

        val taskWords = filteredSets.map { it.words }
                .flatten()
                .distinct()
                .subtract(intersection)

        fun similarity(w1: Set<String>, w2: Set<String>) = w1.intersect(w2).size

        for ((word, sentence) in wordsWithSentences) {
            val similarTask = taskBySentence[sentence].orEmpty().firstOrNull { similarity(taskWords, it.words) >= 5 }
            if (similarTask != null) {
                similarTask.words.addAll(taskWords)
                similarTask.synsets.addAll(sets)
            } else {
                val tId = taskId++
                val taskTag = "${tId}_$iId"
                val task = Task(iId, HashSet(taskWords), word, taskTag, HashSet(sets), sentence)
                taskBySentence.getOrPut(sentence) { mutableListOf() }.add(task)
            }

        }
    }

    return taskBySentence.values.flatten().toSet()
}

private val mTsarReplacement = listOf<Pair<Regex, (MatchResult) -> CharSequence>>(
        Regex("""\{\{-\}\}""") to { `_` -> " - " },
        Regex("""^\{\{--\}\}""") to { `_` -> "- " },
        Regex("""\{\{--\}\}""") to { `_` -> "\n- " },
        Regex("""\{\{итд\}\}""") to { `_` -> "и т.д." },
        Regex("""\{\{выдел\|(.*?)\}\}""") to { it -> "*${it.groups[1]!!.value}*" })