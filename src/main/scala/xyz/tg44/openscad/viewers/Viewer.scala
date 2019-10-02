package xyz.tg44.openscad.viewers

import java.io.File

trait Viewer {
  def view(file: File): Unit
}
