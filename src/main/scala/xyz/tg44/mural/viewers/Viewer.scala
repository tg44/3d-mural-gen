package xyz.tg44.mural.viewers

import java.io.File

trait Viewer {
  def view(file: File): Unit
}
