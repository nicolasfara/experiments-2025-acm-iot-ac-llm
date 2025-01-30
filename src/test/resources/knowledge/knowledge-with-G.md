**Introduction**

This document explains a Domain Specific Language (DSL) designed for aggregate computing, a paradigm used in collective systems. The language is implemented in Scala, so all expressions must be valid Scala syntax.

We'll explore each language construct with descriptions and supporting examples and tests.

**Context**

Imagine a distributed system where each node runs a local program and communicates with its neighbors. Execution begins when `main()` is called on each node. We assume that between successive calls to `main()` on the same node, all nodes communicate with each other, and the system reaches a consistent state.

Therefore:

```
main()
main()
```
means that the program is executed on the same node multiple times, with communication happening between the calls.

**Interacting with the Environment**

You can read environmental data using the `sense` construct:

```scala
def sense[A](sensor: String): A
```
For example:

```scala
def main(): Double = sense[Double]("temperature")
```

This program returns the ambient temperature.

* If the environment's temperature is 16.0:
  ```
  main() // Output: 16.0
  ```
* If the environment's temperature changes to 20.0:
  ```
  main() // Output: 20.0
  ```

**Temporal Evolution**

The `rep` construct allows you to evolve a value over time, applying a function to its previous value:

```scala
def rep[A](init: => A)(evolve: A => A): A
```

* `init`: The initial value.
* `evolve`: A function that takes the current value and returns the next value.

For example:
```scala
def main(): Int = rep(0)(_ + 1)
```

* `main()` called repeatedly will result in:
  ```
  main() // Output: 0
  main() // Output: 1
  main() // Output: 2
  ```

**Spatial Interaction (Neighborhood)**

Aggregate computing enables interaction with neighbors using the `foldhood` and `nbr` constructs:

```scala
def foldhood[A](init: A)(combine: (A, A) => A)(neighbourExpression: => A): A
def nbr[A](data: => A): A
```

*  `nbr`: Accesses data from a neighbor.
*  `foldhood`: Combines the values from all neighbors (including itself) into a single value.

**Example: Counting Neighbors (including self)**
```scala
def main(): Int = foldhood(0)(_ + _)(nbr(1))
```

Given a network like: `0 - 1 - 2`

Each node has three neighbors, including itself:

```
main() // Output: 3 (for node 0)
main() // Output: 3 (for node 1)
main() // Output: 3 (for node 2)
```

**Example: Counting Neighbors (excluding self) using foldhoodPlus**

The `foldhoodPlus` construct allows excluding the current node itself from the aggregation:

```scala
def foldhoodPlus[A](init: A)(combine: (A, A) => A)(neighbourExpression: => A): A
```

```scala
def main(): Int = foldhoodPlus(0)(_ + _)(nbr(1))
```

Given the same network: `0 - 1 - 2`

Each node has two neighbors:
```
main() // Output: 2
main() // Output: 2
main() // Output: 2
```
**Combining `nbr`, `foldhood`, and `sense`**

These constructs can be combined with `sense` and other Scala expressions.

**Example: Average Temperature of the Neighborhood**

```scala
def main(): Double = {
 val devices = foldhood(0)(_ + _)(nbr(1))
 val temperature = foldhood(0.0)(_ + _)(nbr(sense[Double]("temperature")))
 temperature / devices
}
```

Given a fully connected network with these temperatures:

*  Node 0: 10.0
*  Node 1: 15.0
*  Node 2: 5.0

Each node calculates the average temperature as: `(10 + 15 + 5) / 3 = 10`
```
main() // Output: 10.0 (for all nodes)
```
**Important Restrictions:**

*  **No Nested `nbr`:** You cannot call `nbr` inside another `nbr`:
  ```scala
  foldhood(0)(_ + 1)(nbr(nbr(10))) // This is ILLEGAL and will cause a compilation error.
  ```
*  **No Assigning `nbr` to `val`:** You cannot directly assign the result of `nbr` to a `val`:
   ```scala
   val data = nbr("sensor") // This is ILLEGAL.
   ```
   However, you can assign to a `def`:
   ```scala
   def senseNeighTemperature: Double = nbr(sense[Double]("temperature")) // This is LEGAL.
   ```
**Platform-Specific Sensors**

Special platform sensors can be used instead of `nbr`, such as `nbrRange` (to access the distance to a neighbor).

**Example: Finding the Closest Neighbor**

```scala
def main(): Double = {
  val idsAndNeigh = foldhood(List.empty[(Double, ID)])(_ ++ _)(List(nbrRange() -> nbr(mid())))
  idsAndNeigh.minBy(_._1)._1 // minBy allows to find the min using a specific value
}
```

Given these devices with these neighbor ranges:

*  Node 0: `nbrRange` is 0
*  Node 2: `nbrRange` is 2
*  Node 3: `nbrRange` is 1

The program returns the closest neighbor's `nbrRange`. Node 0's closest neighbor is itself, which has `nbrRange` equal to 0.
```
main() // Output: 0 (on node 0)
```
The result might differ on other nodes.
**Example: Finding the Closest Neighbor (excluding self)**

