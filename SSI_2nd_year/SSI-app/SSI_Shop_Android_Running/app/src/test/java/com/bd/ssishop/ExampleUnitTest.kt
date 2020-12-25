package com.bd.ssishop

import android.util.Log
import com.bd.ssishop.api.SsiApi
import kotlinx.coroutines.*
import org.junit.Test

import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import kotlinx.coroutines.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)

        runBlocking {
            val response = SsiApi.instance.test()
            print(response.data)
        }

    }

    @Test
    fun testRunblock() = runBlocking {

        launch(Dispatchers.IO) {
            delay(1000L)
            println("world")
            println(Thread.currentThread())
        }

        println("hello")
        println(Thread.currentThread())
    }

    @Test
    fun coroutineTest(){
        val job = GlobalScope.launch {
//        runBlocking {
//            delay(1000L)


            testSuspend()

            println("world")
            println(Thread.currentThread())
        }
        println("hello")
        println(Thread.currentThread())
        readLine()
    }

    suspend fun testSuspend(){

        println("test suspend")
        delay(1000L)
        println(Thread.currentThread())
    }

    data class Person(val name: String, val age: Int)
    val people = listOf(Person("Alice", 29), Person("Bob", 31))

    @Test
    fun test(){
        lookForAlice(people)
    }

    fun lookForAlice(people: List<Person>) {
        people.forEach {
            if (it.name == "Alice") {
                println("Found!")
                return
            }
        }
        println("Alice is not found")
    }

}