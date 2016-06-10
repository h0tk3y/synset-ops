package com.github.h0tk3y.synsetOps

data class Synset(val id: Int,
                  val words: Set<String>,
                  val partOfSpeech: String) {
    companion object {
        /**
         * @param input synset in CSV format: id,word1;word2;word3,partOfSpeech
         */
        fun fromCsv(input: String): Synset =
                input.split(",").let { parts ->
                    Synset(parts[0].toInt(),
                           parts[1].split(";").toSet(),
                           parts[2])
                }

        /**
         * @param input CSV format text including header line, which will be dropped in parsing
         * @see fromCsv
         */
        fun listFromCsv(input: String): List<Synset> =
                input.lines().drop(1).filter { it.isNotBlank() }.map { fromCsv(it) }
    }
}