# Informe de proceso — Un robot en la cuadrícula

Fundamentos de Programación Funcional y Concurrente

El robot ocupa una celda $(x, y)$ de una cuadrícula infinita y mira hacia un punto
cardinal. Empieza en el origen mirando al norte: `inicial = Estado(0, 0, Norte)`.
Avanzar hacia el norte aumenta $y$, hacia el este aumenta $x$, avanzar con $n$
negativo retrocede.

Todas las funciones se escriben con `match` sobre las alternativas de `Orden`, sin
variables mutables ni ciclos. La recursión sigue la forma del tipo: `Repetir` y
`Secuencia` contienen otras órdenes, así que las funciones que las recorren son
recursivas..

## Punto 1: `girar`

`girar(d, s)` hace `match` sobre la pareja `(d, s)` y devuelve el siguiente punto
cardinal en el sentido pedido: girar a la derecha recorre Norte → Este → Sur → Oeste,
y a la izquierda al revés.

```scala
case (Norte, Derecha) => Este
case (Este,  Derecha) => Sur
...
```
## Punto 2: `ejecutar`

Hay un caso por cada orden:

1. `Avanzar(n)` suma o resta $n$ a `x` o a `y` según `e.mira`, con `e.copy`.
2. `Girar(s)` cambia solo la dirección, mediante `girar(e.mira, s)`.
3. `Repetir(veces, o)` con `veces <= 0` deja `e` igual; si no, ejecuta `o` una vez
   y vuelve a llamarse con `veces - 1`.
4. `Secuencia(os)` aplica `foldLeft`: parte de `e` y ejecuta cada orden en orden.

### Traza: el cuadrado

Orden: `cuadrado = Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))`,
desde `inicial`.

| Paso | Orden que se ejecuta | Estado resultante |
|---|---|---|
| 0 | — | `(0, 0, Norte)` |
| 1 | `Avanzar(1)` | `(0, 1, Norte)` |
| 2 | `Girar(Derecha)` | `(0, 1, Este)` |
| 3 | `Avanzar(1)` | `(1, 1, Este)` |
| 4 | `Girar(Derecha)` | `(1, 1, Sur)` |
| 5 | `Avanzar(1)` | `(1, 0, Sur)` |
| 6 | `Girar(Derecha)` | `(1, 0, Oeste)` |
| 7 | `Avanzar(1)` | `(0, 0, Oeste)` |
| 8 | `Girar(Derecha)` | `(0, 0, Norte)` |

La orden que queda por ejecutar se va acortando: al inicio de cada vuelta es
`Repetir(k, secuencia)` con $k = 4, 3, 2, 1$; al final del paso 8 se llega a
`Repetir(0, ...)`, que es el caso base y devuelve el estado sin tocarlo. El robot
vuelve al origen mirando al norte.

Recorrido en la cuadrícula (cada flecha es un avance; el número es el paso):

```mermaid
flowchart LR
    P0["(0,0) Norte<br/>paso 0"] -->|"paso 1 ↑"| P1["(0,1) Norte"]
    P1 -.->|"paso 2 ⟳"| P1b["(0,1) Este"]
    P1b -->|"paso 3 →"| P2["(1,1) Este"]
    P2 -.->|"paso 4 ⟳"| P2b["(1,1) Sur"]
    P2b -->|"paso 5 ↓"| P3["(1,0) Sur"]
    P3 -.->|"paso 6 ⟳"| P3b["(1,0) Oeste"]
    P3b -->|"paso 7 ←"| P4["(0,0) Oeste"]
    P4 -.->|"paso 8 ⟳"| P0
```

### Traza: una L

Orden:
`Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(1), Girar(Derecha), Avanzar(3)))`.

| Paso | Orden | Estado |
|---|---|---|
| 0 | — | `(0, 0, Norte)` |
| 1 | `Avanzar(2)` | `(0, 2, Norte)` |
| 2 | `Girar(Derecha)` | `(0, 2, Este)` |
| 3 | `Avanzar(1)` | `(1, 2, Este)` |
| 4 | `Girar(Derecha)` | `(1, 2, Sur)` |
| 5 | `Avanzar(3)` | `(1, -1, Sur)` |

El `foldLeft` empieza en `(0, 0, Norte)` y le pasa a cada orden el estado que
dejó la anterior; el resultado es `Estado(1, -1, Sur)`.

### Repeticiones anidadas

`Repetir(2, Repetir(2, Secuencia(List(Avanzar(2), Girar(Derecha)))))` ejecuta el
avance de dos casillas y el giro cuatro veces, siempre en casillas distintas, y
termina en `Estado(0, 0, Norte)`:

```mermaid
flowchart LR
    A["(0,0) Norte"] -->|"↑2"| B["(0,2) Norte"]
    B -->|"⟳"| C["(0,2) Este"]
    C -->|"→2"| D["(2,2) Este"]
    D -->|"⟳"| E["(2,2) Sur"]
    E -->|"↓2"| F["(2,0) Sur"]
    F -->|"⟳"| G["(2,0) Oeste"]
    G -->|"←2"| H["(0,0) Oeste"]
    H -->|"⟳"| A
```

## Punto 3: `cuantasBasicas` y `profundidad`

