# Informe de corrección

Taller 2: Un robot en la cuadrícula. Fundamentos de Programación Funcional y Concurrente.

## 0. Cómo se argumenta la corrección

El tipo `Orden` está definido recursivamente por cuatro alternativas:

- Casos básicos: `Avanzar(n)` y `Girar(s)`.
- Casos recursivos: `Repetir(k, o)` y `Secuencia(List(o_1, ..., o_n))`, que contienen otras órdenes.

Por eso, para demostrar que una función sobre `Orden` es correcta se usa **inducción estructural**: un caso por cada alternativa, y en los casos recursivos se supone la hipótesis de inducción (HI) para las órdenes contenidas. Cuando una función usa `foldLeft` (proceso iterativo), se argumenta con **estado, estado inicial, estado final, invariante y transformación**.

| Punto | Función | Técnica |
|---|---|---|
| 1 | `girar` | Análisis de los 8 casos del `match` |
| 2 | `ejecutar` | Inducción estructural + inducción sobre `veces` + invariante del `foldLeft` |
| 3 | `cuantasBasicas`, `profundidad` | Inducción estructural + invariante del `foldLeft` |
| 4 | `componer`, `rutina`, `aplicarA`, `transformar` | Sustitución, inducción sobre `veces`, sobre listas y estructural |
| 5 | `simplificar` | Inducción estructural, regla por regla |

## 1. Especificación

Sea $\mathcal{E}(o) : \text{Estado} \to \text{Estado}$ la función que describe lo que **dice el enunciado** que hace la orden $o$. Se define por casos sobre $o$. Con $e = (x, y, m)$:

$$
\mathcal{E}(\text{Avanzar}(n))(x, y, m) =
\begin{cases}
(x,\ y + n,\ m) & m = \text{Norte} \\
(x,\ y - n,\ m) & m = \text{Sur} \\
(x + n,\ y,\ m) & m = \text{Este} \\
(x - n,\ y,\ m) & m = \text{Oeste}
\end{cases}
$$

x

$$
\mathcal{E}(\text{Repetir}(k, o)) = \mathcal{E}(o)^{k}, \qquad \mathcal{E}(\text{Secuencia}(\text{List}(o_1, \ldots, o_n))) = \mathcal{E}(o_n) \circ \cdots \circ \mathcal{E}(o_1)
$$

Donde:

- $f^0 = \text{id}$ y $f^{k+1} = f \circ f^{k}$. Si $k \leq 0$, $\mathcal{E}(o)^{k} = \text{id}$ (el enunciado dice que `Repetir` con cero o menos veces no hace nada).
- Para $n = 0$, la composición vacía es $\text{id}$ (`Secuencia(Nil)` no hace nada).
- $g(m, s)$ es el giro de noventa grados: con `Derecha` la dirección avanza en el orden Norte → Este → Sur → Oeste → Norte, y con `Izquierda` retrocede en ese mismo orden.

Lo que se demuestra para `ejecutar` es que coincide con $\mathcal{E}$:

$$
\forall o \in \text{Orden},\ \forall e \in \text{Estado} : \text{ejecutar}(e, o) == \mathcal{E}(o)(e)
$$

## 2. Punto 1: `girar`

```scala
def girar(d: Direccion, s: Sentido): Direccion = (d, s) match {
  case (Norte, Derecha)   => Este
  ...
}
```

**Teorema 1.** $\forall d, s : \text{girar}(d, s) == g(d, s)$.

Como `Direccion` y `Sentido` son `sealed`, hay $4 \cdot 2 = 8$ parejas posibles y el `match` tiene un caso para cada una (nunca falla con `MatchError`). Se verifica cada una contra la especificación (`Derecha` avanza una posición en el ciclo Norte, Este, Sur, Oeste; `Izquierda` retrocede una):

| $d$ | $s$ | `girar(d, s)` | $g(d, s)$ |
|---|---|---|---|
| Norte | Derecha | Este | Este |
| Norte | Izquierda | Oeste | Oeste |
| Este | Derecha | Sur | Sur |
| Este | Izquierda | Norte | Norte |
| Sur | Derecha | Oeste | Oeste |
| Sur | Izquierda | Este | Este |
| Oeste | Derecha | Norte | Norte |
| Oeste | Izquierda | Sur | Sur |

Las 8 filas coinciden. $\blacksquare$

## 3. Punto 2: `ejecutar`

