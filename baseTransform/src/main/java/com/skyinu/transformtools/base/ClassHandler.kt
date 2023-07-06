package com.skyinu.transformtools.base

import javassist.CtClass

interface ClassHandler {
  fun travelClass(ctClass: CtClass): Boolean
}