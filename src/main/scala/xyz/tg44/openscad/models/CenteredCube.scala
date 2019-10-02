package xyz.tg44.openscad.models

import squants.Length
import xyz.tg44.openscad.core.Solids._
import squants.space.LengthConversions._
import xyz.tg44.openscad.core.Renderable

object CenteredCube {

  def apply(x: Length, y: Length, z:Length)(implicit ev: Renderable[Cube]) = Translate(-x/2, -y/2, -z/2, Cube(x,y,z))

  def xy(x: Length, y: Length, z:Length)(implicit ev: Renderable[Cube]) = Translate(-x/2, -y/2, 0 mm, Cube(x,y,z))

  def xz(x: Length, y: Length, z:Length)(implicit ev: Renderable[Cube]) = Translate(-x/2, 0 mm, -z/2, Cube(x,y,z))

  def yz(x: Length, y: Length, z:Length)(implicit ev: Renderable[Cube]) = Translate(0 mm, -y/2, -z/2, Cube(x,y,z))

  def x(x: Length, y: Length, z:Length)(implicit ev: Renderable[Cube]) = Translate(-x/2, 0 mm, 0 mm, Cube(x,y,z))

  def y(x: Length, y: Length, z:Length)(implicit ev: Renderable[Cube]) = Translate(0 mm, -y/2, 0 mm, Cube(x,y,z))

  def z(x: Length, y: Length, z:Length)(implicit ev: Renderable[Cube]) = Translate(0 mm, 0 mm, -z/2, Cube(x,y,z))

}
