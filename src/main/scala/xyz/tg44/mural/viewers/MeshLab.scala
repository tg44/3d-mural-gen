package xyz.tg44.mural.viewers

import java.io.File

import xyz.tg44.mural.utils.SysCmd

case class MeshLab() extends Viewer {

  def apply(stl: String, options: Iterable[String] = Nil): SysCmd.ExecResult = {
    SysCmd( Array("meshlab", stl) ++ options )
  }

  def view(file: File): Unit = apply(file.getPath)

  lazy val isPresent = {
    val isWindows = java.lang.System.getProperty("os.name").toLowerCase().contains("windows")
    val cmd = if (isWindows) "where" else "which"
    SysCmd(Array(cmd, "meshlab"))._1 == 0
  }
}
