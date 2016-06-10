package com.github.h0tk3y.synsetOps

import java.io.File
import kotlin.comparisons.compareBy

/**
 * Created by igushs on 5/31/16.
 */

fun main(args: Array<String>) {
    with(InputContext) {
        val tasks = makeTasks(pwnClusters, wiktionaryExamples, mTsarReplacement)

        val time = System.currentTimeMillis()

        File("mtsar.tasks.csv").printWriter().use { writer ->
            for (t in tasks.sortedWith(compareBy({ it.taskId }))) {

                val wordsUnionParts = t.words
                        .toBatch(7)
                        .toMutableList()
                        .apply {
                            if (size > 1 && last().size <= 2) {
                                val last = get(lastIndex)
                                val beforeLast = get(lastIndex - 1)
                                removeAt(lastIndex)
                                removeAt(lastIndex)
                                add(last + beforeLast)
                            }
                        }

                for (b in wordsUnionParts) {
                    val wordsString = b.joinToString("|")
                    writer.println("${t.taskId},substitutions,$time,${t.taskTag},multiple,\"${t.sentence}\",$wordsString")
                }
            }
        }
    }
    println("Done.")
}

private val mTsarReplacement = listOf<Pair<Regex, (MatchResult) -> CharSequence>>(
        Regex("""\{\{-\}\}""")   to { `_` -> " - " },
        Regex("""^\{\{--\}\}""") to { `_` -> "- " },
        Regex("""\{\{--\}\}""")  to { `_` -> " - " },
        Regex("""\{\{итд\}\}""") to { `_` -> "и т.д." },
        Regex("""\{\{выдел\|(.*?)\}\}""") to { it -> "*${it.groups[1]!!.value}*"})