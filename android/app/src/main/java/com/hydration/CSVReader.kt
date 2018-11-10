package com.hydration

import java.io.InputStream

class CSVReader(inputStream: InputStream) {
    val rows: List<List<String>>

    init {
        rows = inputStream.reader()
            .readLines()
            .asSequence()
            .filter { !it.startsWith("#") }
            .map { it.split(",") }
            .toList()
    }
}