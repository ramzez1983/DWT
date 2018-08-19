package prv.ramzez.dwt

import org.bytedeco.javacpp.opencv_core.Mat

case class Filter(columnLow: Mat, columnHigh: Mat) {
  lazy val rowHigh: Mat = transpose(columnHigh)
  lazy val rowLow: Mat = transpose(columnLow)

  private def transpose(filter: Mat): Mat = filter.t().asMat()


}

object Filter {
  private val s2: Double = Math.sqrt(2)
  val haar = Filter(List(1 / s2, 1 / s2), List(1 / s2, -1 / s2))
}
