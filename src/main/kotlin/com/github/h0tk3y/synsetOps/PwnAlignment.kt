package com.github.h0tk3y.synsetOps

import java.io.File

/**
 * Created by igushs on 5/26/16.
 */

/**
 * @return mapping from PWN id to YARN synsets
 */
fun parsePwnAlignmentClusters(input: String, synsetsById: Map<Int, Synset>): Map<String, List<Synset>> =
        input.lines().drop(1)
                .filter { it.isNotEmpty() }
                .map { it.split(",").let { it[0] to synsetsById[it[1].drop(1).toInt()]!! } }
                .groupBy { it.first }
                .mapValues { it.value.map { it.second } }

/**
 * Makes human-readable form of the PWN alignment groups.
 */
fun main(args: Array<String>) {
    val synsetsById = Synset.listFromCsv(File("yarn-synsets.csv").readText()).associate { it.id to it }
    val groups = parsePwnAlignmentClusters(File("duplicate-cleansing-2016-05-25.in.csv").readText(), synsetsById)

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