package com.github.h0tk3y.synsetOps

import java.io.File

/**
 * Created by igushs on 5/31/16.
 */

fun main(args: Array<String>) {
    with(InputContext) {
        val tasksByTag = makeTasks(pwnClusters, wiktionaryExamples, mTsarReplacement).associateBy { it.taskTag }

        val mTsarTasksIdToTag = File("mtsar.tasks.csv").readLines().drop(1).filter { it.isNotBlank() }.associate { it.split(",").let { it[0] to it[3] } }
        val mTsarTags = mTsarTasksIdToTag.values.toSet()

        val tolokaTasks = File("toloka.answers.tsv").readLines().drop(1).filter { it.isNotBlank() && it.startsWith("\t") }
                .map { it.split("\t").dropWhile { it.isBlank() }.let { it[10] to it.subList(0, 9).filterIndexed { i, s -> it[11 + i] == "true" } } }
                .filter { it.first in mTsarTags }
                .groupBy({ it.first }) { it.second }

        for ((taskTag, answersList) in tolokaTasks) {
            val task = tasksByTag[taskTag]!!
            val newSynset = mutableSetOf<String>()
            val n = answersList.size.toDouble() / mTsarTasksIdToTag.count { it.value == taskTag }
            val wordsCounts = answersList.flatten().groupBy { it }.mapValues { it.value.size }
            newSynset.addAll(wordsCounts.filter { it.value >= (n.toDouble() / 2) - 0.001 }.map { it.key })
            if (newSynset.isNotEmpty()) {
                newSynset.add(task.markedWord)
                println("New synset: $newSynset\n    for task tag $taskTag,\n    sentence: ${task.sentence},\n    from synsets: ${task.synsets.map { it.id }}")
            }
        }
    }

    println("Done.")
}

private val mTsarReplacement = listOf<Pair<Regex, (MatchResult) -> CharSequence>>(
        Regex("""\{\{-\}\}""") to { `_` -> " - " },
        Regex("""^\{\{--\}\}""") to { `_` -> "- " },
        Regex("""\{\{--\}\}""") to { `_` -> "\n- " },
        Regex("""\{\{итд\}\}""") to { `_` -> "и т.д." },
        Regex("""\{\{выдел\|(.*?)\}\}""") to { it -> "*${it.groups[1]!!.value}*" })