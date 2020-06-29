package utils

import dbis.stark.STObject.GeoType
import dbis.stark.STObject
import org.locationtech.jts.geom.{Coordinate, Geometry, GeometryFactory}

object STObjectUtils {

  def createPoint(x: Long, y: Long): GeoType = {
    new GeometryFactory().createPoint(new Coordinate(x,y))
  }

  def createSTObject(x: Long, y: Long): STObject = {
    STObject(x, y)
  }

  def createSTObject(x: Long, y: Long, t: Long): STObject = {
    STObject(x, y, t)
  }

  def createSTObject(g: GeoType, t: Long): STObject = {
    STObject(g, t)
  }

  def getX(sto: STObject): Long = {
    sto.g.getCoordinate.x.toLong
  }

  def getY(sto: STObject): Long = {
    sto.g.getCoordinate.y.toLong
  }

  def getTime(sto: STObject): Long = {
    sto.time.get.start.value
  }
}
