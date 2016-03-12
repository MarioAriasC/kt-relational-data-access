package org.kotlinprimavera.demo

import org.kotlinprimavera.jdbc.core.extract
import org.kotlinprimavera.jdbc.core.query
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootApplication
open class Application : CommandLineRunner {

    private val log = LoggerFactory.getLogger(Application::class.java)

    //    @Autowired var jdbcTemplate: JdbcTemplate = uninitialized()
    @Autowired lateinit var jdbcTemplate: JdbcTemplate

    override fun run(vararg strings: String?) {
        log.info("Creating tables")

        with(jdbcTemplate) {
            execute("DROP TABLE customers IF EXISTS")
            execute("""
            CREATE TABLE customers (
                id SERIAL,
                first_name VARCHAR(255),
                last_name VARCHAR(255)
            )""")
        }

        val splitUpNames = arrayOf("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").map { name -> name.split(" ").toTypedArray() }

        splitUpNames.forEach { name -> log.info("Inserting customer record for %s %s".format(name[0], name[1])) }

        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?, ?)", splitUpNames)

        log.info("Querying for customer records where first_name = 'Josh':")

        //Without KotlinPrimavera
        /*jdbcTemplate.query("SELECT id, first_name, last_name FROM customers WHERE first_name = ?", arrayOf("Josh")) { rs, rowNum ->
            Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
        }.forEach { customer -> log.info(customer.toString()) }*/

        jdbcTemplate.query("SELECT id, first_name, last_name FROM customers WHERE first_name = ?", "Josh") { rs, rowNum ->
            rs.extract { Customer(long["id"]!!, string["first_name"]!!, string["last_name"]!!) }
        }.forEach { customer -> log.info(customer.toString()) }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

data class Customer(val id: Long, val firstName: String, val lastName: String)
