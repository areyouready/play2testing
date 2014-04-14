package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Disc(id: Long, label: String)
	
object Disc {
	val disc = {
		get[Long]("id") ~
		get[String]("label") map {
			case id~label => Disc(id, label)
		}
	}
	def all(): List[Disc] = DB.withConnection { implicit c =>
		SQL("select * from disc").as(disc *)
	}
	def create(label: String) {
		DB.withConnection { implicit c => 
			SQL("Insert into disc (label) values ({label})").on(
				'label -> label
			).executeUpdate()
		}
	}
	def delete(id: Long) {
		DB.withConnection { implicit c => 
			SQL("delete from disc where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}
}