Using `foldhoodPlus`, you can exclude the node itself when finding the closest neighbor:
```scala
def main(): Double = {
  val idsAndNeigh = foldhoodPlus(List.empty[(Double, ID)])(_ ++ _)(List(nbrRange() -> nbr(mid())))
  idsAndNeigh.minBy(_._1)._1
}
```

Using the same device setup as before:
```
main() // Output: 1 (on node 0, node 3 is the closest with a range of 1)
```

**Spatiotemporal Evolution (Global Computation)**

Combining `rep`, `foldhood`, and `nbr` enables spatiotemporal computations that go beyond local neighborhoods.

**Example: Finding the Minimum ID in the Network**

Given the network `0 - 1 - 2`:

```scala
def main(): Int = rep(Int.MaxValue) {
  minId => foldhood(mid())(_ min _)(nbr(minId))
}
```

*  After the first execution: `0 - 0 - 1` (the node with ID 0 communicates it's lower ID, and then node 1 gets the lower ID from node 0)
*  After the second execution: `0 - 0 - 0` (node 2 gets the lower ID from node 1)
This computation propagates the minimum ID across the network over time. Each times you would write system-wise (collective) computations, you should combine rep and foldhood.

**Conditional Execution (Branching)**

The `branch` construct creates non-communicating partitions within the space:

```scala
def branch[A](cond: => Boolean)(thenB: A)(elseB: A): A
```

*  `cond`: A condition that determines which branch to execute.
*  `thenB`: The expression to evaluate if the condition is true.
*  `elseB`: The expression to evaluate if the condition is false.

**Example: Temperature-Based Alarm (non-communicating)**
```scala
def main(): Boolean = branch(sense[Double]("temperature") > 10) {
  val nodes = foldhood(0)(_ + _)(nbr(1))
  foldhood(0.0)(_ + _)(nbr(sense[Double]("temperature"))) / nodes > 20
} {
  false
}
```
Given the network `0 - 1 - 2 - 0`, with the following temperatures:
*  Node 0: 5
*  Node 1: 25
*  Node 2: 19
The output is:
*  Node 0: `branch` evaluates to `false`, the output is `false`
*  Node 1: `branch` evaluates to `true`, the `nodes` variable is `2`, and the average temperature is `(25+19) / 2` which is greater than 20, the output is `true`.
**Conditional Execution (with communication) `mux`**

The `mux` construct is similar to `branch` but allows neighbors to receive information even if the condition doesn't hold true for the current node:
```scala
def mux[A](cond: => Boolean)(thenB: A)(elseB: A): A
```

**Example: Temperature-Based Alarm (communicating)**

```scala
def main(): Boolean = mux(sense[Double]("temperature") > 10) {
  val nodes = foldhood(0)(_ + _)(nbr(1))
  foldhood(0.0)(_ + _)(nbr(sense[Double]("temperature"))) / nodes > 20
} {
  false
}
```

*  Node 0: The `mux` condition is `false`, the output is `false`, but it is still communicating, and other node will consider him in the `foldhood`
*  Node 1: The `mux` condition is `true`, all the nodes are considered, the average temperature is `(5+25+19)/3` which is not greater than 20, so the result is `false`.

This means that Node 1 will be able to consider data from Node 0, and the alarm might not be triggered.

-------

## Gradient propagation

To compute the distance from a source node to all the other nodes in the network,
you can use a combination of the `rep` and `nbr` constructs.

```scala
def classicGradient(source: Boolean): Double =
    rep(Double.PositiveInfinity) { case d =>
      mux(source) {
        0.0
      } {
        minHoodPlus(nbr(d) + nbrRange())
      }
    }
```

A slight more general version abstracts from the metrics used to compute the distance from the other nodes:

```scala
def classicGradient[V](source: Boolean, metric: () => Double = nbrRange): V =
    rep(Double.PositiveInfinity) { case d =>
      mux(source) {
        0.0
      } {
        minHoodPlus(nbr(d) + metric())
      }
    }
```

Where:
* `source`: A boolean that indicates if the current node is the source of the gradient.
* `minHoodPlus`: A function that returns the minimum value of the neighborhood, excluding the current node.

A generalized version of the gradient propagation can be implemented using the `G` construct.
This generalized version can be used to propagate any value across the network.

```scala
def G[V](source: Boolean, center: V, acc: V => V, metric: () => Double): V
```
Where:
* `source`: A boolean that indicates if the current node is the source of the gradient.
* `center`: The value to be casted.
* `acc`: A function that takes the current value and returns the next value in the propagation
* `metric`: A function that returns the distance to its neighbors. Some valid function may be `nbrRange` or `v => v + 1`

For instance, given this network:
```
1 -- 2 -- 3 -- 4
```
You can cast a value from node 1 to the rest of the network using:
```scala
def main(): Double = G(mid() == 1, sense[Double]("temperature"), temp => temp, nbrRange)
```
If the temperature at node 1 is 10.0, then:
```
main() 
```
will produce
inf -- 10 -- inf -- inf
and if you call again:
```
main() 
```
will produce
10 -- 10 -- 10 -- inf
```
and finally:
```
main()
```
will produce
10 -- 10 -- 10 -- 10
```