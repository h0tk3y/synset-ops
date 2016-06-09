package com.github.h0tk3y.synsetOps

import java.io.File

/**
 * Created by igushs on 5/26/16.
 */

fun parseSynsetAlignmentGroups(input: String, synsetsMap: Map<Int, Synset>) =
        input.lines().drop(1)
                .filter { it.isNotEmpty() }
                .map { it.split(",").let { it[0] to synsetsMap[it[1].drop(1).toInt()]!! } }
                .groupBy { it.first }
                .mapValues { it.value.map { it.second } }


fun main(args: Array<String>) {
    val synsets = Synset.readList(File("yarn-synsets.csv")).associate { it.id to it }
    val groups = parseSynsetAlignmentGroups(File("duplicate-cleansing-2016-05-25.in.csv").readText(), synsets)

    File("groups.out").printWriter().use { writer ->
        groups.forEach {
            writer.println("Group: ${it.key}")
            for (s in it.value) {
                writer.println(s.words)
            }
            writer.println()
        }
    }
}