```scala
def ejecutar(e: Estado, o: Orden): Estado = o match {
  case Avanzar(n) => e.mira match { ... }
  case Girar(s) => e.copy(mira = girar(e.mira, s))
  case Repetir(veces, orden) =>
    if (veces <= 0) e
    else ejecutar(ejecutar(e, orden), Repetir(veces - 1, orden))
  case Secuencia(ordenes) =>
    ordenes.foldLeft(e)((estado, orden) => ejecutar(estado, orden))
}
```

### Lema A: `Repetir` es la potencia

Sea $o$ una orden tal que $\forall s : \text{ejecutar}(s, o) == \mathcal{E}(o)(s)$. Entonces:

$$
\forall k \in \mathbb{Z},\ \forall e : \text{ejecutar}(e, \text{Repetir}(k, o)) == \mathcal{E}(o)^{k}(e)
$$

**Caso base:** $k \leq 0$.

$$
\text{ejecutar}(e, \text{Repetir}(k, o)) \rightarrow \text{if } (k \leq 0)\ e \text{ else } \ldots \rightarrow e
$$

Por otro lado, $\mathcal{E}(o)^{k}(e) = \text{id}(e) = e$. Entonces son iguales.

**Caso de inducción:** $k \geq 1$. Hipótesis de inducción (HI): $\text{ejecutar}(s, \text{Repetir}(k-1, o)) == \mathcal{E}(o)^{k-1}(s)$ para todo $s$.

$$
\text{ejecutar}(e, \text{Repetir}(k, o)) \rightarrow \text{ejecutar}(\text{ejecutar}(e, o),\ \text{Repetir}(k-1, o))
$$

Como $\text{ejecutar}(e, o) = \mathcal{E}(o)(e)$ por hipótesis sobre $o$, y usando la HI con $s = \mathcal{E}(o)(e)$:

$$
\rightarrow \mathcal{E}(o)^{k-1}(\mathcal{E}(o)(e)) = \mathcal{E}(o)^{k}(e)
$$

Concluimos por inducción sobre $k$. $\blacksquare$

### Lema B: el `foldLeft` de `Secuencia` es una iteración correcta

Sea $L = \text{List}(o_1, \ldots, o_n)$ tal que $\forall i, \forall s : \text{ejecutar}(s, o_i) == \mathcal{E}(o_i)(s)$. Entonces:

$$
L.\text{foldLeft}(e)(\text{ejecutar}) == \mathcal{E}(o_n) \circ \cdots \circ \mathcal{E}(o_1)(e)
$$

Este programa implementa el siguiente proceso iterativo:

- Un estado $s = (acc, l)$, donde $acc$ es el estado del robot y $l = \text{List}(o_i, \ldots, o_n)$ son las órdenes que faltan por ejecutar.
- El estado inicial es $s_0 = (e, L)$.
- $(acc, l)$ es final si $l$ es vacía.
- La invariante es $\text{Inv}(acc, l) \equiv l = \text{List}(o_i, \ldots, o_n) \land acc = \mathcal{E}(o_{i-1}) \circ \cdots \circ \mathcal{E}(o_1)(e)$ (para $i = 1$, $acc = e$).
- $\text{transformar}((acc, l)) = (\text{ejecutar}(acc, l.\text{head}),\ l.\text{tail})$.

Demostración de los puntos:

**1.** $\text{Inv}(s_0)$: con $i = 1$, $l = L$ y $acc = e$ (composición vacía). Se cumple.

**2.** Si $l \neq \text{List}()$ e $\text{Inv}(acc, l)$, entonces $\text{Inv}(\text{transformar}(acc, l))$:

$$
acc' = \text{ejecutar}(acc, o_i) = \mathcal{E}(o_i)(acc) = \mathcal{E}(o_i) \circ \cdots \circ \mathcal{E}(o_1)(e) \quad \land \quad l' = l.\text{tail} = \text{List}(o_{i+1}, \ldots, o_n)
$$

(La segunda igualdad usa la hipótesis sobre $o_i$.) Es la invariante con $i + 1$.

**3.** $\text{Inv}(s_f) \rightarrow$ respuesta correcta. En un estado final $l = \text{List}()$, es decir $i = n + 1$, y entonces $acc = \mathcal{E}(o_n) \circ \cdots \circ \mathcal{E}(o_1)(e)$.

**4.** En cada paso $l$ pierde un elemento. Después de $n$ pasos $l = \text{List}()$, así que siempre se llega al estado final. $\blacksquare$

### Teorema 2: corrección de `ejecutar`

