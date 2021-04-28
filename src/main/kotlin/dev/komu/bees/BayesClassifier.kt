package dev.komu.bees

import kotlin.math.exp
import kotlin.math.ln

class BayesClassifier private constructor(
    private val categoriesByName: Map<String, Category>,
    private val wordPriors: Map<String, Double>
) {

    /** Alpha is added to each word-count to give non-zero probability for all words */
    private val alpha = 1.0

    fun classify(words: List<String>): Pair<String, Double>? {

        // TODO: assume uniform prior, could be something else as well
        val prior = ln(1.0 / categoriesByName.size)
        val probabilitiesByCategory = categoriesByName.mapValues { (_, category) ->
            var logProbability = prior

            for (word in words) {
                val propPrior = wordPriors[word] ?: continue
                val probCat = (category.wordCount(word) + alpha) / (category.totalWords + alpha * wordPriors.size)
                logProbability += ln(probCat) - propPrior
            }

            exp(logProbability)
        }

        return probabilitiesByCategory.entries.maxByOrNull { it.value }?.toPair()
    }

    private class Category {

        var documents = 0
            private set

        var totalWords = 0
            private set
        val histogram = mutableMapOf<String, Int>()

        fun addDocument(words: Collection<String>) {
            documents++
            totalWords += words.size
            for (word in words)
                histogram[word] = (histogram[word] ?: 0) + 1
        }

        fun wordCount(word: String): Int =
            histogram[word] ?: 0
    }

    class Builder {
        private val categoriesByName = mutableMapOf<String, Category>()

        fun addDocument(category: String, words: List<String>) {
            val histogram = categoriesByName.getOrPut(category) { Category() }
            histogram.addDocument(words)
        }

        fun build(): BayesClassifier {
            val vocabulary = categoriesByName.values.flatMap { it.histogram.keys }.toSet()
            val totalWords = categoriesByName.values.sumBy { it.totalWords }

            return BayesClassifier(categoriesByName, wordPriors = vocabulary.associateWith { w ->
                ln(categoriesByName.values.sumBy { it.wordCount(w) }.toDouble() / totalWords)
            })
        }
    }

    companion object {
        operator fun invoke(init: Builder.() -> Unit): BayesClassifier {
            val builder = Builder()
            builder.init()
            return builder.build()
        }
    }
}
