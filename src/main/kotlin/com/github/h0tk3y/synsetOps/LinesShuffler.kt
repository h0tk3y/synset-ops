package com.github.h0tk3y.synsetOps

import java.io.File
import java.util.*

/**
 * Created by igushs on 6/6/16.
 */

fun main(args: Array<String>) {
    val out = File("shuffle.out").printWriter()
    out.use { writer ->
        File(args[0]).readLines().toMutableList().shuffled().forEach { writer.println(it) }
    }
}

fun <T> MutableList<T>.shuffled(): List<T> {
    val result = this.toMutableList()
    Collections.shuffle(result)
    return result
}