$$
\forall o \in \text{Orden},\ \forall e \in \text{Estado} : \text{ejecutar}(e, o) == \mathcal{E}(o)(e)
$$

Se demuestra por inducción estructural sobre $o$.

**Caso base:** $o = \text{Avanzar}(n)$. Sea $e = (x, y, m)$. El `match` sobre `e.mira` tiene cuatro casos:

$$
m = \text{Norte}: \text{e.copy}(y = e.y + n) = (x,\ y + n,\ \text{Norte}) \qquad
m = \text{Sur}: (x,\ y - n,\ \text{Sur})
$$

$$
m = \text{Este}: (x + n,\ y,\ \text{Este}) \qquad
m = \text{Oeste}: (x - n,\ y,\ \text{Oeste})
$$

Los cuatro coinciden con $\mathcal{E}(\text{Avanzar}(n))(e)$. (Con $n$ negativo la fórmula es la misma, y por eso retrocede.)

**Caso base:** $o = \text{Girar}(s)$.

$$
\text{ejecutar}(e, \text{Girar}(s)) \rightarrow \text{e.copy}(\text{mira} = \text{girar}(m, s)) = (x,\ y,\ \text{girar}(m, s))
$$

Por el Teorema 1, $\text{girar}(m, s) = g(m, s)$. Entonces el resultado es $(x, y, g(m, s)) = \mathcal{E}(\text{Girar}(s))(e)$.

**Caso de inducción:** $o = \text{Repetir}(k, o')$. HI: $\text{ejecutar}(s, o') == \mathcal{E}(o')(s)$ para todo $s$. Esto es exactamente la hipótesis del Lema A, que da $\text{ejecutar}(e, \text{Repetir}(k, o')) == \mathcal{E}(o')^{k}(e) = \mathcal{E}(\text{Repetir}(k, o'))(e)$.

**Caso de inducción:** $o = \text{Secuencia}(\text{List}(o_1, \ldots, o_n))$. HI: la propiedad vale para cada $o_i$ (todas son subórdenes). Por el Lema B:

$$
\text{ejecutar}(e, o) \rightarrow \text{ordenes.foldLeft}(e)(\ldots) == \mathcal{E}(o_n) \circ \cdots \circ \mathcal{E}(o_1)(e) = \mathcal{E}(o)(e)
$$

Para $n = 0$ el resultado es $e$, que es $\text{id}(e)$.

Concluimos por inducción estructural que $\forall o, \forall e : \text{ejecutar}(e, o) == \mathcal{E}(o)(e)$. $\blacksquare$

A partir de aquí, "ejecutar $o$" y "aplicar $\mathcal{E}(o)$" son lo mismo.

## 4. Punto 3: `cuantasBasicas` y `profundidad`

### `cuantasBasicas`

Sea $N(o)$ el número de veces que `ejecutar` llega a un caso `Avanzar` o `Girar` al ejecutar $o$ (contando repeticiones). Por el Teorema 2, `Repetir(k, o)` con $k \geq 0$ ejecuta $o$ exactamente $k$ veces, y `Secuencia` ejecuta cada una de sus órdenes una vez. Entonces:

$$
N(\text{Avanzar}(n)) = N(\text{Girar}(s)) = 1, \qquad N(\text{Repetir}(k, o)) = k \cdot N(o), \qquad N(\text{Secuencia}(\text{List}(o_1, \ldots, o_n))) = \sum_{i=1}^{n} N(o_i)
$$

**Hipótesis:** todo `Repetir(k, o)` tiene $k \geq 0$.

```scala
def cuantasBasicas(o: Orden): Int = o match {
  case Avanzar(_) => 1
  case Girar(_)   => 1
  case Secuencia(ordenes) => ordenes.map(cuantasBasicas).sum
  case Repetir(veces, orden) => veces * cuantasBasicas(orden)
}
```

**Teorema 3.** $\forall o : \text{cuantasBasicas}(o) == N(o)$.

Se usan dos propiedades de las listas, ambas por inducción sobre la lista:

- $\text{List}(a_1, \ldots, a_n).\text{map}(f) = \text{List}(f(a_1), \ldots, f(a_n))$: para `Nil` es `Nil`, y para `h :: t` es `f(h) :: t.map(f)`.
- $\text{List}(b_1, \ldots, b_n).\text{sum} = \sum b_i$: para `Nil` es 0, y para `h :: t` es `h + t.sum`.

**Casos base:** $\text{cuantasBasicas}(\text{Avanzar}(n)) \rightarrow 1 = N(\text{Avanzar}(n))$. Igual para `Girar`.

