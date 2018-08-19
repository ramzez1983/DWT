package prv.ramzez.dwt

import org.bytedeco.javacpp.opencv_core.{Mat, Point}
import org.bytedeco.javacpp.{opencv_core, opencv_imgproc}

object Transformer {
}

case class Transformer(filter: Filter) {

  private def downscaleColumns(dest: Mat): Mat = scaleColumns(dest, 0.5)

  private def upscaleColumns(dest: Mat): Mat = {
    val columns = 0 until dest.cols() * 2
    val z = Mat.zeros(dest.col(0).rows(), 1, dest.`type`()).asMat()
    val result = columns.map(r => get(dest, r, x => dest.col(x), z)).reduce { (a, b) => a.push_back(b); a }.reshape(0, columns.length).t().asMat()
    result
  }

  private def get(dest: Mat, r: Int, g: Int => Mat, z: Mat) = {
    if (Math.floorMod(r, 2) == 0) g(Math.floorDiv(r, 2))
    else z
  }

  private def scaleColumns(dest: Mat, factor: Double): Mat = {
    val columns = 0 until (dest.cols() * factor).round.toInt
    columns.map(r => dest.col((r / factor).toInt)).reduce { (a, b) => a.push_back(b); a }.reshape(0, columns.length).t().asMat()
  }

  private def downscaleRows(dest: Mat): Mat = scaleRows(dest, 0.5)

  private def upscaleRows(dest: Mat): Mat = {
    val rows = 0 until dest.rows() * 2
    val z = Mat.zeros(1, dest.row(0).cols(), dest.`type`()).asMat()
    val result = rows.map(r => get(dest, r, x => dest.row(x), z)).reduce { (a, b) => a.push_back(b); a }
    result
  }

  private def scaleRows(dest: Mat, factor: Double): Mat = {
    val rows = 0 until (dest.rows() * factor).round.toInt
    rows.map(r => dest.row((r / factor).toInt)).reduce { (a, b) => a.push_back(b); a }
  }

  private val anchor = new Point(-1, -1)
  private val delta = 0.0
  private val borderType = opencv_core.BORDER_DEFAULT

  def decomposeStep(img: Mat): DwtStep = {
    val low, high, ll, lh, hl, hh = new Mat()

    opencv_imgproc.filter2D(img, low, -1, filter.rowLow, anchor, delta, borderType)
    val low_2 = downscaleColumns(low)
    opencv_imgproc.filter2D(img, high, -1, filter.rowHigh, anchor, delta, borderType)
    val high_2 = downscaleColumns(high)

    opencv_imgproc.filter2D(low_2, ll, -1, filter.columnLow, anchor, delta, borderType)
    val ll_2 = downscaleRows(ll)
    opencv_imgproc.filter2D(low_2, lh, -1, filter.columnHigh, anchor, delta, borderType)
    val lh_2 = downscaleRows(lh)

    opencv_imgproc.filter2D(high_2, hl, -1, filter.columnLow, anchor, delta, borderType)
    val hl_2 = downscaleRows(hl)
    opencv_imgproc.filter2D(high_2, hh, -1, filter.columnHigh, anchor, delta, borderType)
    val hh_2 = downscaleRows(hh)

    DwtStep(hh_2, hl_2, lh_2, ll_2)
  }

  def recomposeStep(dwtStep: DwtStep): Mat = {
    val high_1, high_2, low_1, low_2, img_1, img_2 = new Mat()
    val hh = upscaleRows(dwtStep.HH)
    opencv_imgproc.filter2D(hh, high_1, -1, filter.columnHigh, anchor, delta, borderType)
    val hl = upscaleRows(dwtStep.HL)
    opencv_imgproc.filter2D(hl, high_2, -1, filter.columnLow, anchor, delta, borderType)
    val high = upscaleColumns(opencv_core.add(high_1, high_2).asMat())


    val lh = upscaleRows(dwtStep.LH)
    opencv_imgproc.filter2D(lh, low_1, -1, filter.columnHigh, anchor, delta, borderType)
    val ll = upscaleRows(dwtStep.LL)
    opencv_imgproc.filter2D(ll, low_2, -1, filter.columnLow, anchor, delta, borderType)
    val low = upscaleColumns(opencv_core.add(low_1, low_2).asMat())

    opencv_imgproc.filter2D(high, img_1, -1, filter.rowHigh, anchor, delta, borderType)
    opencv_imgproc.filter2D(low, img_2, -1, filter.rowLow, anchor, delta, borderType)
    opencv_core.add(img_1, img_2).asMat()
  }

  def decompose(img: Mat, level: Int): DwtStructure = {
    decompose(img, level, List())
  }

  def decompose(img: Mat, level: Int, structure: DwtStructure): DwtStructure = {
    if (level <= 0) structure
    else {
      val step = decomposeStep(img)
      decompose(step.LL, level - 1, structure :+ step)
    }
  }
}