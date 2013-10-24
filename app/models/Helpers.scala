package models

import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import org.joda.time.DateTime
import scala.reflect.runtime.{universe => ru}
import com.github.tototoshi.slick.JodaSupport._
import play.api.libs.json.Json

/**
 * Helper for otherwise verbose Slick model definitions
 */
trait CRUDSuperPowers[T <: AnyRef {
  val id : Option[Long]
  val createdAt : Option[DateTime]
  val updatedAt : Option[DateTime]
}] {
  self: Table[T] =>

  def id: Column[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def createdAt: Column[Option[DateTime]] = column[Option[DateTime]]("createdAt")

  def updatedAt: Column[Option[DateTime]] = column[Option[DateTime]]("updatedAt")

  //  def * : scala.slick.lifted.ColumnBase[T]

  def autoInc = * returning id

  def base = id.? ~ createdAt ~ updatedAt

  def insert(entity: T): Long = {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        autoInc.insert(entity)
    }
  }

  def insertAndReturn(entity: T): Option[T] = {
    val k = insert(entity)
    findById(k)
  }

  def insertAll(entities: Seq[T]) {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        autoInc.insertAll(entities: _*)
    }
  }

  def update(id: Long, entity: T) {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        tableQueryToUpdateInvoker(
          tableToQuery(this).where(_.id === id)
        ).update(entity)
    }
  }

  def delete(id: Long) {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        queryToDeleteInvoker(
          tableToQuery(this).where(_.id === id)
        ).delete
    }
  }

  def deleteAll {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        queryToDeleteInvoker(
          tableToQuery(this)
        ).delete
    }
  }

  def count = DB.withSession {
    implicit session: scala.slick.session.Session =>
      Query(tableToQuery(this).length).first
  }


  def findById(pk: Long): Option[T] = {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        (for {
          a <- tableToQuery(this) if a.id === pk
        } yield a).list.headOption
    }
  }

  def findAll: List[T] = {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
      // FIXME check if need for iterator
        (for {
          a <- tableToQuery(this)
        } yield a).list
    }
  }

  def apply(id: Long): Option[T] = {
    findById(id)
  }

}


object JsonSerializers {
  implicit val comw = Json.writes[models.Comment]
}