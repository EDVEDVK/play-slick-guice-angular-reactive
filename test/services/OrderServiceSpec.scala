package services

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import fixtures.ServiceSpec
import maps.Maps._
import maps.OrderMap
import org.joda.time.DateTime

class OrderServiceSpec extends ServiceSpec {

	import dbConfig.driver.api._
	lazy val service = app.injector.instanceOf(classOf[IOrderService])

	"getOrders() method" should {
		"return the list of orders" in {
			val reset = db.run {
				sqlu"""
					TRUNCATE TABLE "order";
				   	INSERT INTO "order" (id, date, name, description) VALUES (1, '2015-11-17 00:00:00.000000', 'name', 'description');
				"""
			}
			Await.result(reset, 2 seconds)
			val result = service.getOrders
			val orders = Await.result(result, 2 seconds)
			orders.size mustBe 1
			orders.head.description mustEqual Some("description")
		}
	}

	"upsertOrder() method" should {
		"update the order" in {
			val reset = db.run {
				sqlu"""
					TRUNCATE TABLE "order";
				   	INSERT INTO "order" (id, date, name, description) VALUES (1, '2015-11-17 00:00:00.000000', 'name', 'description');
				"""
			}
			Await.result(reset, 2 seconds)
			val order = OrderMap(Some(1), Some(DateTime.now()), "name", Some("description1"))
			val result = Await.result(service.upsertOrder(order), 2 seconds)
			result mustEqual 1
			val upsertedOrderFuture = db.run { sql"""SELECT * FROM "order" WHERE id = 1""".as[OrderMap] }
			val upsertedOrder = Await.result(upsertedOrderFuture, 2 seconds)
			upsertedOrder.size mustEqual 1
			upsertedOrder(0).description mustEqual Some("description1")
		}
		"insert a new order" in {
			val reset = db.run { sqlu"""TRUNCATE TABLE "order"""" }
			Await.result(reset, 2 seconds)
			val order = OrderMap(None, Some(DateTime.now()), "name2", Some("description2"))
			val result = Await.result(service.upsertOrder(order), 2 seconds)
			result mustEqual 1
			val upsertedOrderFuture = db.run { sql"""SELECT * FROM "order"""".as[OrderMap] }
			val upsertedOrder = Await.result(upsertedOrderFuture, 2 seconds)
			upsertedOrder.size mustEqual 1
			upsertedOrder(0).description mustEqual Some("description2")
		}
	}

}