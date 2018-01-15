/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
@file:API(status = EXPERIMENTAL, since = "5.1")
package org.junit.jupiter.api

import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.junit.jupiter.api.function.Executable
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * [Stream] of functions to be executed.
 */
private typealias ExecutableStream = Stream<() -> Unit>
private fun ExecutableStream.convert() = map { Executable(it) }

/**
 * @see Assertions.assertAll
 */
fun assertAll(executables: ExecutableStream) =
    Assertions.assertAll(executables.convert())

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, executables: ExecutableStream) =
    Assertions.assertAll(heading, executables.convert())

/**
 * [Collection] of functions to be executed.
 */
private typealias ExecutableCollection = Collection<() -> Unit>
private fun ExecutableCollection.convert() = map { Executable(it) }

/**
 * @see Assertions.assertAll
 */
fun assertAll(executables: ExecutableCollection) =
    Assertions.assertAll(executables.convert())

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, executables: ExecutableCollection) =
    Assertions.assertAll(heading, executables.convert())

/**
 * @see Assertions.assertAll
 */
fun assertAll(vararg executables: () -> Unit) =
    assertAll(executables.toList().stream())

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, vararg executables: () -> Unit) =
    assertAll(heading, executables.toList().stream())

/**
 * Example usage:
 * ```kotlin
 * val exception = assertThrows<IllegalArgumentException> {
 *     throw IllegalArgumentException("Talk to a duck")
 * }
 * assertEquals("Talk to a duck", exception.message)
 * ```
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(noinline executable: () -> Unit): T =
    Assertions.assertThrows(T::class.java, Executable(executable))

/**
 * Example usage:
 * ```kotlin
 * val exception = assertThrows<IllegalArgumentException>("Should throw an Exception") {
 *     throw IllegalArgumentException("Talk to a duck")
 * }
 * assertEquals("Talk to a duck", exception.message)
 * ```
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(message: String, noinline executable: () -> Unit): T =
    assertThrows({ message }, executable)

/**
 * Example usage:
 * ```kotlin
 * val exception = assertThrows<IllegalArgumentException>({ "Should throw an Exception" }) {
 *     throw IllegalArgumentException("Talk to a duck")
 * }
 * assertEquals("Talk to a duck", exception.message)
 * ```
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(noinline message: () -> String, noinline executable: () -> Unit): T =
    Assertions.assertThrows(T::class.java, Executable(executable), Supplier {
        /*
         * This is a hacky workaround due to a bug in how the JDK 9 JavaDoc code generator interacts with the
         * generated Kotlin Bytecode.
         * https://youtrack.jetbrains.com/issue/KT-20025
         */
        message()
    })
