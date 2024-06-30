package com.github.kekovd.dependencycheckplugin.services.interfaces

interface CSVService {
    fun readCsvFile(
        csvFilePath: String,
        excludeColumns: Set<Int>,
        excludeTailColumnsCount: Int,
        basePath: String?
    ): Pair<Array<String>, List<Array<String>>>
}