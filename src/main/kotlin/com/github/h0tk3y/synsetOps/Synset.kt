package com.github.h0tk3y.synsetOps

import java.io.File
import java.io.InputStreamReader

data class Synset(val id: Int,
                  val words: Set<String>,
                  val partOfSpeech: String) {
    companion object {
        fun fromCsv(input: String): Synset =
                input.split(",").let { parts ->
                    Synset(parts[0].toInt(),
                           parts[1].split(";").toSet(),
                           parts[2])
                }

        fun readList(stream: InputStreamReader): List<Synset> =
                stream.readLines().drop(1).map { fromCsv(it) }

        fun readList(file: File) = file.inputStream().reader().use { readList(it) }
    }
}