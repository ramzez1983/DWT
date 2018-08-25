package prv.ramzez.dwt

import com.typesafe.scalalogging.LazyLogging
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.Mat

//TODO switch everything to opencv_core.CV_64F double precision
case class Transformer(filter: Wavelet) extends LazyLogging with RangeFilter with WrapBorder {

  private def get(r: Int, g: Int => Mat, z: Mat) = {
    if (Math.floorMod(r, 2) != 0) g(Math.floorDiv(r, 2))
    else z.clone()
  }

  private def upscaleColumns(dest: Mat): Mat = {
    val columns = 0 until dest.cols() * 2
    val z = Mat.zeros(dest.col(0).rows(), 1, dest.`type`()).asMat()
    val a = columns.map(r => get(r, x => dest.col(x), z)).reduce { (a, b) => a.push_back(b); a }
    val result = a.reshape(0, columns.length).t().asMat()
    logger.debug(s"upscaled columns of ${printMat(dest)} to ${printMat(result)}")
    result
  }

  private def downscaleColumns(dest: Mat): Mat = scaleColumns(dest, 0.5)

  private def scaleColumns(dest: Mat, factor: Double): Mat = {
    val columns = 0 until (dest.cols() * factor).round.toInt
    val list = columns.map(r => dest.col((r / factor).toInt))
    list.reduce { (a, b) => a.push_back(b); a }.reshape(0, columns.length).t().asMat()
  }

  private def upscaleRows(dest: Mat): Mat = {
    val rows = 0 until dest.rows() * 2
    val z = Mat.zeros(1, dest.row(0).cols(), dest.`type`()).asMat()
    val result = rows.map(r => get(r, x => dest.row(x), z)).reduce { (a, b) => a.push_back(b); a }
    logger.debug(s"upscaled rows of ${printMat(dest)} to ${printMat(result)}")
    result
  }

  private def downscaleRows(dest: Mat): Mat = scaleRows(dest, 0.5)

  private def scaleRows(dest: Mat, factor: Double): Mat = {
    val rows = 0 until (dest.rows() * factor).round.toInt
    rows.map(r => dest.row((r / factor).toInt)).reduce { (a, b) => a.push_back(b); a }
  }

  def recomposeStep(dwtStep: Dwt2DStep): Mat = {
    logger.debug(s"recompose step started")
    val hh = upscaleRows(dwtStep.hh)
    val high_1 = filterColumn(hh, filter.reversedColumnHigh)
    val hl = upscaleRows(dwtStep.hl)
    val high_2 = filterColumn(hl, filter.reversedColumnLow)
    val high = upscaleColumns(opencv_core.add(high_1, high_2).asMat())

    val lh = upscaleRows(dwtStep.lh)
    val low_1 = filterColumn(lh, filter.reversedColumnHigh)
    val ll = upscaleRows(dwtStep.ll)
    val low_2 = filterColumn(ll, filter.reversedColumnLow)
    val low = upscaleColumns(opencv_core.add(low_1, low_2).asMat())

    val img_1 = filterRow(high, filter.reversedRowHigh)
    val img_2 = filterRow(low, filter.reversedRowLow)
    val result = opencv_core.add(img_1, img_2).asMat()
    logger.debug(s"result is ${printMat(result)}")
    result
  }

  def decomposeStep(img: Mat): Dwt2DStep = {
    logger.debug(s"decompose step started")
    val low = downscaleColumns(filterRow(img, filter.rowLow))
    val high = downscaleColumns(filterRow(img, filter.rowHigh))

    val ll = downscaleRows(filterColumn(low, filter.columnLow))
    val lh = downscaleRows(filterColumn(low, filter.columnHigh))
    val hl = downscaleRows(filterColumn(high, filter.columnLow))
    val hh = downscaleRows(filterColumn(high, filter.columnHigh))

    Dwt2DStep(hh, hl, lh, ll)
  }


  def decompose(img: Mat, level: Int): DwtStructure = {
    val floatMat = new Mat()
    img.convertTo(floatMat, opencv_core.CV_32F)
    val splitted = matSplit(floatMat)
    val splittedResult = splitted.map(i => decompose(i, level, List()))
    splittedResult.transpose.map(l => Dwt2DStep.mergeChannels(l))
  }

  def decompose(img: Mat, level: Int, structure: DwtStructure): DwtStructure = {
    if (level <= 0) structure
    else {
      val step = decomposeStep(img)
      decompose(step.ll, level - 1, structure :+ step)
    }
  }
}