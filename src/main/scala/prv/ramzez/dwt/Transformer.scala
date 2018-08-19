package prv.ramzez.dwt

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.opencv_core.{Mat, Point}
import org.bytedeco.javacpp.{opencv_core, opencv_imgproc}

object Transformer {
}

case class Transformer(filter: Filter) {

  private def downscaleColumns(dest: Mat): Mat = scaleColumns(dest, 0.5)

  private def upscaleColumns(dest: Mat): Mat = {
    val columns = 0 until dest.cols() * 2
    val z = Mat.zeros(dest.col(0).rows(), 1, dest.`type`()).asMat()
    val a = columns.map(r => get(dest, r, x => dest.col(x), z)).reduce { (a, b) => a.push_back(b); a }
    val result =  a.reshape(0, columns.length).t().asMat()
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
    result
  }

  private def scaleRows(dest: Mat, factor: Double): Mat = {
    val rows = 0 until (dest.rows() * factor).round.toInt
    rows.map(r => dest.row((r / factor).toInt)).reduce { (a, b) => a.push_back(b); a }
  }

  private val anchor = new Point(-1, -1)
  private val delta = 0.0
  private val borderType = opencv_core.BORDER_DEFAULT

  def getColRange(img: Mat, start: Int, size: Int): Mat = {
    val result = new Mat
    for (i <- start until (start+size)) {
      result.push_back(img.col(i % img.cols()))
    }
    result.reshape(0,size).t().asMat()
  }

  def filterRow(img: Mat, kernel: Mat) = {
    val result,tmp = new Mat()
    for (i <- 0 until img.cols()) {
      opencv_core.gemm(getColRange(img,i,2), kernel, 1, new Mat(), 0, tmp)
      result.push_back(tmp)
    }
    result.reshape(0,img.rows()).t().asMat()
  }

  def getRowRange(img: Mat, start: Int, size: Int): Mat = {
    val result = new Mat
    for (i <- start until (start+size)) {
      result.push_back(img.row(i % img.rows()))
    }
    result
  }

  def filterColumn(img: Mat, kernel: Mat) = {
    val result,tmp = new Mat()
    for (i <- 0 until img.rows()) {
      opencv_core.gemm(kernel, getRowRange(img,i,2), 1, new Mat(), 0, tmp)
      result.push_back(tmp)
    }
    result
  }

  def decomposeStep(img: Mat): DwtStep = {
    val low = downscaleColumns(filterRow(img,filter.rowLow))
    val high = downscaleColumns(filterRow(img,filter.rowHigh))

    val ll = downscaleRows(filterColumn(low,filter.columnLow))
    val lh = downscaleRows(filterColumn(low,filter.columnHigh))
    val hl = downscaleRows(filterColumn(high,filter.columnLow))
    val hh = downscaleRows(filterColumn(high,filter.columnHigh))

    DwtStep(hh, hl, lh, ll)
  }

  def recomposeStep(dwtStep: DwtStep): Mat = {
    val hh = upscaleRows(dwtStep.HH)
    //opencv_imgproc.filter2D(hh, high_1, -1, filter.columnHigh, anchor, delta, borderType)
    val high_1 = filterColumn(hh,filter.columnHigh)
    val hl = upscaleRows(dwtStep.HL)
    //opencv_imgproc.filter2D(hl, high_2, -1, filter.columnLow, anchor, delta, borderType)
    val high_2 = filterColumn(hl,filter.columnLow)
    val high = upscaleColumns(opencv_core.add(high_1, high_2).asMat())


    val lh = upscaleRows(dwtStep.LH)
    //opencv_imgproc.filter2D(lh, low_1, -1, filter.columnHigh, anchor, delta, borderType)
    val low_1 = filterColumn(lh,filter.columnHigh)
    val ll = upscaleRows(dwtStep.LL)
    //opencv_imgproc.filter2D(ll, low_2, -1, filter.columnLow, anchor, delta, borderType)
    val low_2 = filterColumn(ll,filter.columnLow)
    val low = upscaleColumns(opencv_core.add(low_1, low_2).asMat())

    //opencv_imgproc.filter2D(high, img_1, -1, filter.rowHigh, anchor, delta, borderType)
    val img_1 = filterRow(high,filter.rowHigh)
    //opencv_imgproc.filter2D(low, img_2, -1, filter.rowLow, anchor, delta, borderType)
    val img_2 = filterRow(low,filter.rowLow)
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