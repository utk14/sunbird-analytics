package org.ekstep.analytics.util

import com.typesafe.config.{Config, ConfigFactory}
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}


object RedisUtil {
    private lazy val config: Config = ConfigFactory.load
    private lazy val redis_host: String = config.getString("redis.host")
    private lazy val redis_port: Integer = config.getInt("redis.port")
    private lazy val jedisPool = new JedisPool(buildPoolConfig, redis_host, redis_port)

    private def buildPoolConfig: JedisPoolConfig = {
        val poolConfig = new JedisPoolConfig
        poolConfig.setMaxTotal(config.getInt("redis.connection.max"))
        poolConfig.setMaxIdle(config.getInt("redis.connection.idle.max"))
        poolConfig.setMinIdle(config.getInt("redis.connection.idle.min"))
        poolConfig.setTestOnBorrow(true)
        poolConfig.setTestOnReturn(true)
        poolConfig.setTestWhileIdle(true)
        poolConfig.setMinEvictableIdleTimeMillis(config.getInt("redis.connection.minEvictableIdleTimeSeconds"))
        poolConfig.setTimeBetweenEvictionRunsMillis(config.getInt("redis.connection.timeBetweenEvictionRunsSeconds"))
        poolConfig.setNumTestsPerEvictionRun(3)
        poolConfig.setBlockWhenExhausted(true)
        poolConfig
    }

    def getConnection: Jedis = jedisPool.getResource
}
