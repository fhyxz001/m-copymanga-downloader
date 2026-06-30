package com.copymanga.downloader.util

import kotlin.random.Random

private val FIRST_NAMES = listOf(
    "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
    "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
    "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa",
    "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
)
private val LAST_NAMES = listOf(
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
    "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
    "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young",
)

fun randomUsername(): String {
    val first = FIRST_NAMES.random()
    val last = LAST_NAMES.random()
    val number = Random.nextInt(10, 999)
    return "$first$last$number".filter { it.isLetterOrDigit() }
}

fun randomPassword(length: Int = Random.nextInt(10, 30)): String {
    val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + "!@#$%^&*"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}
