package com.github.h0tk3y.synsetOps

import java.io.File

/**
 * Created by igushs on 5/31/16.
 */

fun main(args: Array<String>) {
    with(InputContext) {
        val tasks = makeTasks(pwnClusters, wiktionaryExamples, tolokaReplacement)

        File("tasks.out.tsv").printWriter().use { writer ->
            writer.println("INPUT:word1\tINPUT:word2\tINPUT:word3\tINPUT:word4\tINPUT:word5\tINPUT:word6\tINPUT:word7\tINPUT:word8\tINPUT:word9\tINPUT:sentence\tINPUT:synsetTag\tGOLDEN:word1\tGOLDEN:word2\tGOLDEN:word3\tGOLDEN:word4\tGOLDEN:word5\tGOLDEN:word6\tGOLDEN:word7\tGOLDEN:word8\tGOLDEN:word9\tHINT:text")
            for (t in tasks) {
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
                    val taskWords = b.supplemented(9, "")
                    writer.println("${taskWords.joinToString("\t")}\t${t.sentence}\t${t.taskTag}${"\t".repeat(10)}")
                }
            }
        }
    }
    println("Done.")
}

private val tolokaReplacement = listOf<Pair<Regex, (MatchResult) -> CharSequence>>(
        Regex("""\{\{-\}\}""") to { `_` -> " - " },
        Regex("""^\{\{--\}\}""") to { `_` -> "- " },
        Regex("""\{\{--\}\}""") to { `_` -> "<br>- " },
        Regex("""\{\{итд\}\}""") to { `_` -> "и т.д." },
        Regex("""\{\{выдел\|(.*?)\}\}""") to { it -> "<b><font color=\"#cc0000\">${it.groups[1]!!.value}</font></b>" })