package io.opencubes.boxlin

import net.minecraftforge.fml.DistExecutor
import java.util.function.Supplier

/** @see DistExecutor.runForDist */
fun <R> runForDist(client: () -> R, server: () -> R): R =
  DistExecutor.runForDist({ Supplier(client) }, { Supplier(server) })
