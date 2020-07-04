import utils.STUtils

object ScalaMain {

  def main(args: Array[String]): Unit = {
    val sto = STUtils.createSTObject(111, 222, 33L)
    println(STUtils.getX(sto))
    println(STUtils.getY(sto))
    println(STUtils.getTime(sto))
  }

}
