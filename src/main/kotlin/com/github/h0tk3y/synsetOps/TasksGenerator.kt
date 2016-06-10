package com.github.h0tk3y.synsetOps

import java.util.*

/**
 * Created by igushs on 6/9/16.
 */
data class Task(var taskId: Int,
                val clusterId: Int,
                val words: MutableSet<String>,
                val markedWord: String,
                val synsets: MutableSet<Synset>,
                val sentence: String) {
    val taskTag: String get() = "${clusterId}_$taskId"
}

private fun reformat(example: String, replacement: List<Pair<Regex, (MatchResult) -> CharSequence>>) =
        replacement.fold(example, { acc, it -> acc.replace(it.first, it.second) })

fun makeTasks(clusters: Map<Set<String>, Set<Synset>>,
              examplesByWord: Map<String, List<String>>,
              sentenceReformat: List<Pair<Regex, (MatchResult) -> CharSequence>>): Set<Task> {
    val taskBySentence = HashMap<String, MutableList<Task>>()

    var intersectionId = 1
    var taskId = 1

    for ((intersection, sets) in clusters) {
        val iId = intersectionId++

        val filteredSets = sets.filter { it.words.size > intersection.size }
        if (filteredSets.size < 2) {
            continue
        }

        val wordsWithSentences = intersection
                .flatMap { word -> examplesByWord[word].orEmpty().map { word to reformat(it, sentenceReformat) } }
                .filter { "*" in it.second && "{" !in it.second && it.second.count { it == ' ' } > 1 }

        if (wordsWithSentences.isEmpty()) {
            continue
        }

        val taskWords = filteredSets.map { it.words }
                .flatten()
                .distinct()
                .subtract(intersection)

        fun similarity(w1: Set<String>, w2: Set<String>) = w1.intersect(w2).size

        for ((word, sentence) in wordsWithSentences) {
            val similarTask = taskBySentence[sentence].orEmpty().firstOrNull { similarity(taskWords, it.words) >= 5 }
            if (similarTask != null) {
                similarTask.words.addAll(taskWords)
                similarTask.synsets.addAll(sets)
            } else {
                val tId = taskId++
                val task = Task(tId, iId, HashSet(taskWords), word, HashSet(sets), sentence)
                taskBySentence.getOrPut(sentence) { mutableListOf() }.add(task)
            }
        }
    }

    return taskBySentence.values.flatten().toSet()
}