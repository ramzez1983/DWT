package prv.ramzez.dwt

import org.bytedeco.javacpp.opencv_core.Mat

case class Wavelet(columnLow: Mat, columnHigh: Mat) {
  lazy val reversedColumnLow: Mat = reverseCol(columnLow)
  lazy val reversedColumnHigh: Mat = reverseCol(columnHigh)
  lazy val rowHigh: Mat = transpose(columnHigh)
  lazy val rowLow: Mat = transpose(columnLow)
  lazy val reversedRowHigh: Mat = transpose(reversedColumnHigh)
  lazy val reversedRowLow: Mat = transpose(reversedColumnLow)

  private def transpose(filter: Mat): Mat = filter.t().asMat()

  private def reverseCol(filter: Mat): Mat = {
    val columns = (0 until filter.cols()).reverse
    val result = columns.map(r => filter.col(r)).reduce { (a, b) => a.push_back(b); a }.reshape(0, columns.length).t().asMat()
    result
  }

}

object Wavelet {
  private val s3 = Math.sqrt(3)
  private val s2 = Math.sqrt(2)
  val d4 = Wavelet(List((1 + s3) / 4 / s2, (3 + s3) / 4 / s2, (3 - s3) / 4 / s2, (1 - s3) / 4 / s2),
    List((1 - s3) / 4 / s2, -1 * (3 - s3) / 4 / s2, (3 + s3) / 4 / s2, -1 * (1 + s3) / 4 / s2))

  val haar = Wavelet(List(1 / s2, 1 / s2), List(1 / s2, -1 / s2))

}
