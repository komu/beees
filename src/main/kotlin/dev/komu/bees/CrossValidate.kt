package dev.komu.bees

import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Cross-validates by repeatedly training the classifier on all documents except one
 * and then testing the classification of the omitted document.
 */
fun crossValidate(
    baseDirs: Map<String, File>,
    folds: Int = 5,
    tokenizer: Tokenizer = SimpleTokenizer,
    filter: (File) -> Boolean = { true }
) {
    val corpora = readCorpora(baseDirs, tokenizer, filter)

    val entries = corpora.entries.flatMap { (k, docs) -> docs.map { k to it } }

    println("Performing $folds-fold cross-validation for ${entries.size} documents of corpus")
    var totalTrainingTime = 0L
    var totalValidationTime = 0L
    var totalErrors = 0
    var totalValidated = 0
    for ((training, validation) in entries.shuffled().divide(folds)) {
        var errors = 0

        val classifier: BayesClassifier
        val trainingTime = measureTimeMillis {
            classifier = BayesClassifier {
                for ((corpus, doc) in training)
                    addDocument(corpus, doc.words)
            }
        }

        val validationTime = measureTimeMillis {
            for ((corpus, doc) in validation) {
                val result = classifier.classify(doc.words)
                if (result?.first != corpus)
                    errors++
            }
        }

        totalTrainingTime += trainingTime
        totalValidationTime += validationTime
        totalErrors += errors
        totalValidated += validation.size
        val percent = errors.toDouble() / validation.size * 100
        System.out.printf(
            "errors: %d/%d (%.1f %%) (training %d ms, validation %d ms)\n",
            errors,
            validation.size,
            percent,
            trainingTime,
            validationTime
        )
    }

    val percent = totalErrors.toDouble() / totalValidated * 100
    System.out.printf(
        "Total errors: %d/%d (%.1f %%) (training %d ms, validation %d ms)\n",
        totalErrors,
        totalValidated,
        percent,
        totalTrainingTime,
        totalValidationTime
    )
}

private fun <T> List<T>.divide(folds: Int): List<Pair<List<T>, List<T>>> {
    val foldSize = ((size - 1) / folds) + 1

    val result = ArrayList<Pair<List<T>, List<T>>>(folds + 1)

    for (i in 0..size step foldSize) {
        val end = (i + foldSize).coerceAtMost(size)
        val training = subList(0, i) + subList(end, size)
        val validation = subList(i, end)

        result += Pair(training, validation)
    }

    return result
}
