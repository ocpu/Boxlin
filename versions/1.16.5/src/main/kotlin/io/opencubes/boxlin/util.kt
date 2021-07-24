package io.opencubes.boxlin

import net.minecraftforge.fml.DistExecutor

/** @see DistExecutor.runForDist */
fun <R> runForDist(client: () -> R, server: () -> R): R =
  DistExecutor.safeRunForDist({ DistExecutor.SafeSupplier(client) }, { DistExecutor.SafeSupplier(server) })
