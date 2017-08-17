/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.jcastro.delfos.scala.common

/**
  * Clase que encapsula el funcionamiento de un reloj. Permite obtener el tiempo
  * desde que se reinición y el tiempo desde que se marcó el último tiempo
  * parcial.
  *
  * @author jcastro-inf ( https://github.com/jcastro-inf )
  * @version 1.0 Unknown date
  * @version 1.1 20-Mar-2013
  */
class Chronometer() {

  private var previousTime = System.currentTimeMillis()
  private var initTime = previousTime

  /**
    * Reinicia el cronómetro para volver a contar el tiempo desde el punto en
    * el que se reinició.
    */
  final def reset() = {
    previousTime = System.currentTimeMillis
    initTime = previousTime
  }

  /**
    * Establece el punto de control al tiempo actual.
    */
  final def setPartialEllapsedCheckpoint() = {
    previousTime = System.currentTimeMillis
  }

  /**
    * Devuelve el tiempo que ha pasado desde que se reinició el cronómetro como
    * una cadena en la que se muestra el tiempo pasado
    *
    * @return Devuelve una cadena con el tiempo total transcurrido
    * @see DateCollapse#collapse(long)
    */
  def printTotalElapsed: String = {
    val d = System.currentTimeMillis - initTime
    DateCollapse.collapse(d)
  }

  /**
    * Devuelve el número de milisegundos que han transcurrido desde el inicio
    *
    * @return milisegundos que han transcurrido en total
    */
  def getTotalElapsed: Long = System.currentTimeMillis - initTime

  /**
    * Devuelve el tiempo que ha pasado desde que se marcó un tiempo parcial
    * como una cadena en la que se muestra el tiempo pasado
    *
    * @return Devuelve una cadena con el tiempo parcial transcurrido
    * @see DateCollapse#collapse(long)
    */
  def printPartialElapsed: String = {
    val d = System.currentTimeMillis - previousTime
    DateCollapse.collapse(d)
  }

  /**
    * Devuelve el número de milisegundos que han transcurrido desde el último
    * tiempo parcial
    *
    * @return milisegundos que han transcurrido desde el último tiempo parcial
    */
  def getPartialElapsed: Long = {
    val actual = System.currentTimeMillis
    val ret = actual - previousTime
    ret
  }
}
