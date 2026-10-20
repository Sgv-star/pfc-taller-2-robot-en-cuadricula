package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RobotCuadriculaTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  val cuadrado = Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))

  // Punto 1 -------------------------------------------------------------------

  test("girar: Norte a la derecha da Este") { assert(girar(Norte, Derecha) == Este) }
  test("girar: Norte a la izquierda da Oeste") { assert(girar(Norte, Izquierda) == Oeste) }
  test("girar: Oeste a la derecha da Norte") { assert(girar(Oeste, Derecha) == Norte) }
  test("girar: Sur a la izquierda da Este") { assert(girar(Sur, Izquierda) == Este) }

  test("girar: cuatro giros al mismo lado vuelven al principio") {
    val cuatro = List(Derecha, Derecha, Derecha, Derecha)
      .foldLeft(Norte: Direccion)((d, s) => girar(d, s))
    assert(cuatro == Norte)
  }

  test("girar: un giro y su contrario se cancelan") {
    assert(girar(girar(Este, Derecha), Izquierda) == Este)
  }
  test("girar: Este a la derecha da Sur") { assert(girar(Este, Derecha) == Sur) }
  test("girar: Este a la izquierda da Norte") { assert(girar(Este, Izquierda) == Norte) }
  test("girar: Sur a la derecha da Oeste") { assert(girar(Sur, Derecha) == Oeste) }
  test("girar: Oeste a la izquierda da Sur") { assert(girar(Oeste, Izquierda) == Sur) }
  test("girar: dos giros a la derecha equivalen a dos a la izquierda") {
    assert(girar(girar(Norte, Derecha), Derecha) == girar(girar(Norte, Izquierda), Izquierda))
  }

  // Punto 2 -------------------------------------------------------------------

  test("ejecutar: avanzar tres al norte") {
    assert(ejecutar(inicial, Avanzar(3)) == Estado(0, 3, Norte))
  }

  test("ejecutar: una secuencia de avance, giro y avance") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(4)))) ==
      Estado(4, 2, Este))
  }

  test("ejecutar: el cuadrado vuelve al punto de partida") {
    assert(ejecutar(inicial, cuadrado) == Estado(0, 0, Norte))
  }

  test("ejecutar: avanzar con paso negativo retrocede") {
    assert(ejecutar(inicial, Avanzar(-2)) == Estado(0, -2, Norte))
  }

  test("ejecutar: repetir cero veces no hace nada") {
    assert(ejecutar(inicial, Repetir(0, Avanzar(5))) == inicial)
  }

  test("ejecutar: la secuencia vacía no hace nada") {
    assert(ejecutar(inicial, Secuencia(Nil)) == inicial)
  }

  test("ejecutar: avanzar al sur disminuye y") {
    assert(ejecutar(Estado(5, 5, Sur), Avanzar(2)) == Estado(5, 3, Sur))
  }

  test("ejecutar: avanzar al oeste disminuye x") {
    assert(ejecutar(Estado(5, 5, Oeste), Avanzar(2)) == Estado(3, 5, Oeste))
  }

  test("ejecutar: girar a la derecha desde el este mira al sur") {
    assert(ejecutar(inicial, Girar(Derecha)) == Estado(0, 0, Este) &&
      ejecutar(Estado(0, 0, Este), Girar(Derecha)) == Estado(0, 0, Sur))
  }

  test("ejecutar: girar a la izquierda desde el sur mira al este") {
    assert(ejecutar(Estado(2, 7, Sur), Girar(Izquierda)) == Estado(2, 7, Este))
  }

  test("ejecutar: dos repeticiones anidadas multiplican sus avances") {
    assert(ejecutar(inicial, Repetir(2, Repetir(3, Avanzar(2)))) == Estado(0, 12, Norte))
  }

  test("ejecutar: repetir una secuencia gira y avanza en cada vuelta") {
    val orden = Repetir(2, Secuencia(List(Girar(Derecha), Avanzar(3))))
    assert(ejecutar(inicial, orden) == Estado(3, -3, Sur))
  }

  test("ejecutar: repetir una cantidad negativa no mueve al robot") {
    assert(ejecutar(Estado(1, 2, Este), Repetir(-3, Avanzar(5))) == Estado(1, 2, Este))
  }

  // Punto 3 -------------------------------------------------------------------

  test("cuantasBasicas: una orden básica cuenta uno") {
    assert(cuantasBasicas(Avanzar(3)) == 1)
  }

  test("cuantasBasicas: el cuadrado ejecuta ocho básicas") {
    assert(cuantasBasicas(cuadrado) == 8)
  }

  test("cuantasBasicas: repetir cero veces no ejecuta nada") {
    assert(cuantasBasicas(Repetir(0, cuadrado)) == 0)
  }

  test("cuantasBasicas: la secuencia vacía no ejecuta nada") {
    assert(cuantasBasicas(Secuencia(Nil)) == 0)
  }

  test("profundidad: una orden básica tiene profundidad 1") {
    assert(profundidad(Avanzar(3)) == 1)
  }

  test("profundidad: una secuencia de básicas tiene profundidad 2") {
    assert(profundidad(Secuencia(List(Avanzar(1), Girar(Derecha)))) == 2)
  }

  test("profundidad: el cuadrado tiene profundidad 3") {
    assert(profundidad(cuadrado) == 3)
  }

  test("profundidad: la secuencia vacía tiene profundidad 1") {
    assert(profundidad(Secuencia(Nil)) == 1)
  }

  test("profundidad: manda la rama más honda") {
    assert(profundidad(Secuencia(List(Avanzar(1), cuadrado))) == 4)
  }

  test("cuantasBasicas: un giro cuenta como una básica") {
    assert(cuantasBasicas(Girar(Izquierda)) == 1)
  }

  test("cuantasBasicas: dos avances en secuencia suman dos") {
    assert(cuantasBasicas(Secuencia(List(Avanzar(2), Avanzar(3)))) == 2)
  }

  test("cuantasBasicas: repetir tres veces un giro da tres básicas") {
    assert(cuantasBasicas(Repetir(3, Girar(Derecha))) == 3)
  }

  test("cuantasBasicas: las secuencias anidadas suman todas sus básicas") {
    val anidada = Secuencia(List(
      Secuencia(List(Avanzar(1), Girar(Derecha))),
      Secuencia(List(Secuencia(List(Avanzar(2)))))))
    assert(cuantasBasicas(anidada) == 3)
  }

  test("cuantasBasicas: repetir una secuencia multiplica sus básicas") {
    assert(cuantasBasicas(Repetir(3, Secuencia(List(Avanzar(1), Girar(Izquierda))))) == 6)
  }

  test("profundidad: un giro es una orden básica") {
    assert(profundidad(Girar(Izquierda)) == 1)
  }

  test("profundidad: repetir una básica anida un nivel") {
    assert(profundidad(Repetir(5, Avanzar(2))) == 2)
  }

  test("profundidad: repetir una secuencia anida dos niveles") {
    assert(profundidad(Repetir(2, Secuencia(List(Avanzar(1), Girar(Derecha))))) == 3)
  }

  test("profundidad: una secuencia de secuencias vacías tiene profundidad 2") {
    assert(profundidad(Secuencia(List(Secuencia(Nil), Secuencia(Nil)))) == 2)
  }

  test("profundidad: manda la rama más honda aunque haya varias") {
    val orden = Secuencia(List(
      Repetir(2, Repetir(3, Avanzar(1))),
      Girar(Izquierda),
      Secuencia(List(Avanzar(4)))))
    assert(profundidad(orden) == 4)
  }

  // Punto 4 -------------------------------------------------------------------

  test("componer: deja las dos órdenes en secuencia") {
    assert(componer(Avanzar(1), Girar(Derecha)) ==
      Secuencia(List(Avanzar(1), Girar(Derecha))))
  }

  test("componer: ejecutar la compuesta es ejecutar una y después la otra") {
    assert(ejecutar(inicial, componer(Avanzar(2), Girar(Derecha))) ==
      ejecutar(ejecutar(inicial, Avanzar(2)), Girar(Derecha)))
  }

  test("rutina: currificada, el cuadrado de cuatro vuelve al origen") {
    val cuadradoDe = rutina(1)(Derecha) _
    assert(ejecutar(inicial, cuadradoDe(4)) == Estado(0, 0, Norte))
  }

  test("rutina: con los mismos argumentos construye el cuadrado del enunciado") {
    assert(rutina(1)(Derecha)(4) == cuadrado)
  }

  test("aplicarA: ejecuta la misma orden sobre varios estados") {
    assert(aplicarA(List(inicial, Estado(5, 5, Sur)), Avanzar(2)) ==
      List(Estado(0, 2, Norte), Estado(5, 3, Sur)))
  }

  test("aplicarA: sobre la lista vacía no hay nada que hacer") {
    assert(aplicarA(Nil, Avanzar(2)) == Nil)
  }

  test("transformar: duplicar los avances deja los giros como estaban") {
    val doble = transformar(cuadrado, {
      case Avanzar(n) => Avanzar(2 * n)
      case otra => otra
    })
    assert(doble == Repetir(4, Secuencia(List(Avanzar(2), Girar(Derecha)))))
  }

  test("transformar: con la identidad no cambia nada") {
    assert(transformar(cuadrado, (o: Orden) => o) == cuadrado)
  }

  test("componer: componer tres órdenes anida las secuencias a la izquierda") {
    val compuesta = componer(componer(Avanzar(1), Girar(Derecha)), Avanzar(2))
    assert(compuesta == Secuencia(List(
      Secuencia(List(Avanzar(1), Girar(Derecha))), Avanzar(2))))
  }

  test("componer: la compuesta ejecuta la primera dos veces y luego la segunda") {
    val compuesta = componer(Repetir(2, Avanzar(1)), Girar(Izquierda))
    assert(ejecutar(inicial, compuesta) == Estado(0, 2, Oeste))
  }

  test("rutina: repetir cero veces no produce ninguna vuelta") {
    assert(ejecutar(Estado(4, 1, Este), rutina(2)(Izquierda)(0)) == Estado(4, 1, Este))
  }

  test("rutina: dos pasos al norte y luego mirar al oeste") {
    assert(ejecutar(inicial, rutina(2)(Izquierda)(1)) == Estado(0, 2, Oeste))
  }

  test("aplicarA: cada estado avanza según su propia dirección") {
    val estados = List(Estado(0, 0, Norte), Estado(0, 0, Este), Estado(0, 0, Oeste))
    assert(aplicarA(estados, Avanzar(1)) ==
      List(Estado(0, 1, Norte), Estado(1, 0, Este), Estado(-1, 0, Oeste)))
  }

  test("aplicarA: un giro deja las posiciones y cambia las direcciones") {
    val estados = List(Estado(2, 3, Norte), Estado(2, 3, Sur))
    assert(aplicarA(estados, Girar(Derecha)) ==
      List(Estado(2, 3, Este), Estado(2, 3, Oeste)))
  }

  test("transformar: reemplaza también las básicas dentro de una secuencia") {
    val giros = transformar(Secuencia(List(Avanzar(1), Girar(Derecha))), {
      case Girar(Derecha) => Girar(Izquierda)
      case otra => otra
    })
    assert(giros == Secuencia(List(Avanzar(1), Girar(Izquierda))))
  }

  test("transformar: aplica f a las básicas de las repeticiones anidadas") {
    val todas = transformar(Repetir(2, Repetir(3, Avanzar(1))), {
      case Avanzar(n) => Avanzar(n + 1)
      case otra => otra
    })
    assert(todas == Repetir(2, Repetir(3, Avanzar(2))))
  }

  test("componer: con una secuencia vacía primero queda un anidamiento") {
    assert(componer(Secuencia(Nil), Avanzar(1)) ==
      Secuencia(List(Secuencia(Nil), Avanzar(1))))
  }

  test("componer: la segunda no se ejecuta si la compuesta se repite cero veces") {
    assert(ejecutar(inicial, componer(Avanzar(5), Avanzar(3))) == Estado(0, 8, Norte) &&
      ejecutar(inicial, Repetir(0, componer(Avanzar(5), Avanzar(3)))) == inicial)
  }

  test("componer: es asociativa al ejecutarse") {
    val a = Avanzar(2)
    val b = Girar(Derecha)
    val c = Avanzar(3)
    assert(ejecutar(inicial, componer(componer(a, b), c)) ==
      ejecutar(inicial, componer(a, componer(b, c))))
  }

  test("rutina: tres vueltas dejan al robot mirando al oeste") {
    assert(ejecutar(inicial, rutina(1)(Derecha)(3)) == Estado(1, 0, Oeste))
  }

  test("rutina: los pasos pueden ser negativos") {
    assert(ejecutar(inicial, rutina(-2)(Derecha)(1)) == Estado(0, -2, Este))
  }

  test("rutina: la función currificada sirve para varios tamaños") {
    val cierra = (veces: Int) => ejecutar(inicial, rutina(1)(Izquierda)(veces))
    assert(cierra(4) == inicial && cierra(1) == Estado(0, 1, Oeste))
  }

  test("aplicarA: preserva el orden y el tamaño de la lista") {
    val estados = List(Estado(0, 0, Norte), Estado(1, 1, Sur), Estado(2, 2, Este))
    assert(aplicarA(estados, Avanzar(1)) ==
      List(Estado(0, 1, Norte), Estado(1, 0, Sur), Estado(3, 2, Este)))
  }

  test("aplicarA: equivale a ejecutar sobre un solo estado") {
    val estados = List(Estado(-3, 4, Oeste), Estado(5, -1, Sur))
    assert(aplicarA(estados, Repetir(2, Avanzar(2))) ==
      estados.map(e => ejecutar(e, Repetir(2, Avanzar(2)))))
  }

  test("aplicarA: una compuesta mueve cada estado a un sitio distinto") {
    val estados = List(inicial, Estado(1, 0, Sur))
    assert(aplicarA(estados, Secuencia(List(Girar(Derecha), Avanzar(3)))) ==
      List(Estado(3, 0, Este), Estado(-2, 0, Oeste)))
  }

  test("transformar: no toca las compuestas vacías ni las de veces cero") {
    assert(transformar(Secuencia(Nil), (o: Orden) => Avanzar(9)) == Secuencia(Nil) &&
      transformar(Repetir(0, Avanzar(1)), (o: Orden) => Avanzar(9)) ==
        Repetir(0, Avanzar(9)))
  }

  test("transformar: puede devolver otra clase de orden básica") {
    val todoGira = transformar(Secuencia(List(Avanzar(1), Girar(Derecha))),
      (o: Orden) => Girar(Izquierda))
    assert(todoGira == Secuencia(List(Girar(Izquierda), Girar(Izquierda))))
  }

  test("transformar: recorre todas las ramas de una secuencia larga") {
    val orden = Secuencia(List(Avanzar(1), Girar(Derecha), Repetir(2, Avanzar(1)),
      Secuencia(List(Girar(Izquierda)))))
    val sumando = transformar(orden, { case Avanzar(n) => Avanzar(n + 1); case otra => otra })
    assert(sumando == Secuencia(List(Avanzar(2), Girar(Derecha), Repetir(2, Avanzar(2)),
      Secuencia(List(Girar(Izquierda))))))
  }

  // Punto 5 -------------------------------------------------------------------

  test("simplificar: dos avances seguidos se juntan") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(3)))) == Avanzar(5))
  }

  test("simplificar: repetir una vez es la orden misma") {
    assert(simplificar(Repetir(1, Avanzar(4))) == Avanzar(4))
  }

  test("simplificar: avanzar cero desaparece de la secuencia") {
    assert(simplificar(Secuencia(List(Avanzar(0), Girar(Derecha)))) == Girar(Derecha))
  }

  test("simplificar: junta solo los avances contiguos") {
    assert(simplificar(Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3)))) ==
      Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(3))))
  }

  test("simplificar: repetir cero veces no hace nada") {
    assert(simplificar(Repetir(0, Avanzar(9))) == Secuencia(Nil))
  }

  test("simplificar: dos avances que se cancelan desaparecen") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(-2)))) == Secuencia(Nil))
  }

  test("simplificar: es idempotente") {
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Secuencia(List(Avanzar(0), Avanzar(4)))),
      Secuencia(List(Secuencia(Nil), Repetir(0, Avanzar(3)))))
    assert(ordenes.forall(o => simplificar(simplificar(o)) == simplificar(o)))
  }

  test("simplificar: no cambia el resultado de ejecutar") {
    val estados = List(inicial, Estado(3, -2, Este), Estado(-1, 4, Sur))
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(2), Avanzar(3))),
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Avanzar(4)),
      Repetir(0, Avanzar(9)),
      Secuencia(List(Avanzar(0), Girar(Derecha))),
      Secuencia(List(Avanzar(2), Avanzar(-2))),
      Repetir(3, Secuencia(List(Repetir(1, Avanzar(2)), Girar(Izquierda)))))
    assert(ordenes.forall(o =>
      estados.forall(e => ejecutar(e, o) == ejecutar(e, simplificar(o)))))
  }

  test("simplificar: el anidamiento interior también se reduce") {
    assert(simplificar(Repetir(3, Repetir(1, Avanzar(2)))) == Repetir(3, Avanzar(2)))
  }
  test("simplificar: tres avances seguidos se juntan en uno") {
    assert(simplificar(Secuencia(List(Avanzar(1), Avanzar(2), Avanzar(3)))) == Avanzar(6))
  }

  test("simplificar: avances de signo contrario se restan") {
    assert(simplificar(Secuencia(List(Avanzar(5), Avanzar(-2)))) == Avanzar(3))
  }

  test("simplificar: avanzar cero entre dos giros desaparece") {
    assert(simplificar(Secuencia(List(Girar(Derecha), Avanzar(0), Girar(Izquierda)))) ==
      Secuencia(List(Girar(Derecha), Girar(Izquierda))))
  }

  test("simplificar: simplifica el interior de una repetición") {
    assert(simplificar(Repetir(3, Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha))))) ==
      Repetir(3, Secuencia(List(Avanzar(3), Girar(Derecha)))))
  }

  test("simplificar: aplana las secuencias anidadas") {
    assert(simplificar(Secuencia(List(
      Secuencia(List(Avanzar(1), Girar(Derecha))), Avanzar(2)))) ==
      Secuencia(List(Avanzar(1), Girar(Derecha), Avanzar(2))))
  }
}