**Caso de inducción:** $o = \text{Repetir}(k, o')$. Por HI, $\text{cuantasBasicas}(o') = N(o')$. Entonces $\text{cuantasBasicas}(o) \rightarrow k \cdot \text{cuantasBasicas}(o') = k \cdot N(o') = N(o)$.

**Caso de inducción:** $o = \text{Secuencia}(\text{List}(o_1, \ldots, o_n))$. Por HI, $\text{cuantasBasicas}(o_i) = N(o_i)$ para todo $i$. Entonces:

$$
\text{ordenes.map(cuantasBasicas).sum} = \text{List}(N(o_1), \ldots, N(o_n)).\text{sum} = \sum_{i=1}^{n} N(o_i) = N(o)
$$

Concluimos por inducción estructural. $\blacksquare$

### `profundidad`

Especificación: una orden básica tiene profundidad 1, y una compuesta tiene uno más que la más honda que contenga (con una secuencia vacía de profundidad 1):

$$
P(\text{Avanzar}(n)) = P(\text{Girar}(s)) = 1, \qquad P(\text{Repetir}(k, o)) = 1 + P(o)
$$

$$
P(\text{Secuencia}(\text{List}(o_1, \ldots, o_n))) = 1 + \max(\{0\} \cup \{P(o_1), \ldots, P(o_n)\})
$$

**Lema C: el `foldLeft` del máximo es una iteración correcta.** Sea $L = \text{List}(o_1, \ldots, o_n)$ con $\text{profundidad}(o_i) = P(o_i)$. Entonces:

$$
L.\text{foldLeft}(0)((m, x) \Rightarrow \text{math.max}(m, \text{profundidad}(x))) == \max(\{0\} \cup \{P(o_1), \ldots, P(o_n)\})
$$

- Un estado $s = (m, l)$, con $l = \text{List}(o_i, \ldots, o_n)$ las órdenes pendientes.
- El estado inicial es $s_0 = (0, L)$.
- $(m, l)$ es final si $l$ es vacía.
- $\text{Inv}(m, l) \equiv l = \text{List}(o_i, \ldots, o_n) \land m = \max(\{0\} \cup \{P(o_1), \ldots, P(o_{i-1})\})$.
- $\text{transformar}((m, l)) = (\text{math.max}(m, \text{profundidad}(l.\text{head})),\ l.\text{tail})$.

**1.** $\text{Inv}(s_0)$: $i = 1$, $m = 0 = \max(\{0\})$.

**2.** Si $l \neq \text{List}()$, el nuevo valor es $\max(m, P(o_i)) = \max(\{0\} \cup \{P(o_1), \ldots, P(o_i)\})$ y la lista queda en $\text{List}(o_{i+1}, \ldots, o_n)$. Se mantiene la invariante.

**3.** En el estado final $i = n + 1$, así que $m = \max(\{0\} \cup \{P(o_1), \ldots, P(o_n)\})$.

**4.** En cada paso $l$ pierde un elemento, y después de $n$ pasos queda vacía. $\blacksquare$

```scala
def profundidad(o: Orden): Int = o match {
  case Avanzar(_) => 1
  case Girar(_)   => 1
  case Repetir(_, orden) => 1 + profundidad(orden)
  case Secuencia(ordenes) =>
    val honda = ordenes.foldLeft(0)((m, x) => math.max(m, profundidad(x)))
    1 + honda
}
```

**Teorema 4.** $\forall o : \text{profundidad}(o) == P(o)$.

**Casos base:** `Avanzar` y `Girar` devuelven $1 = P(o)$.

**Caso de inducción:** $o = \text{Repetir}(k, o')$. Por HI, $\text{profundidad}(o') = P(o')$, y entonces $\text{profundidad}(o) \rightarrow 1 + \text{profundidad}(o') = 1 + P(o') = P(o)$.

**Caso de inducción:** $o = \text{Secuencia}(\text{List}(o_1, \ldots, o_n))$. Por HI, $\text{profundidad}(o_i) = P(o_i)$ para todo $i$. Por el Lema C, $\text{honda} = \max(\{0\} \cup \{P(o_i)\})$, y entonces $\text{profundidad}(o) \rightarrow 1 + \text{honda} = P(o)$. Para $n = 0$ da $1 + 0 = 1$.

Concluimos por inducción estructural. $\blacksquare$

## 5. Punto 4: rutinas y funciones de alto orden

### `componer`: la rutina compuesta equivale a ejecutar sus partes

```scala
def componer(o1: Orden, o2: Orden): Orden = Secuencia(List(o1, o2))
```

**Teorema 5.** $\forall e, o_1, o_2 : \text{ejecutar}(e, \text{componer}(o_1, o_2)) == \text{ejecutar}(\text{ejecutar}(e, o_1), o_2)$.

Por el modelo de sustitución:

$$
\text{ejecutar}(e, \text{componer}(o_1, o_2)) \rightarrow \text{ejecutar}(e, \text{Secuencia}(\text{List}(o_1, o_2))) \rightarrow \text{List}(o_1, o_2).\text{foldLeft}(e)(\text{ejecutar})
$$

$$
\rightarrow \text{ejecutar}(\text{ejecutar}(e, o_1), o_2)
$$

El `foldLeft` sobre una lista de dos elementos aplica primero $o_1$ a $e$ y luego $o_2$ al resultado. Es lo mismo que el Lema B con $n = 2$. $\blacksquare$

En términos de la especificación: $\mathcal{E}(\text{componer}(o_1, o_2)) = \mathcal{E}(o_2) \circ \mathcal{E}(o_1)$.

### `rutina`: la rutina repetida equivale a ejecutar sus partes en cada vuelta

```scala
def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
  Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro))))
```

Sea $R = \mathcal{E}(\text{Girar}(giro)) \circ \mathcal{E}(\text{Avanzar}(pasos))$ el efecto de **una vuelta** (avanzar y después girar).

**Teorema 6.** Para todo $e$ y todo $v \geq 0$:

$$
\text{ejecutar}(e, \text{rutina}(pasos)(giro)(v)) == R^{v}(e)
$$

Demostración. Por el modelo de sustitución, $\text{rutina}(pasos)(giro)(v) \rightarrow \text{Repetir}(v, c)$ con $c = \text{Secuencia}(\text{List}(\text{Avanzar}(pasos), \text{Girar}(giro)))$. Por el Teorema 2 sobre `Secuencia` (Lema B con $n = 2$):

$$
\mathcal{E}(c) = \mathcal{E}(\text{Girar}(giro)) \circ \mathcal{E}(\text{Avanzar}(pasos)) = R
$$

Por el Lema A, $\text{ejecutar}(e, \text{Repetir}(v, c)) == \mathcal{E}(c)^{v}(e) = R^{v}(e)$. $\blacksquare$

Es decir, ejecutar la rutina `v` veces es ejecutar `v` veces sus partes (`Avanzar(pasos)` y luego `Girar(giro)`), en ese orden.

**Corolario 6.1 (la rutina se puede partir).** Para $v, w \geq 0$:

$$
\text{ejecutar}(e, \text{rutina}(pasos)(giro)(v + w)) == \text{ejecutar}(\text{ejecutar}(e, \text{rutina}(pasos)(giro)(v)),\ \text{rutina}(pasos)(giro)(w))
$$

Por el Teorema 6, el lado izquierdo es $R^{v+w}(e)$ y el derecho es $R^{w}(R^{v}(e))$, y son iguales porque $R^{v+w} = R^{w} \circ R^{v}$. $\blacksquare$

**Sobre la currificación.** `rutina(pasos)(giro)` es la función `veces => rutina(pasos)(giro)(veces)`. Fijar los dos primeros argumentos no cambia el valor de ninguna llamada completa, así que el Teorema 6 vale para cualquier aplicación parcial.

### `aplicarA`

```scala
def aplicarA(estados: List[Estado], o: Orden): List[Estado] =
  estados.map(e => ejecutar(e, o))
```

**Teorema 7.** Para $\text{estados} = \text{List}(e_1, \ldots, e_n)$:

$$
\text{aplicarA}(\text{estados}, o) == \text{List}(\mathcal{E}(o)(e_1), \ldots, \mathcal{E}(o)(e_n))
$$

Se demuestra por inducción sobre la lista.

**Caso base:** $\text{Nil.map}(f) \rightarrow \text{Nil}$, y la lista de resultados también es vacía.

**Caso de inducción:** $\text{estados} = e_1 :: t$. Por la definición de `map`, $\text{aplicarA}(e_1 :: t, o) \rightarrow \text{ejecutar}(e_1, o) :: t.\text{map}(\ldots)$. Por el Teorema 2, $\text{ejecutar}(e_1, o) = \mathcal{E}(o)(e_1)$, y por HI $t.\text{map}(\ldots) = \text{List}(\mathcal{E}(o)(e_2), \ldots, \mathcal{E}(o)(e_n))$. Se concluye por inducción. $\blacksquare$

La misma orden se ejecuta sobre cada estado de forma independiente, en el mismo orden de la lista.

### `transformar`

```scala
def transformar(o: Orden, f: Orden => Orden): Orden = o match {
  case Avanzar(_) => f(o)
  case Girar(_)   => f(o)
  case Repetir(veces, orden) => Repetir(veces, transformar(orden, f))
  case Secuencia(ordenes) => Secuencia(ordenes.map(x => transformar(x, f)))
}
```

Especificación: $T_f(o)$ reemplaza cada orden básica $b$ por $f(b)$ y deja intacta la estructura de las compuestas. Es decir:

$$
T_f(b) = f(b) \text{ si } b \text{ es básica}, \quad T_f(\text{Repetir}(k, o)) = \text{Repetir}(k, T_f(o)), \quad T_f(\text{Secuencia}(\text{List}(o_i))) = \text{Secuencia}(\text{List}(T_f(o_i)))
$$

**Teorema 8.** $\forall o : \text{transformar}(o, f) == T_f(o)$, y además:

- `Repetir` conserva su número de `veces`.
- `Secuencia` conserva su número de elementos y su orden.

Se demuestra por inducción estructural sobre $o$.

**Casos base:** `Avanzar` y `Girar` devuelven $f(o)$, igual que $T_f$.

**Caso de inducción:** $o = \text{Repetir}(k, o')$. Por HI, $\text{transformar}(o', f) = T_f(o')$. Entonces $\text{transformar}(o, f) \rightarrow \text{Repetir}(k, T_f(o')) = T_f(o)$, con el mismo $k$.

**Caso de inducción:** $o = \text{Secuencia}(\text{List}(o_1, \ldots, o_n))$. Por HI, $\text{transformar}(o_i, f) = T_f(o_i)$ para todo $i$. Por la propiedad de `map` (Punto 3), el resultado es $\text{Secuencia}(\text{List}(T_f(o_1), \ldots, T_f(o_n))) = T_f(o)$, con $n$ elementos en el mismo orden. $\blacksquare$

**Corolario 8.1.** Si $f$ no cambia el efecto de las órdenes básicas, es decir $\mathcal{E}(f(b)) = \mathcal{E}(b)$ para toda orden básica $b$, entonces $\mathcal{E}(\text{transformar}(o, f)) = \mathcal{E}(o)$ para toda $o$.

Demostración por inducción estructural. Casos base: es la hipótesis sobre $f$. `Repetir`: $\mathcal{E}(T_f(o'))^{k} = \mathcal{E}(o')^{k}$ por HI. `Secuencia`: la composición de los $\mathcal{E}(T_f(o_i)) = \mathcal{E}(o_i)$ es la misma. $\blacksquare$

## 6. Punto 5: `simplificar`

```scala
def simplificar(o: Orden): Orden = o match {
  case Avanzar(0) => Secuencia(Nil)
  case Avanzar(_) => o
  case Girar(_)   => o
  case Repetir(veces, orden) => ...
  case Secuencia(ordenes) => ...
}
```

Por el Teorema 2, lo que pide el enunciado ("para toda orden $o$ y todo estado $e$, ejecutar $o$ y ejecutar `simplificar(o)` dejan el mismo estado") equivale a:

$$
\forall o : \mathcal{E}(\text{simplificar}(o)) = \mathcal{E}(o)
$$

Dos hechos que se usan en toda la demostración, ambos consecuencia directa de la definición de $\mathcal{E}$:

- $\mathcal{E}(\text{Avanzar}(0)) = \text{id}$, porque moverse 0 casillas en cualquier dirección no cambia el estado. También $\mathcal{E}(\text{Secuencia}(\text{Nil})) = \text{id}$.
- Para listas de órdenes $l_1, l_2$ (Lema concatenación): $\mathcal{E}(\text{Secuencia}(l_1 \mathbin{+\!+} l_2)) = \mathcal{E}(\text{Secuencia}(l_2)) \circ \mathcal{E}(\text{Secuencia}(l_1))$. Sale de que la composición es asociativa.

Se escribe $\Sigma(l) = \mathcal{E}(\text{Secuencia}(l))$.

**Teorema 9.** $\forall o : \mathcal{E}(\text{simplificar}(o)) = \mathcal{E}(o)$.

Se demuestra por inducción estructural sobre $o$.

### Casos base

**Regla 1, `Avanzar(0)`:** $\text{simplificar}(\text{Avanzar}(0)) \rightarrow \text{Secuencia}(\text{Nil})$, y $\mathcal{E}(\text{Secuencia}(\text{Nil})) = \text{id} = \mathcal{E}(\text{Avanzar}(0))$.

**`Avanzar(n)` con $n \neq 0$ y `Girar(s)`:** devuelven $o$ sin cambios, así que $\mathcal{E}(\text{simplificar}(o)) = \mathcal{E}(o)$.

### Caso de inducción: `Repetir(k, o')`

Sea $i = \text{simplificar}(o')$. HI: $\mathcal{E}(i) = \mathcal{E}(o')$. Hay cuatro posibilidades, según las ramas del `if`:

- **$k \leq 0$ (Regla 1):** devuelve `Secuencia(Nil)`, cuyo efecto es $\text{id}$. Por otro lado, $\mathcal{E}(\text{Repetir}(k, o')) = \mathcal{E}(o')^{k} = \text{id}$ cuando $k \leq 0$. Son iguales.
- **$k = 1$ (Regla 3):** devuelve $i$. Entonces $\mathcal{E}(i) = \mathcal{E}(o') = \mathcal{E}(o')^{1} = \mathcal{E}(\text{Repetir}(1, o'))$.
- **$k \geq 2$ e $i = \text{Secuencia}(\text{Nil})$ (Regla 1, la orden interior no hace nada):** devuelve `Secuencia(Nil)`, que es $\text{id}$. Por otro lado, $\mathcal{E}(o')^{k} = \mathcal{E}(i)^{k} = \text{id}^{k} = \text{id}$ (usando la HI).
- **$k \geq 2$ e $i \neq \text{Secuencia}(\text{Nil})$:** devuelve $\text{Repetir}(k, i)$. Su efecto es $\mathcal{E}(i)^{k} = \mathcal{E}(o')^{k}$ por la HI.

### Caso de inducción: `Secuencia(List(o_1, ..., o_n))`

Sea $s_j = \text{simplificar}(o_j)$. HI: $\mathcal{E}(s_j) = \mathcal{E}(o_j)$ para todo $j$. Entonces:

$$
\mathcal{E}(\text{Secuencia}(\text{List}(s_1, \ldots, s_n))) = \mathcal{E}(s_n) \circ \cdots \circ \mathcal{E}(s_1) = \mathcal{E}(o_n) \circ \cdots \circ \mathcal{E}(o_1) = \mathcal{E}(o)
$$

El código procesa esta lista en cuatro etapas. Se muestra que cada una conserva $\Sigma$.

**Etapa 1: `aplanadas` (aplanar secuencias anidadas).** El `flatMap` reemplaza cada $s_j$ por:

- sus hijas, si $s_j = \text{Secuencia}(\text{hijas})$. En ese caso $\Sigma(\text{hijas}) = \mathcal{E}(s_j)$ por definición.
- `List(s_j)` en otro caso, y $\Sigma(\text{List}(s_j)) = \mathcal{E}(s_j)$ (Regla 4 sobre un solo elemento).

El resultado es la concatenación de esos trozos en orden. Por el lema de concatenación, $\Sigma(\text{aplanadas}) = \mathcal{E}(s_n) \circ \cdots \circ \mathcal{E}(s_1) = \mathcal{E}(o)$.

**Etapa 2: `juntas` (juntar avances seguidos, Regla 2).** El `foldLeft` es un proceso iterativo:

- Un estado $(acc, l)$, donde $acc$ guarda lo ya procesado en orden **inverso** y $l$ lo que falta.
- El estado inicial es $(\text{List}(), \text{aplanadas})$, y es final si $l$ es vacía.
- $\text{Inv}(acc, l) \equiv \Sigma(acc.\text{reverse}) = \Sigma(\text{prefijo ya procesado de aplanadas})$.
- $\text{transformar}$ toma $x = l.\text{head}$ y actúa según el patrón.

Demostración de los puntos:

**1.** $\text{Inv}(s_0)$: ambos lados son $\Sigma(\text{List}()) = \text{id}$.

**2.** La invariante se mantiene:

- Si $acc = \text{Avanzar}(a) :: resto$ y $x = \text{Avanzar}(b)$, el nuevo acumulado es $\text{Avanzar}(a + b) :: resto$. Basta ver que $\mathcal{E}(\text{Avanzar}(b)) \circ \mathcal{E}(\text{Avanzar}(a)) = \mathcal{E}(\text{Avanzar}(a + b))$: `Avanzar` no cambia la dirección $m$, y en cada una de las cuatro direcciones el desplazamiento acumulado es $a + b$ (por ejemplo, para Norte, $(y + a) + b = y + (a + b)$). Esto es la Regla 2.
- En cualquier otro caso, el nuevo acumulado es $x :: acc$. Al invertirlo, $x$ queda al final del prefijo ya procesado, así que $\Sigma$ no cambia.

**3.** En el estado final $l = \text{List}()$, así que el prefijo es toda `aplanadas` y $\Sigma(\text{juntas}) = \Sigma(acc.\text{reverse}) = \Sigma(\text{aplanadas})$.

**4.** En cada paso $l$ pierde un elemento, así que se llega al estado final.

**Etapa 3: `sinCeros` (quitar órdenes que no hacen nada, Reglas 1 y 5).** Se quitan los elementos iguales a `Avanzar(0)`, cuyo efecto es $\text{id}$. Por inducción sobre la lista, quitar elementos con efecto $\text{id}$ de una composición no cambia el resultado:

$$
\text{Nil}: \text{trivial.} \qquad x :: t:\ \Sigma(x :: t) = \Sigma(t) \circ \mathcal{E}(x) = \Sigma(t) \ \text{si } \mathcal{E}(x) = \text{id}
$$

Entonces $\Sigma(\text{sinCeros}) = \Sigma(\text{juntas})$. (El filtro va después de juntar porque dos avances pueden sumar cero, como `Avanzar(2)` y `Avanzar(-2)`.)

**Etapa 4: el `match` final.**

- `Nil` $\rightarrow$ `Secuencia(Nil)`, cuyo efecto es $\text{id} = \Sigma(\text{Nil})$.
- `unica :: Nil` $\rightarrow$ `unica` (Regla 4): $\mathcal{E}(\text{unica}) = \Sigma(\text{List}(\text{unica}))$.
- `varias` $\rightarrow$ `Secuencia(varias)`, cuyo efecto es $\Sigma(\text{varias})$ por definición.

Encadenando las cuatro etapas:

$$
\mathcal{E}(\text{simplificar}(o)) = \Sigma(\text{sinCeros}) = \Sigma(\text{juntas}) = \Sigma(\text{aplanadas}) = \mathcal{E}(o)
$$

**Terminación.** `simplificar` solo se llama recursivamente sobre subórdenes estrictos (`orden` en `Repetir`, cada `x` en `ordenes`), y `flatMap`, `foldLeft` y `filter` recorren listas finitas. Siempre termina.

Concluimos por inducción estructural que $\forall o, \forall e : \text{ejecutar}(e, \text{simplificar}(o)) == \text{ejecutar}(e, o)$. $\blacksquare$

En resumen, cada regla del enunciado conserva el efecto de la orden:

| Regla | Dónde se justifica |
|---|---|
| 1. `Avanzar(0)` y `Repetir(0, _)` se vuelven `Secuencia(Nil)` | Casos base y rama $k \leq 0$ de `Repetir`: ambos efectos son $\text{id}$ |
| 2. Dos `Avanzar` seguidos se suman | Etapa 2: $\mathcal{E}(\text{Avanzar}(b)) \circ \mathcal{E}(\text{Avanzar}(a)) = \mathcal{E}(\text{Avanzar}(a + b))$ |
| 3. `Repetir(1, o)` se vuelve `o` | Rama $k = 1$ de `Repetir` |
| 4. Una secuencia de un solo elemento se vuelve ese elemento | Etapas 1 y 4 |
| 5. Las órdenes que no hacen nada se quitan de las secuencias | Etapas 1 y 3: se quitan elementos con efecto $\text{id}$ |

## 7. Conclusión

Se demostró, por inducción estructural sobre `Orden` (con inducción sobre `veces` para `Repetir` e invariantes de ciclo para los `foldLeft`), que:

- `girar` coincide con la rotación de noventa grados del enunciado.
- `ejecutar` coincide con la especificación $\mathcal{E}$ (Teorema 2), y es la base de las demás pruebas.
- `cuantasBasicas` y `profundidad` miden lo que dicen medir.
- `componer`, `rutina`, `aplicarA` y `transformar` se comportan como la ejecución de sus partes (Teoremas 5 a 8).
- `simplificar` no cambia el estado resultante de ejecutar una orden (Teorema 9).