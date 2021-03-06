package com.redis

import org.scalatest.FunSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith


@RunWith(classOf[JUnitRunner])
class OperationsSpec extends FunSpec 
                     with Matchers
                     with BeforeAndAfterEach
                     with BeforeAndAfterAll {

  val r = new RedisClient("localhost", 6379)

  override def beforeEach = {
  }

  override def afterEach = {
    r.flushdb
  }

  override def afterAll = {
    r.disconnect
  }

  describe("keys") {
    it("should fetch keys") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.keys("anshin*") match {
        case Some(s: List[Option[String]]) => s.size should equal(2)
        case None => fail("should have 2 elements")
      }
    }

    it("should fetch keys with spaces") {
      r.set("anshin 1", "debasish")
      r.set("anshin 2", "maulindu")
      r.keys("anshin*") match {
        case Some(s: List[Option[String]]) => s.size should equal(2)
        case None => fail("should have 2 elements")
      }
    }
  }

  describe("randomkey") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.randomkey match {
        case Some(s: String) => s should startWith("anshin") 
        case None => fail("should have 2 elements")
      }
    }
  }

  describe("rename") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.rename("anshin-2", "anshin-2-new") should equal(true)
      val thrown = intercept[Exception] { r.rename("anshin-2", "anshin-2-new") }
      thrown.getMessage should equal ("ERR no such key")
    }
  }

  describe("renamenx") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.renamenx("anshin-2", "anshin-2-new") should equal(true)
      r.renamenx("anshin-1", "anshin-2-new") should equal(false)
    }
  }

  describe("dbsize") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.dbsize.get should equal(2)
    }
  }

  describe("exists") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.exists("anshin-2") should equal(true)
      r.exists("anshin-1") should equal(true)
      r.exists("anshin-3") should equal(false)
    }
  }

  describe("del") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.del("anshin-2", "anshin-1").get should equal(2)
      r.del("anshin-2", "anshin-1").get should equal(0)
    }
  }

  describe("type") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.getType("anshin-2").get should equal("string")
    }
  }
  describe("expire") {
    it("should give") {
      r.set("anshin-1", "debasish")
      r.set("anshin-2", "maulindu")
      r.expire("anshin-2", 1000) should equal(true)
      r.ttl("anshin-2") should equal(Some(1000))
      r.expire("anshin-3", 1000) should equal(false)
    }
  }
  describe("sort") {
    it("should give") {
// sort[A](key:String, limit:Option[Pair[Int, Int]] = None, desc:Boolean = false, alpha:Boolean = false, by:Option[String] = None, get:List[String] = Nil)(implicit format:Format, parse:Parse[A]):Option[List[Option[A]]] = {
      r.hset("hash-1", "description", "one")
      r.hset("hash-1", "order", "100")
      r.hset("hash-2", "description", "two")
      r.hset("hash-2", "order", "25")
      r.hset("hash-3", "description", "three")
      r.hset("hash-3", "order", "50")
      r.sadd("alltest", 1)
      r.sadd("alltest", 2)
      r.sadd("alltest", 3)
      r.sort("alltest").getOrElse(Nil) should equal(List(Some("1"), Some("2"), Some("3")))
      r.sort("alltest", Some(Pair(0, 1))).getOrElse(Nil) should equal(List(Some("1")))
      r.sort("alltest", None, true).getOrElse(Nil) should equal(List(Some("3"), Some("2"), Some("1")))
      r.sort("alltest", None, false, false, Some("hash-*->order")).getOrElse(Nil) should equal(List(Some("2"), Some("3"), Some("1")))
      r.sort("alltest", None, false, false, None, List("hash-*->description")).getOrElse(Nil) should equal(List(Some("one"), Some("two"), Some("three")))
      r.sort("alltest", None, false, false, None, List("hash-*->description", "hash-*->order")).getOrElse(Nil) should equal(List(Some("one"), Some("100"), Some("two"), Some("25"), Some("three"), Some("50")))
    }
  }

  describe("ping") {
    it("should pong") {
      r.ping match {
        case Some(s: String) => s should equal("PONG")
        case None => fail("should return PONG")
      }
    }
  }

  describe("echo") {
    it("should give") {
      r.echo("it's me") match {
        case Some(s: String) => s should equal("it's me")
        case None => fail("should return it's me")
      }
    }
  }

  describe("scan") {
    it("should give all keys") {
      r.set("testkey", "testvalue")

      var scanCount = 0
      r.scan(){
        keys =>
          assert(keys.size > 0)
          scanCount += 1
      }
      scanCount should equal (1)
    }
  }

  describe("pattern match scan") {
    it("should give all matched keys") {
      r.set("testkey", "testvalue")
      r.set("testkey2", "testvalue")
      r.set("testkey22", "testvalue")
      r.set("testkey3", "testvalue")

      var scanCount = 0
      r.scan(Some("*key2*")){
        keys =>
          keys.size should equal (2)
          scanCount += 1
      }
      scanCount should equal (1)
    }
  }

  describe("scan with limit") {
    it("should give numer of limited keys") {
      r.set("testkey", "testvalue")
      r.set("testkey2", "testvalue")
      r.set("testkey22", "testvalue")
      r.set("testkey3", "testvalue")

      var scanCount = 0
      r.scan(Some("*key*"), Some(2)){
        keys =>
          keys.size should equal (2)
          scanCount += 1
      }
      scanCount should equal (1)
    }
  }

  describe("scan non-exist key pattern") {
    it("should give empty list") {
      var scanCount = 0
      r.scan(Some("impossibleToHave")){
        keys =>
          keys.size should equal (0)
          scanCount += 1
      }
      scanCount should equal (1)
    }
  }

  describe("hscan") {
    it("should give all keys") {
      val hashkey = "testhkey"
      (0 until 100).foreach(i => r.hset(hashkey, s"key${i}", s"testvalue${i}"))

      val resp = r.hscan(hashkey)
      resp.scanStatus should equal (ScanStatus.Finished)
      resp.result.size should equal(100)
    }
  }

  describe("hscan while hash doesn't exist") {
    it("should nil") {
      val hashkey = "testhkey"
      r.del(hashkey)
      r.exists(hashkey) should be (false)

      val resp = r.hscan(hashkey)
      resp.scanStatus should equal (ScanStatus.Finished)
      resp.result.size should equal(0)
    }
  }

  describe("hscan with pattern match") {
    it("should give all keys which match the specified pattern") {
      val hashkey = "testhkey"
      (0 until 100).foreach(i => r.hset(hashkey, s"key${i}", s"testvalue${i}"))

      val resp = r.hscan(hashkey, pattern = Some("key1*"))
      resp.scanStatus should equal (ScanStatus.Finished)
      resp.result.size should equal(11)
      resp.result.map(_._1).toSet should equal (Set("key19", "key18", "key17", "key16", "key15", "key14", "key13", "key12", "key11", "key10", "key1"))
    }
  }

  describe("hscan with batch size") {
    it("should give the size of keys in one batch") {
      val hashkey = "testhkey"
      (0 until 100).foreach(i => r.hset(hashkey, s"key${i}", s"testvalue${i}"))

      val resp = r.hscan(hashkey, batchSize = Some(20))
      (resp.result.size >= 20) should be(true)
    }
  }
}
