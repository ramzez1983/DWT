package prv.ramzez.dwt

import com.typesafe.scalalogging.LazyLogging
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.{Mat, Point}

object Transformer {
}

case class Transformer(filter: Filter) extends LazyLogging {

  private def downscaleColumns(dest: Mat): Mat = scaleColumns(dest, 0.5)

  private def upscaleColumns(dest: Mat): Mat = {
    val columns = 0 until dest.cols() * 2
    val z = Mat.zeros(dest.col(0).rows(), 1, dest.`type`()).asMat()
    val a = columns.map(r => get(dest, r, x => dest.col(x), z)).reduce { (a, b) => a.push_back(b); a }
    val result =  a.reshape(0, columns.length).t().asMat()
    logger.debug(s"upscaled columns of ${printMat(dest)} to ${printMat(result)}")
    result
  }

  private def get(dest: Mat, r: Int, g: Int => Mat, z: Mat) = {
    if (Math.floorMod(r, 2) != 0) g(Math.floorDiv(r, 2))
    else z.clone()
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
    logger.debug(s"upscaled rows of ${printMat(dest)} to ${printMat(result)}")
    result
  }

  private def scaleRows(dest: Mat, factor: Double): Mat = {
    val rows = 0 until (dest.rows() * factor).round.toInt
    rows.map(r => dest.row((r / factor).toInt)).reduce { (a, b) => a.push_back(b); a }
  }

  private val anchor = new Point(-1, -1)
  private val delta = 0.0
  private val borderType = opencv_core.BORDER_DEFAULT

  def recomposeStep(dwtStep: DwtStep): Mat = {
    logger.debug(s"recompose step started")
    val hh = upscaleRows(dwtStep.HH)
    val high_1 = filterColumn(hh, filter.reversedColumnHigh)
    val hl = upscaleRows(dwtStep.HL)
    val high_2 = filterColumn(hl, filter.reversedColumnLow)
    val high = upscaleColumns(opencv_core.add(high_1, high_2).asMat())

    val lh = upscaleRows(dwtStep.LH)
    val low_1 = filterColumn(lh, filter.reversedColumnHigh)
    val ll = upscaleRows(dwtStep.LL)
    val low_2 = filterColumn(ll, filter.reversedColumnLow)
    val low = upscaleColumns(opencv_core.add(low_1, low_2).asMat())

    val img_1 = filterRow(high, filter.reversedRowHigh)
    val img_2 = filterRow(low, filter.reversedRowLow)
    val result = opencv_core.add(img_1, img_2).asMat()
    logger.debug(s"result is ${printMat(result)}")
    result
  }

  def filterRow(img: Mat, kernel: Mat): Mat = {
    val r, tmp = new Mat()
    val step = kernel.rows()
    val mod = step / 2 - 1
    for (i <- 0 - mod until img.cols() - mod) {
      opencv_core.gemm(getColRange(img, i, step), kernel, 1, new Mat(), 0, tmp)
      r.push_back(tmp)
    }
    val result = r.reshape(0, img.rows()).t().asMat()
    logger.debug(s"filtered rows of ${printMat(img)} to ${printMat(result)}")
    result
  }

  def getColRange(img: Mat, start: Int, size: Int): Mat = {
    val result = new Mat
    for (i <- start until (start+size)) {
      result.push_back(img.col(withBorder(i, img.cols())))
    }
    result.reshape(0, size).t().asMat()
  }

  private def withBorder(i: Int, max: Int) = {
    val r = Math.floorMod(i, max)
    //val r = if (i < 0) -i else if (i>=max)  max - (i - max) - 2 else i
    //val r = if (i < 0) 0 else if (i>=max)  max-1 else i
    r
  }

  def filterColumn(img: Mat, kernel: Mat): Mat = {
    //opencv_imgproc.filter2D(hh, high_1, -1, filter.columnHigh, anchor, delta, borderType)
    val result,tmp = new Mat()
    val step = kernel.cols()
    val mod = step / 2 - 1
    for (i <- 0 - mod until img.rows() - mod) {
      opencv_core.gemm(kernel, getRowRange(img, i, step), 1, new Mat(), 0, tmp)
      result.push_back(tmp)
    }
    logger.debug(s"filtered columns of ${printMat(img)} to ${printMat(result)}")
    result
  }

  def decomposeStep(img: Mat): DwtStep = {
    logger.debug(s"decompose step started")
    val low = downscaleColumns(filterRow(img,filter.rowLow))
    val high = downscaleColumns(filterRow(img,filter.rowHigh))

    val ll = downscaleRows(filterColumn(low,filter.columnLow))
    val lh = downscaleRows(filterColumn(low,filter.columnHigh))
    val hl = downscaleRows(filterColumn(high,filter.columnLow))
    val hh = downscaleRows(filterColumn(high,filter.columnHigh))

    DwtStep(hh, hl, lh, ll)
  }

  def getRowRange(img: Mat, start: Int, size: Int): Mat = {
    val result = new Mat
    for (i <- start until (start + size)) {
      result.push_back(img.row(withBorder(i, img.rows())))
    }
    result
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