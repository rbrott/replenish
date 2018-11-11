package com.replenish

import java.io.InputStream

class CSVReader(inputStream: InputStream) {
    val rows: List<List<String>> = inputStream.reader()
        .readLines()
        .asSequence()
        .filter { !it.startsWith("#") }
        .map { it.split(",") }
        .toList()

}