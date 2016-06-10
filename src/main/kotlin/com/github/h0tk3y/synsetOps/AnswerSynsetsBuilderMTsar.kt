package com.github.h0tk3y.synsetOps

import java.io.File

/**
 * Created by igushs on 5/31/16.
 */

fun main(args: Array<String>) {
    with(InputContext) {

        val tasksByTag = makeTasks(pwnClusters, wiktionaryExamples, mTsarReplacement).associateBy { it.taskTag }

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
    }

    println("Done.")
}

private val mTsarReplacement = listOf<Pair<Regex, (MatchResult) -> CharSequence>>(
        Regex("""\{\{-\}\}""") to { `_` -> " - " },
        Regex("""^\{\{--\}\}""") to { `_` -> "- " },
        Regex("""\{\{--\}\}""") to { `_` -> "\n- " },
        Regex("""\{\{итд\}\}""") to { `_` -> "и т.д." },
        Regex("""\{\{выдел\|(.*?)\}\}""") to { it -> "*${it.groups[1]!!.value}*" })