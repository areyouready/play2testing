package models

case class Disc(id: Long, label: String)
	
object Disc {
	def all(): List[Disc] = Nil
	def create(label: String) {}
	def delete(id: Long) {}
}
