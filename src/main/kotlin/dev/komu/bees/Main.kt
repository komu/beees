package dev.komu.bees

import java.io.File

fun main() {
    crossValidate(
        mapOf(
            "foo" to File("data/finnish/foo"),
            "bar" to File("data/finnish/bar"),
            "baz" to File("data/finnish/baz")
        ),
        tokenizer = FinnishTokenizer
    )

    crossValidate(
        mapOf(
            "ham" to File("data/enron1/ham"),
            "spam" to File("data/enron1/spam")
        )
    )
}