`cuantasBasicas` cuenta sin mover al robot: una básica vale 1, una `Secuencia`
suma las de sus hijas (`map` y `sum`) y un `Repetir(v, o)` multiplica por $v$.
`profundidad` vale 1 en las básicas; en `Repetir` es $1 +$ la de su orden y en
`Secuencia` es $1 +$ el máximo de las suyas (`foldLeft` con `math.max`).

Para el cuadrado se llega a `8` así: la secuencia interna vale $1 + 1 = 2$ y la
repetición multiplica por 4, dando `8`. La profundidad es $3$: el cuadrado es un
`Repetir` ($1$), seguido de la `Secuencia` interna ($2$), seguida de las órdenes
básicas ($3$). Ninguna de las dos ejecuta la orden, entonces no hay trazas de
posiciones.

## Punto 4: órdenes y funciones de alto orden

- `componer(o1, o2)` devuelve `Secuencia(List(o1, o2))`.
- `rutina(pasos)(giro)(veces)` está currificada: `rutina(2)(Izquierda)` ya es una
  función que espera las veces. Produce
  `Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro))))`; con `4` da
  `Repetir(4, Secuencia(List(Avanzar(2), Girar(Izquierda))))`.
- `aplicarA` hace `map` de `ejecutar(e, o)` sobre la lista de estados.
- `transformar` recorre la orden y aplica `f` solo a las básicas, reconstruyendo
  `Repetir` y `Secuencia` con las hijas transformadas.

`transformar(cuadrado, { case Avanzar(n) => Avanzar(2 * n); case otra => otra })`
devuelve `Repetir(4, Secuencia(List(Avanzar(2), Girar(Derecha))))`: el avance
pasa de 1 a 2 y los giros quedan igual. `aplicarA(List(inicial, Estado(5, 5, Sur)), Avanzar(2))`
da `List(Estado(0, 2, Norte), Estado(5, 3, Sur))`: cada estado avanza en la
dirección en que miraba.

## Punto 5: `simplificar`

La función aplica las reglas del enunciado en una sola pasada recursiva:
simplifica primero el interior de cada compuesta y luego procesa el resultado que
sube. Sobre una `Secuencia` trabaja en tres etapas, en este orden:

1. **Simplificar y aplanar.** Se simplifica cada hija; si el resultado es una
   `Secuencia`, se reemplaza por sus elementos (esto es el aplanado) y, si no,
   se deja un solo elemento.
2. **Juntar avances seguidos** (`foldLeft`): dos `Avanzar` consecutivos se suman
   en uno.
3. **Quitar las órdenes que no hacen nada** y decidir la forma final: sin
   elementos queda `Secuencia(Nil)`, con uno queda ese elemento, y con varios
   queda la `Secuencia`.

Traza con
`s = Secuencia(List(Repetir(1, Secuencia(List(Avanzar(1), Avanzar(2)))), Secuencia(Nil), Avanzar(3), Avanzar(0), Girar(Derecha)))`.
Se muestra el estado de la lista de órdenes en cada etapa:

| Etapa | Lista de órdenes | Regla |
|---|---|---|
| 0, entrada | `Sec(Rep1(Seq(Av1, Av2)), Seq(Nil), Av3, Av0, Gir(D))` | — |
| 1a, interior de `Repetir(1, …)` | `Seq(Avanzar(1), Avanzar(2))` → `Avanzar(3)` | 2: se juntan |
| 1b, `Repetir(1, Avanzar(3))` | → `Avanzar(3)` | 3: `Repetir(1, o)` es `o` |
| 1c, hijas ya simplificadas | `List(Av3, Seq(Nil), Av3, Seq(Nil), Gir(D))` | 1: `Av0` ya es `Seq(Nil)` |
| 1d, aplanado (`flatMap`) | `List(Avanzar(3), Avanzar(3), Girar(Derecha))` | 5: las `Seq(Nil)` no aportan elementos |
| 2, juntar (`foldLeft`) | `List(Avanzar(6), Girar(Derecha))` | 2: `Av3 + Av3` en `Avanzar(6)` |
| 3, quitar inútiles y decidir | `List(Avanzar(6), Girar(Derecha))` | más de un elemento: se queda como `Secuencia` |

Resultado: `Secuencia(List(Avanzar(6), Girar(Derecha)))`. La orden original y la
simplificada ejecutan el mismo resultado: ambas dejan `Estado(0, 6, Este)`.

Otro caso, con una repetición que se simplifica por dentro pero no por fuera:
`Repetir(2, Secuencia(List(Avanzar(1), Avanzar(2))))`. El interior se reduce a
`Avanzar(3)` (regla 2), y como los `Repetir` con dos o más veces no se tocan, el
resultado es `Repetir(2, Avanzar(3))`, que ya es más corto que la orden original.
Con `Repetir(1, ...)` el mismo interior daría directamente `Avanzar(3)` (regla 3).

```mermaid
flowchart TD
    S["Secuencia(Rep1(Seq(Av1, Av2)), Seq(Nil), Av3, Av0, Gir(D))"]
    S --> R1["1. simplificar hijas y aplanar<br/>List(Av3, Av3, Gir(D))"]
    R1 --> R2["2. juntar avances seguidos<br/>List(Avanzar(6), Gir(D))"]
    R2 --> R3["3. quitar inútiles y decidir forma<br/>Secuencia(Avanzar(6), Girar(Derecha))"]
```