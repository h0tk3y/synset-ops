package com.github.h0tk3y.synsetOps

import java.io.File

fun main(args: Array<String>) {
    val timestampsForParticipants = File("""synsetOps\mtsar_add_remove.in""").readLines()
            .map { it.split(",").let { it[6] to it[2].toInt() } }
            .groupBy({ it.first }) { it.second }

    val minTotal = timestampsForParticipants.values.flatten().min()!!
    val maxTotal = timestampsForParticipants.values.flatten().max()!!
    println("Total: ${maxTotal - minTotal} seconds")

    val averageTime = timestampsForParticipants.values.map { it.max()!! - it.min()!! }.average()
    println("Average: $averageTime")
}