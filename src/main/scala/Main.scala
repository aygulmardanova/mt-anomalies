import utils.STObjectUtils

object Main {

  def main(args: Array[String]): Unit = {
    val sto = STObjectUtils.createSTObject(111, 222, 33L)
    println(STObjectUtils.getX(sto))
    println(STObjectUtils.getY(sto))
    println(STObjectUtils.getTime(sto))
  }

}
