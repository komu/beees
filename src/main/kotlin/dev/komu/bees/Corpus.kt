package dev.komu.bees

import fi.evident.raudikko.Morphology
import java.io.File

typealias Corpus = List<Document>

fun readCorpora(
    directoriesByLabel: Map<String, File>,
    tokenizer: Tokenizer = SimpleTokenizer,
    filter: (File) -> Boolean = { true }
): Map<String, Corpus> =
    directoriesByLabel.mapValues { (_, dir) ->
        readCorpus(dir, tokenizer, filter)
    }

private fun readCorpus(directory: File, tokenizer: Tokenizer, filter: (File) -> Boolean): Corpus {
    val result = mutableListOf<Document>()

    fun recurse(dir: File) {
        for (child in dir.listFiles().orEmpty()) {
            if (child.isDirectory)
                recurse(child)
            else if (filter(child))
                result += Document(child, tokenizer)
        }
    }

    recurse(directory)
    return result
}

interface Tokenizer {
    fun tokenize(s: String): List<String>
}

object SimpleTokenizer : Tokenizer {
    override fun tokenize(s: String) =
        s.split(Regex("""([.,;:@+&<>?\n(){}/]|\s)+""")).filter { it.isNotBlank() }
            .map { it.toLowerCase() } // TODO: quick'n'dirty hack
}

object FinnishTokenizer : Tokenizer {

    private val analyzer = Morphology.loadBundled().newAnalyzer()

    override fun tokenize(s: String): List<String> =
        SimpleTokenizer.tokenize(s).map { analyzer.baseForms(it).firstOrNull() ?: it }.map { it.toLowerCase() }
}

class Document(private val file: File, private val tokenizer: Tokenizer) {
    val words: List<String>
        get() = tokenizer.tokenize(file.readText())
}
